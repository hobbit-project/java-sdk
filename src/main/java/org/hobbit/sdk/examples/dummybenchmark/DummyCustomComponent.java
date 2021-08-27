package org.hobbit.sdk.examples.dummybenchmark;

import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DummyCustomComponent implements Component {
    private Logger logger = LoggerFactory.getLogger(DummyCustomComponent.class);


    @Override
    public void init() throws Exception {

    }

    @Override
    public void run() {
        logger.debug("Running DummyCustomComponent & closing it");
    }


    @Override
    public void close() throws IOException {
        logger.debug("Terminated");
    }
}
