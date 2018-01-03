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


public class MultipleCommandsReaction implements CommandReaction {
    private static final Logger logger = LoggerFactory.getLogger(MultipleCommandsReaction.class);
    private final ComponentsExecutor componentsExecutor;
    private final CommandQueueListener commandQueueListener;

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

    private String dataGeneratorImageName;
    private String taskGeneratorImageName;
    private String evalStorageImageName;
    private String evalModuleImageName;

    private String dataGenContainerId;
    private String taskGenContainerId;
    private String systemContainerId;
    private String evalModuleContainerId;
    private String evalStorageContainerId;


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
    public void handleCmd(Byte command, byte[] bytes, String replyTo){

        if (command == Commands.DOCKER_CONTAINER_START){
            String dataString = RabbitMQUtils.readString(bytes);
            StartCommandData startCommandData = gson.fromJson(dataString, StartCommandData.class);
            logger.debug("CONTAINER_START signal received with imageName="+startCommandData.image+"");

            Component compToSubmit = null;
            String containerId = null;

            if (dataGenerator!=null && startCommandData.image.equals(dataGeneratorImageName)) {
                compToSubmit = dataGenerator;
                containerId = dataGeneratorImageName;
            }

            if(taskGenerator!=null && startCommandData.image.equals(taskGeneratorImageName)) {
                compToSubmit = taskGenerator;
                containerId = taskGeneratorImageName;
            }

            if(evalStorage!=null && startCommandData.image.equals(evalStorageImageName)){
                compToSubmit = evalStorage;
                containerId = evalStorageImageName;
            }

            if(evalModule!=null && startCommandData.image.equals(evalModuleImageName)) {
                compToSubmit = evalModule;
                containerId = evalModuleImageName;
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
                logger.error("No component to submit for imageName="+startCommandData.image);
            }
        }


        if(command==Commands.DOCKER_CONTAINER_TERMINATED){
            CommandSender commandSender = null;
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            String containerName = RabbitMQUtils.readString(buffer);
            if(containerName.equals(systemContainerId)){
                String test1="123";
            }
            //if(containerName.equals(dataGenContainerId))
            if(containerName.equals(dataGeneratorImageName))
                commandSender = new CommandSender(Commands.DATA_GENERATION_FINISHED);

            //if(containerName.equals(taskGenContainerId))
            if(containerName.equals(taskGeneratorImageName))
                commandSender = new CommandSender(Commands.TASK_GENERATION_FINISHED);

            synchronized (this){
                if (commandSender!=null){
                    try {
                        commandSender.send();
                    } catch (Exception e) {
                        //Assert.fail(e.getMessage());
                        logger.error(e.getMessage());
                    }
                }
            }
        }

        if (command == Commands.BENCHMARK_FINISHED_SIGNAL){
            try {
                commandQueueListener.terminate();
                componentsExecutor.shutdown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                //Assert.fail(e.getMessage());
            }
        }

        if (command == Commands.BENCHMARK_READY_SIGNAL)
            benchmarkReady = true;

        if (command == Commands.DATA_GENERATOR_READY_SIGNAL)
            dataGenReady = true;

        if (command == Commands.TASK_GENERATOR_READY_SIGNAL)
            taskGenReady = true;

        if (command == Commands.EVAL_STORAGE_READY_SIGNAL)
            evalStorageReady = true;

        if (command == Commands.SYSTEM_READY_SIGNAL)
            systemReady = true;

        synchronized (this){
            if (benchmarkReady &&
                    (dataGenerator==null || dataGenReady) &&
                    (taskGenerator==null || taskGenReady) &&
                    (evalStorage==null || evalStorageReady) &&
                    systemReady && !startBenchmarkCommandSent) {
                startBenchmarkCommandSent = true;
                try {
                    new CommandSender(Commands.START_BENCHMARK_SIGNAL, systemContainerId).send();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    //Assert.fail(e.getMessage());
                }
            }
        }

    }
}
