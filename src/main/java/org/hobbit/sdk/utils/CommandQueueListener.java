package org.hobbit.sdk.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.sdk.utils.commandreactions.CommandReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roman Katerinenko
 */
public class CommandQueueListener extends AbstractPlatformConnectorComponent {
    private static final Logger logger = LoggerFactory.getLogger(CommandQueueListener.class);

    private final Semaphore blockingSemaphore = new Semaphore(0, true);
    private final Semaphore terminationSemaphore = new Semaphore(0, true);
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private CommandReaction[] commandReactions = new CommandReaction[0];
    private String replyTo;
//    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private boolean catchedExceptions = false;

    @Override
    public void init() throws Exception {
        logger.debug("Initializing...");
        super.init();
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

    public String submit(String imageName, String[] envVariables) {
        return super.createContainer(imageName, envVariables);
    }

    public String createContainer(String imageName, String[] envVariables) {
        return super.createContainer(imageName, envVariables);
    }

    public String createContainer(String imageName, String containerType, String[] envVariables) {
        String ret = super.createContainer(imageName, containerType, envVariables);
        return ret;
    }

//    @Override
    protected void handleCmd(byte[] bytes, String replyTo) {
        this.replyTo = replyTo;
        try {
            super.handleCmd(bytes, replyTo);
        } catch (Throwable e) {
            logger.error("Catched exception while handling command.", e);
            catchedExceptions = true;
        }
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
            } catch (Throwable e) {
                logger.error("Failed to handle command with {}", commandReaction.getClass().getSimpleName(), e);
                catchedExceptions = true;
            }
        }
    }

    public boolean anyExceptions() {
        return catchedExceptions;
    }

}