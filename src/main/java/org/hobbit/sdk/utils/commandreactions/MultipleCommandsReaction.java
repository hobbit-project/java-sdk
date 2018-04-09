package org.hobbit.sdk.utils.commandreactions;

import com.google.gson.Gson;
import com.rabbitmq.client.MessageProperties;
import org.hobbit.core.Commands;
import org.hobbit.core.components.Component;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.CommandSender;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static org.hobbit.sdk.CommonConstants.SYSTEM_CONTAINERS_FINISHED;


public class MultipleCommandsReaction implements CommandReaction {
    private static final Logger logger = LoggerFactory.getLogger(MultipleCommandsReaction.class);
    private final ComponentsExecutor componentsExecutor;
    private final CommandQueueListener commandQueueListener;

    private int dataGeneratorsCount = 0;
    private int taskGeneratorsCount = 0;
    private int systemContainersCount = 0;

    private boolean benchmarkReady = false;
    private boolean dataGenReady = false;
    private boolean taskGenReady = false;
    private boolean evalStorageReady = false;
    private boolean systemReady = false;

    private boolean startBenchmarkCommandSent = false;

    private Component dataGenerator;
    private Component taskGenerator;
    private Component evalStorage;
    private Component evalModule;
    private Component systemSlaveContainer;

    private String dataGeneratorImageName;
    private String taskGeneratorImageName;
    private String evalStorageImageName;
    private String evalModuleImageName;
    private String systemSlaveImageName;

    private String systemContainerId;



    private Gson gson = new Gson();

    public MultipleCommandsReaction(ComponentsExecutor componentsExecutor, CommandQueueListener commandQueueListener){
        this.componentsExecutor = componentsExecutor;
        this.commandQueueListener = commandQueueListener;
    }

    public MultipleCommandsReaction dataGenerator(Component component){
        this.dataGenerator = component;
        return this;
    }

    public MultipleCommandsReaction dataGeneratorImageName(String value){
        this.dataGeneratorImageName = value;
        return this;
    }

    public MultipleCommandsReaction taskGenerator(Component component){
        this.taskGenerator = component;
        return this;
    }

    public MultipleCommandsReaction taskGeneratorImageName(String value){
        this.taskGeneratorImageName = value;
        return this;
    }

    public MultipleCommandsReaction evalStorage(Component component){
        this.evalStorage = component;
        return this;
    }

    public MultipleCommandsReaction evalStorageImageName(String value){
        this.evalStorageImageName = value;
        return this;
    }

    public MultipleCommandsReaction systemSlaveContainer(Component value){
        this.systemSlaveContainer = value;
        return this;
    }

    public MultipleCommandsReaction systemSlaveImageName(String value){
        this.systemSlaveImageName = value;
        return this;
    }

    public MultipleCommandsReaction systemContainerId(String value){
        this.systemContainerId = value;
        return this;
    }


    public MultipleCommandsReaction evalModule(Component value){
        this.evalModule = value;
        return this;
    }

    public MultipleCommandsReaction evalModuleImageName(String value){
        this.evalModuleImageName = value;
        return this;
    }


    @Override
    public void handleCmd(Byte command, byte[] bytes, String replyTo) throws Exception {
        if(systemContainerId==null) {
            throw new Exception("SystemContainerId not specified. Impossible to continue");
        }

        if (command == Commands.DOCKER_CONTAINER_START){
            String dataString = RabbitMQUtils.readString(bytes);
            StartCommandData startCommandData = gson.fromJson(dataString, StartCommandData.class);

            logger.debug("CONTAINER_START signal received with imageName="+startCommandData.image+"");

            Component compToSubmit = null;
            String containerId = null;

            if (dataGenerator!=null && startCommandData.image.equals(dataGeneratorImageName)) {
                compToSubmit = dataGenerator.getClass().getConstructor(null).newInstance(null);
                containerId = dataGeneratorImageName;
                dataGeneratorsCount++;
            }

            if(taskGenerator!=null && startCommandData.image.equals(taskGeneratorImageName)) {
                compToSubmit = taskGenerator.getClass().getConstructor(null).newInstance(null);
                containerId = taskGeneratorImageName;
                taskGeneratorsCount++;
            }

            if(evalStorage!=null && startCommandData.image.equals(evalStorageImageName)){
                compToSubmit = evalStorage;
                containerId = evalStorageImageName;
            }

            if(evalModule!=null && startCommandData.image.equals(evalModuleImageName)) {
                compToSubmit = evalModule;
                containerId = evalModuleImageName;
            }

            if(systemSlaveContainer !=null && startCommandData.image.equals(systemContainerId)) {
                compToSubmit = systemSlaveContainer.getClass().getConstructor(null).newInstance(null);
                systemContainersCount++;
                containerId = systemContainerId+"_"+String.valueOf(systemContainersCount);
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
                throw new Exception("No component to start as imageName="+startCommandData.image);

            }
        }


        if(command==Commands.DOCKER_CONTAINER_TERMINATED){
            CommandSender commandSender = null;
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            String containerName = RabbitMQUtils.readString(buffer);

            logger.debug("DOCKER_CONTAINER_TERMINATED {} received", containerName);

            String commandToSend = null;
            //if(containerName.equals(dataGenContainerId))
            if(containerName.equals(dataGeneratorImageName)){
                dataGeneratorsCount--;
                if(dataGeneratorsCount==0) {
                    commandSender = new CommandSender(Commands.DATA_GENERATION_FINISHED);
                    commandToSend = "DATA_GENERATION_FINISHED";
                }
            }

            //if(containerName.equals(taskGenContainerId))
            if(containerName.equals(taskGeneratorImageName)) {
                taskGeneratorsCount--;
                if(taskGeneratorsCount==0){
                    commandSender = new CommandSender(Commands.TASK_GENERATION_FINISHED);
                    commandToSend = "TASK_GENERATION_FINISHED";
                }
            }

            if(containerName.equals(systemContainerId)) {
                systemContainersCount--;
                if(systemContainersCount==0){
                    commandSender = new CommandSender(SYSTEM_CONTAINERS_FINISHED);
                    commandToSend = "SYSTEM_CONTAINERS_FINISHED";
                }
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

        if (command == Commands.BENCHMARK_FINISHED_SIGNAL){
            logger.debug("BENCHMARK_FINISHED_SIGNAL received");
            try {
                commandQueueListener.terminate();
                componentsExecutor.shutdown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                //Assert.fail(e.getMessage());
            }
        }

        if (command == Commands.BENCHMARK_READY_SIGNAL) {
            benchmarkReady = true;
            logger.debug("BENCHMARK_READY_SIGNAL signal received");
        }

        if (command == Commands.DATA_GENERATOR_READY_SIGNAL) {
            dataGenReady = true;
            logger.debug("DATA_GENERATOR_READY_SIGNAL signal received");
        }

        if (command == Commands.TASK_GENERATOR_READY_SIGNAL) {
            taskGenReady = true;
            logger.debug("TASK_GENERATOR_READY_SIGNAL signal received");
        }

        if (command == Commands.EVAL_STORAGE_READY_SIGNAL) {
            evalStorageReady = true;
            logger.debug("EVAL_STORAGE_READY_SIGNAL signal received");
        }

        if (command == Commands.SYSTEM_READY_SIGNAL) {
            systemReady = true;
            logger.debug("SYSTEM_READY_SIGNAL signal received");
        }

        synchronized (this){
            if (benchmarkReady &&
                    (dataGenerator==null || dataGenReady) &&
                    (taskGenerator==null || taskGenReady) &&
                    (evalStorage==null || evalStorageReady) &&
                    systemReady && !startBenchmarkCommandSent) {
                startBenchmarkCommandSent = true;
                try {
                    logger.debug("sending START_BENCHMARK_SIGNAL");
                    new CommandSender(Commands.START_BENCHMARK_SIGNAL, systemContainerId).send();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    //Assert.fail(e.getMessage());
                }
            }
        }

    }
}
