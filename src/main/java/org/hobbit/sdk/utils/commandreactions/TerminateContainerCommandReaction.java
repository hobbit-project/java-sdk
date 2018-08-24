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

    public class TerminateContainerCommandReaction implements CommandReaction {
        private static final Logger logger = LoggerFactory.getLogger(PlatformCommandsReaction.class);



        public TerminateContainerCommandReaction(CommandReactionsBuilder builder){

//


        }

        @Override
        public void handleCmd(Byte command, byte[] bytes, String replyTo) throws Exception {


            if(command==Commands.DOCKER_CONTAINER_TERMINATED){
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


        }


    }