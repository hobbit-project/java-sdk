package org.hobbit.sdk.examples.dummybenchmark;

import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DummyCustomComponent extends AbstractCommandReceivingComponent {
    private Logger logger = LoggerFactory.getLogger(DummyCustomComponent.class);

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
    }

    @Override
    public void run() throws Exception {
        logger.debug("Initialized: {}", getHobbitSessionId());
        close();
        logger.debug("Terminated");
    }



    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {

    }
}
