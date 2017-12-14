package org.hobbit.sdk.utils.commandreactions;

import org.hobbit.core.components.Component;
import org.hobbit.core.data.StopCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.core.Commands;
import org.hobbit.sdk.utils.CommandSender;

import java.io.IOException;
import java.nio.ByteBuffer;
//import org.junit.Assert;

/**
 * @author Roman Katerinenko
 */
public class TerminateServicesReaction implements CommandReaction {
    private final String dataGenContainerId;
    private final String taskGenContainerId;
    private final String systemContainerId;
    private final String evalModuleContainerId;
    private final String evalStorageContainerId;
    private final CommandQueueListener commandQueueListener;
    private final ComponentsExecutor componentsExecutor;



    public TerminateServicesReaction(String dataGenContainerId,
                                     String taskGenContainerId,
                                     String evalStorageContainerId,
                                     String systemContainerId,
                                     String evalModuleContainerId,
                                     CommandQueueListener commandQueueListener,
                                     ComponentsExecutor componentsExecutor) {
        this.dataGenContainerId = dataGenContainerId;
        this.taskGenContainerId = taskGenContainerId;
        this.evalStorageContainerId = evalStorageContainerId;
        this.systemContainerId = systemContainerId;
        this.evalModuleContainerId = evalModuleContainerId;
        this.commandQueueListener = commandQueueListener;
        this.componentsExecutor = componentsExecutor;
    }

    @Override
    public void handleCmd(Byte command, byte[] data, String replyTo){

        CommandSender commandSender = null;
        if(command==Commands.DOCKER_CONTAINER_TERMINATED){
            ByteBuffer buffer = ByteBuffer.wrap(data);
            String containerName = RabbitMQUtils.readString(buffer);
            if(containerName==systemContainerId){

            }
            if(containerName.equals(dataGenContainerId))
                commandSender = new CommandSender(Commands.DATA_GENERATION_FINISHED);

            if(containerName.equals(taskGenContainerId))
                commandSender = new CommandSender(Commands.TASK_GENERATION_FINISHED);
        }

        if(command==Commands.EVAL_MODULE_FINISHED_SIGNAL) {
            String test="123";
//            try {
//                commandQueueListener.notifyAboutTermination(evalModuleContainerId, 0);
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
        }

        if (command == Commands.BENCHMARK_FINISHED_SIGNAL) {
            try {
                commandQueueListener.terminate();
                componentsExecutor.shutdown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                //Assert.fail(e.getMessage());
            }
        }

        synchronized (this){
            if (commandSender!=null /*&& !commandSent*/) {
                //commandSent = true;
                try {
                    commandSender.send();
                } catch (Exception e) {
                    //Assert.fail(e.getMessage());
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}