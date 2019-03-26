package org.hobbit.sdk.utils.commandreactions;

import com.google.gson.Gson;
import com.rabbitmq.client.MessageProperties;
import org.hobbit.core.Commands;
import org.hobbit.core.components.Component;
//import org.hobbit.core.data.ExecuteCommandData;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.builders.PullBasedDockersBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.CommandSender;
import org.hobbit.sdk.utils.ComponentsExecutor;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

import static org.hobbit.sdk.Constants.HOBBIT_NETWORKS;

public class ContainerCommandsReaction implements CommandReaction {
    private static final Logger logger = LoggerFactory.getLogger(ContainerCommandsReaction.class);

    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    private Component benchmarkController;
    private Component dataGenerator;
    private Component taskGenerator;
    private Component evalStorage;
    private Component evalModule;
    private Component systemAdapter;

    private String benchmarkControllerImageName;
    private String dataGeneratorImageName;
    private String taskGeneratorImageName;
    private String evalStorageImageName;
    private String evalModuleImageName;
    private String systemAdapterImageName;

    private int dataGeneratorsCount = 0;
    private int taskGeneratorsCount = 0;
    private int systemContainersCount = 0;

    private Gson gson = new Gson();

    private Map<String, Component> runningComponents = new HashMap<>();

    private Map<String, Component> customContainers = new HashMap<>();
    private Map<String, Integer> customContainersRunning = new HashMap<>();
    //private String systemContainerId = null;

    public ContainerCommandsReaction(CommandReactionsBuilder builder){
        this.componentsExecutor = builder.componentsExecutor;
        this.commandQueueListener = builder.commandQueueListener;

        this.benchmarkController=builder.benchmarkController;
        this.dataGenerator=builder.dataGenerator;
        this.taskGenerator=builder.taskGenerator;
        this.evalStorage=builder.evalStorage;
        this.evalModule=builder.evalModule;
        this.systemAdapter=builder.systemAdapter;

        this.benchmarkControllerImageName = builder.benchmarkControllerImageName;
        this.dataGeneratorImageName = builder.dataGeneratorImageName;
        this.taskGeneratorImageName = builder.taskGeneratorImageName;
        this.evalStorageImageName = builder.evalStorageImageName ;
        this.evalModuleImageName = builder.evalModuleImageName;
        this.systemAdapterImageName = builder.systemAdapterImageName;
        this.customContainers = builder.customContainers;
    }

    @Override
    public void handleCmd(Byte command, byte[] bytes, String replyTo) throws Exception {

        if (command == Commands.DOCKER_CONTAINER_START){
            String dataString = RabbitMQUtils.readString(bytes);
            StartCommandData startCommandData = gson.fromJson(dataString, StartCommandData.class);

            logger.debug("CONTAINER_START command received with imageName="+startCommandData.image+"");

            String containerId=null;
            Component compToSubmit = null;

            String[] splitted = startCommandData.getImage().split("/");
            String cleanedImageName=splitted[splitted.length-1].split(":")[0];

            if (benchmarkController!=null && startCommandData.image.equals(benchmarkControllerImageName)) {
                compToSubmit = benchmarkController;
                containerId = cleanedImageName;
            }else

            if (dataGenerator!=null && startCommandData.image.equals(dataGeneratorImageName)) {
                compToSubmit = dataGenerator;
                containerId = cleanedImageName+"_"+dataGeneratorsCount;
                dataGeneratorsCount++;
            }else

            if(taskGenerator!=null && startCommandData.image.equals(taskGeneratorImageName)) {
                compToSubmit = taskGenerator;
                containerId = cleanedImageName+"_"+taskGeneratorsCount;
//                if(AbstractDockerizer.class.isInstance(taskGenerator)){
//                    compToSubmit = ((AbstractDockerizer)taskGenerator).clone(new ArrayList(Arrays.asList(startCommandData.environmentVariables)));
//                    containerId = ((AbstractDockerizer)compToSubmit).createContainerWithRemoveAllPrevs(startCommandData.environmentVariables);
//                }else {
//                    compToSubmit = taskGenerator.getClass().getConstructor().newInstance();
//                    containerId = taskGeneratorImageName+"_"+taskGeneratorsCount;
//                }
                taskGeneratorsCount++;
            }else

            if(evalStorage!=null && startCommandData.image.equals(evalStorageImageName)){
                compToSubmit = evalStorage;
                containerId = cleanedImageName;
            }else

            if(evalModule!=null && startCommandData.image.equals(evalModuleImageName)) {
                compToSubmit = evalModule;
                containerId = cleanedImageName;

            }else

            if(systemAdapter !=null && startCommandData.image.equals(systemAdapterImageName)) {
                compToSubmit = systemAdapter;
                containerId = cleanedImageName+"_"+systemContainersCount;
                systemContainersCount++;
            }else if(customContainers.containsKey(startCommandData.image)){

                Component customComponent = customContainers.get(startCommandData.image);
                int runningCustomContainersCount = (customContainersRunning.containsKey(cleanedImageName)? customContainersRunning.get(cleanedImageName) :0);

                if(AbstractDockerizer.class.isInstance(customComponent)){
                    List<String> envVars = new ArrayList(Arrays.asList(startCommandData.environmentVariables));
                    compToSubmit = ((AbstractDockerizer)customComponent).clone(envVars);
                    containerId = ((AbstractDockerizer)compToSubmit).createContainerWithRemoveAllPrevs(envVars.toArray(new String[0]));
                }else {
                    compToSubmit = customComponent.getClass().getConstructor().newInstance();
                    containerId = cleanedImageName+"_"+runningCustomContainersCount;
                }
                runningCustomContainersCount++;
                customContainersRunning.put(cleanedImageName, runningCustomContainersCount);
            }else{
                String imgName = startCommandData.image;
                if(!imgName.contains(":"))
                    imgName+=":latest";
                logger.info("Trying to create container with imageName="+imgName);
                compToSubmit = new PullBasedDockersBuilder(imgName).addNetworks(HOBBIT_NETWORKS).build();
                containerId = ((AbstractDockerizer)compToSubmit).createContainerWithRemoveAllPrevs(startCommandData.environmentVariables);
            }

            if(compToSubmit!=null){

                if(AbstractDockerizer.class.isInstance(compToSubmit)){
                    compToSubmit = ((AbstractDockerizer)compToSubmit).clone(new ArrayList(Arrays.asList(startCommandData.environmentVariables)));
                    containerId = ((AbstractDockerizer)compToSubmit).createContainerWithRemoveAllPrevs(startCommandData.environmentVariables);
                }else {
                    compToSubmit = compToSubmit.getClass().getConstructor().newInstance();
                }

                componentsExecutor.submit(compToSubmit, containerId, startCommandData.getEnvironmentVariables());
                runningComponents.put(containerId, compToSubmit);
                //synchronized (this) {
                    if (containerId!=null){
                        try {
                            new CommandSender(containerId.getBytes(), MessageProperties.PERSISTENT_BASIC, replyTo).send();

                        } catch (Exception e) {
                            logger.error("Failed to send message: {}", e.getMessage());
                            e.printStackTrace();
                            //Assert.fail(e.getMessage());
                        }
                    }else{
                        String test="123";
                    }
                //}
            }else{
                logger.error("No component to submit for the imageName="+startCommandData.image);
                //throw new Exception("No component to submit for the imageName="+startCommandData.image);
                System.exit(1);
            }
        }else if(command==Commands.DOCKER_CONTAINER_TERMINATED){
            CommandSender commandSender = null;
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            String containerName = RabbitMQUtils.readString(buffer);

            logger.debug("DOCKER_CONTAINER_TERMINATED {} received", containerName);

            if(!System.getenv().containsKey("BENCHMARK_CONTAINER_ID"))
                throw new Exception("BENCHMARK_CONTAINER_ID is not specified as env variable. Specify it where you submit benchmark/create benchmark container in checkHealth");

            String commandToSend = null;
//            //if(containerName.equals(dataGenContainerId))
//            if(dataGeneratorImageName!=null && containerName.startsWith(dataGeneratorImageName)){
//                dataGeneratorsCount--;
//                if(dataGeneratorsCount==0) {
//                    //commandSender = new CommandSender(Commands.DATA_GENERATION_FINISHED);
//                    //commandToSend = "DATA_GENERATION_FINISHED";
//                }
//            }
//
//            //if(containerName.equals(taskGenContainerId))
//            if(taskGeneratorImageName!=null && containerName.startsWith(taskGeneratorImageName)) {
//                taskGeneratorsCount--;
//                if(taskGeneratorsCount==0){
//                    //commandSender = new CommandSender(Commands.TASK_GENERATION_FINISHED);
//                    //commandToSend = "TASK_GENERATION_FINISHED";
//                }
//            }
//
//            if(containerName.equals(systemAdapterImageName)){
//                //if(systemContainersCount>0) {
//                    systemContainersCount--;
//                    //if (systemContainersCount == 0) {
//                        //commandSender = new CommandSender(Commands.DOCKER_CONTAINER_TERMINATED, systemAdapterImageName.getBytes());
//                        //commandToSend = "SYSTEM_CONTAINERS_FINISHED";
//                    //}
//                //}
//            }

            if(containerName.equals(System.getenv().get("BENCHMARK_CONTAINER_ID"))){
                commandSender = new CommandSender(Commands.BENCHMARK_FINISHED_SIGNAL);
                commandToSend = "BENCHMARK_FINISHED_SIGNAL";
            }

            synchronized (this){
                if (commandSender!=null){
                    try {
                        logger.debug("Sending "+commandToSend+" signal");
                        commandSender.send();
                    } catch (Exception e) {
                        //Assert.fail(e.getMessage());
                        logger.error(e.getMessage());
                    }
                }
            }
        }
//        else if(command==Commands.EXECUTE_ASYNC_COMMAND){
//            //Supported only for the extended core, not supported in the online platform
//            CommandSender commandSender = null;
//            String dataString = RabbitMQUtils.readString(bytes);
//            ExecuteCommandData executeCommandData = gson.fromJson(dataString, ExecuteCommandData.class);
//
//            logger.debug("EXECUTE_COMMAND_IN_CONTAINER {} received", executeCommandData.containerId);
//
//            AbstractDockerizer runningContainer;
//            if(!runningComponents.containsKey(executeCommandData.containerId)){
//                runningContainer = new PullBasedDockersBuilder("container-"+executeCommandData.containerId).containerName(executeCommandData.containerId).build();
//                //logger.error("No running container {} found", executeCommandData.containerId);
//                //return;
//            }else
//                runningContainer = (AbstractDockerizer)runningComponents.get(executeCommandData.containerId);
//
//            Boolean result0 = runningContainer.execAsyncCommand(executeCommandData.containerId, executeCommandData.command);
//            String result = (result0?"Succeeded":"Failed");
//            synchronized (this) {
//                try {
//                    logger.debug("Sending command execution result: {}", result);
//                    new CommandSender(result.getBytes(), MessageProperties.PERSISTENT_BASIC, replyTo).send();
//
//                } catch (Exception e){
//                    logger.error("Failed send reply: ", e.getLocalizedMessage());
//                    Assert.fail(e.getMessage());
//                }
//            }
//
//
//        }


    }


}