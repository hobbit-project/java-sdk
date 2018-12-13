package org.hobbit.sdk.utils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.utils.commandreactions.CommandReaction;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * @author Roman Katerinenko
 */
public class CommandQueueListener extends AbstractPlatformConnectorComponent {
    private static final Logger logger = LoggerFactory.getLogger(CommandQueueListener.class);

    private final Semaphore blockingSemaphore = new Semaphore(0, true);
    private final Semaphore terminationSemaphore = new Semaphore(0, true);
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private CommandReaction[] commandReactions=new CommandReaction[0];
    private String replyTo;
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Override
    public void init() throws Exception {
        logger.debug("Initializing...");
        String originalRabbitHostName = System.getenv(Constants.RABBIT_MQ_HOST_NAME_KEY);
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost");
        super.init();
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, originalRabbitHostName);
        addCommandHeaderId(Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS);
    }

    @Override
    public void run() throws Exception {
        logger.debug("Initialized. SessionID: {}", getHobbitSessionId());
        countDownLatch.countDown();
        terminationSemaphore.acquire();
        logger.debug("Got termination signal. Terminating...");
        close();
        logger.debug("Terminated");
    }

    public String submit(String imageName, String[] envVariables){
        return super.createContainer(imageName, envVariables);
    }

    public String createContainer(String imageName, String[] envVariables){
        return super.createContainer(imageName, envVariables);
    }

    public String createContainer(String imageName, String containerType, String[] envVariables){
        String originalRabbitHostName = rabbitMQHostName;
        rabbitMQHostName = "rabbit";
        String ret = super.createContainer(imageName, containerType, envVariables);
        rabbitMQHostName = originalRabbitHostName;
        return ret;
    }


//    @Override
    protected void handleCmd(byte[] bytes, String replyTo) {
        this.replyTo = replyTo;
        super.handleCmd(bytes, replyTo);
    }


    public void waitForInitialisation() throws InterruptedException {
        countDownLatch.await();
    }

    public void terminate() {
        terminationSemaphore.release();
        blockingSemaphore.release();
        logger.debug("Terminated");
    }

    public void waitForTermination() throws InterruptedException {
        blockingSemaphore.acquire();
    }

    public void setCommandReactions(CommandReaction... commandReactions) {
        this.commandReactions = commandReactions;
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        for (CommandReaction commandReaction : commandReactions) {
            try {
                commandReaction.handleCmd(command, data, replyTo);
            } catch (Exception e) {
                logger.error("Failed to handle command with {}: {}",commandReaction.toString(), e.getMessage());
            }
        }
    }

}