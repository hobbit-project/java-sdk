package com.agtinternational.hobbit.sdk.utils;

import com.agtinternational.hobbit.sdk.utils.commandreactions.CommandReaction;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * @author Roman Katerinenko
 */
public class CommandQueueListener extends AbstractCommandReceivingComponent {
    private static final Logger logger = LoggerFactory.getLogger(CommandQueueListener.class);

    private final Semaphore blockingSemaphore = new Semaphore(0, true);
    private final Semaphore terminationSemaphore = new Semaphore(0, true);
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private CommandReaction[] commandReactions = new CommandReaction[0];

    @Override
    public void init() throws Exception {
        logger.debug("Initializing...");
        super.init();
        addCommandHeaderId(Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS);
    }

    @Override
    public void run() throws Exception {
        logger.debug("Initialized. Waiting for termination signal");
        countDownLatch.countDown();
        terminationSemaphore.acquire();
        logger.debug("Got termination signal. Terminating...");
        close();
        logger.debug("Terminated");
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
            commandReaction.accept(command, data);
        }
    }
}