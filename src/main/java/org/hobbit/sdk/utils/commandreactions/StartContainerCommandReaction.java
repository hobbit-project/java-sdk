package org.hobbit.sdk.utils.commandreactions;

import com.google.gson.Gson;
import com.rabbitmq.client.MessageProperties;
import org.hobbit.core.Commands;
import org.hobbit.core.components.Component;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.CommandSender;
import org.hobbit.sdk.utils.ComponentsExecutor;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StartContainerCommandReaction implements CommandReaction {
    private static final Logger logger = LoggerFactory.getLogger(PlatformCommandsReaction.class);

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

    private Map<String, Component> customContainers = new HashMap<>();
    private Map<String, Integer> customContainersRunning = new HashMap<>();
    //private String systemContainerId = null;

    public StartContainerCommandReaction(CommandReactionsBuilder builder){
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

            logger.debug("CONTAINER_START signal received with imageName="+startCommandData.image+"");

            Component compToSubmit = null;
            String containerId = null;

            if (benchmarkController!=null && startCommandData.image.equals(benchmarkControllerImageName)) {
                compToSubmit = benchmarkController;
                containerId = benchmarkControllerImageName;
            }else

            if (dataGenerator!=null && startCommandData.image.equals(dataGeneratorImageName)) {
                if(AbstractDockerizer.class.isInstance(dataGenerator)){
                    compToSubmit = ((AbstractDockerizer)dataGenerator).clone(new ArrayList(Arrays.asList(startCommandData.environmentVariables)));
                    containerId = ((AbstractDockerizer)compToSubmit).getContainerName();
                }else {
                    compToSubmit = dataGenerator.getClass().getConstructor().newInstance();
                    containerId = dataGeneratorImageName+"_"+dataGeneratorsCount;
                }
                dataGeneratorsCount++;
            }else

            if(taskGenerator!=null && startCommandData.image.equals(taskGeneratorImageName)) {
                if(AbstractDockerizer.class.isInstance(taskGenerator)){
                    compToSubmit = ((AbstractDockerizer)taskGenerator).clone(new ArrayList(Arrays.asList(startCommandData.environmentVariables)));
                    containerId = ((AbstractDockerizer)compToSubmit).getContainerName();
                }else {
                    compToSubmit = taskGenerator.getClass().getConstructor().newInstance();
                    containerId = taskGeneratorImageName+"_"+taskGeneratorsCount;
                }
                taskGeneratorsCount++;
            }else

            if(evalStorage!=null && startCommandData.image.equals(evalStorageImageName)){
                compToSubmit = evalStorage;
                containerId = evalStorageImageName;
            }else

            if(evalModule!=null && startCommandData.image.equals(evalModuleImageName)) {
                compToSubmit = evalModule;
                containerId = evalModuleImageName;
            }else

            if(systemAdapter !=null && startCommandData.image.equals(systemAdapterImageName)) {
                if(AbstractDockerizer.class.isInstance(systemAdapter)){
                    compToSubmit = ((AbstractDockerizer)systemAdapter).clone(new ArrayList(Arrays.asList(startCommandData.environmentVariables)));
                    //containerId = ((AbstractDockerizer)compToSubmit).getContainerName();
                }else {
                    compToSubmit = systemAdapter.getClass().getConstructor().newInstance();
                    //containerId = systemAdapterImageName+"_"+systemContainersCount;
                }
                containerId = systemAdapterImageName+"_"+systemContainersCount;
                systemContainersCount++;
            }else

            if(customContainers.containsKey(startCommandData.image)){
                String imageName = startCommandData.image;
                Component customComponent = customContainers.get(imageName);
                int runningCustomContainersCount = (customContainersRunning.containsKey(imageName)? customContainersRunning.get(imageName) :0);
                if(AbstractDockerizer.class.isInstance(customComponent)){
                    compToSubmit = ((AbstractDockerizer)customComponent).clone(new ArrayList(Arrays.asList(startCommandData.environmentVariables)));
                    containerId = ((AbstractDockerizer)compToSubmit).getContainerName();
                }else {
                    compToSubmit = customComponent.getClass().getConstructor().newInstance();
                    containerId = imageName+"_"+runningCustomContainersCount;
                }
                runningCustomContainersCount++;
                customContainersRunning.put(imageName, runningCustomContainersCount);
            }

            if(compToSubmit!=null){
                componentsExecutor.submit(compToSubmit, containerId, startCommandData.getEnvironmentVariables());
                synchronized (this) {
                    if (containerId!=null){
                        try {
                            new CommandSender(containerId.getBytes(), MessageProperties.PERSISTENT_BASIC, replyTo).send();

                        } catch (Exception e) {
                            Assert.fail(e.getMessage());
                        }
                    }else{
                        String test="123";
                    }
                }
            }else{
                logger.warn("No component to submit for the imageName="+startCommandData.image);
            }
        }

        if(command==Commands.DOCKER_CONTAINER_TERMINATED){
            CommandSender commandSender = null;
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            String containerName = RabbitMQUtils.readString(buffer);

            logger.debug("DOCKER_CONTAINER_TERMINATED {} received", containerName);

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

            if(containerName.equals(benchmarkControllerImageName)){
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


    }


}