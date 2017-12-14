package org.hobbit.sdk.utils.commandreactions;

import com.google.gson.Gson;
import com.rabbitmq.client.MessageProperties;
import org.hobbit.core.Commands;
import org.hobbit.core.components.Component;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.data.StopCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.CommandSender;
import org.junit.Assert;

import java.io.IOException;


public class StartStopContainersReaction implements CommandReaction {
    private final ComponentsExecutor componentsExecutor;
    private final CommandQueueListener commandQueueListener;

    private final String dataGeneratorImageName;
    private final String taskGeneratorImageName;
    private final String evalStorageImageName;
    private final String evalModuleImageName;

    private final String dataGenContainerId;
    private final String taskGenContainerId;
    private final String systemContainerId;
    private final String evalModuleContainerId;
    private final String evalStorageContainerId;

    private final Component dataGenerator;
    private final Component taskGenerator;
    private final Component evalStorage;
    private final Component system;
    private final Component evalModule;

    private boolean benchmarkReady = false;
    private boolean systemReady = false;
    private boolean commandSent = false;
    private Gson gson = new Gson();

    public StartStopContainersReaction(String dataGeneratorImageName,
                                       String taskGeneratorImageName,
                                       String evalStorageImageName,
                                       String evalModuleImageName,

                                       String dataGenContainerId,
                                       String taskGenContainerId,
                                       String evalStorageContainerId,
                                       String systemContainerId,
                                       String evalModuleContainerId,

                                       Component dataGenerator,
                                       Component taskGenerator,
                                       Component evalStorage,
                                       Component system,
                                       Component evalModule,

                                       ComponentsExecutor componentsExecutor,
                                       CommandQueueListener commandQueueListener
                                         ){

        this.dataGeneratorImageName = dataGeneratorImageName;
        this.taskGeneratorImageName = taskGeneratorImageName;
        this.evalStorageImageName = evalStorageImageName;
        this.evalModuleImageName = evalModuleImageName;

        this.dataGenContainerId = dataGenContainerId;
        this.taskGenContainerId = taskGenContainerId;
        this.evalStorageContainerId = evalStorageContainerId;
        this.systemContainerId = systemContainerId;
        this.evalModuleContainerId = evalModuleContainerId;

        this.dataGenerator = dataGenerator;
        this.taskGenerator = taskGenerator;
        this.evalStorage = evalStorage;
        this.system = system;
        this.evalModule = evalModule;

        this.componentsExecutor = componentsExecutor;
        this.commandQueueListener = commandQueueListener;
    }


    @Override
    public void handleCmd(Byte command, byte[] bytes, String replyTo){

        String containerId = null;
        if (command == Commands.DOCKER_CONTAINER_START){
            benchmarkReady = true;
            String dataString = RabbitMQUtils.readString(bytes);
            StartCommandData startCommandData = gson.fromJson(dataString, StartCommandData.class);

            Component compToSubmit = null;

            if (startCommandData.image.equals(dataGeneratorImageName))
                compToSubmit = dataGenerator;

            if(startCommandData.image.equals(taskGeneratorImageName))
                compToSubmit = taskGenerator;

            if(startCommandData.image.equals(evalStorageImageName))
                compToSubmit = evalStorage;

            if(startCommandData.image.equals(evalModuleImageName))
                compToSubmit = evalModule;

            if(compToSubmit!=null){
                componentsExecutor.submit(compToSubmit);
                if(AbstractDockerizer.class.isInstance(compToSubmit))
                    containerId = ((AbstractDockerizer)compToSubmit).getContainerName();
                else
                    containerId = compToSubmit.getClass().getSimpleName();
            }
        }

        synchronized (this) {
            if (containerId!=null){
                commandSent = true;
                try {
                    new CommandSender(replyTo, containerId.getBytes(), MessageProperties.PERSISTENT_BASIC).send();

                } catch (Exception e) {
                    Assert.fail(e.getMessage());
                }
            }
        }

    }
}
