package com.agtinternational.hobbit.sdk.examples.system;

import org.hobbit.core.components.AbstractSystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.agtinternational.hobbit.sdk.CommonConstants.SYSTEM_FINISHED_SIGNAL;

/**
 * @author Pavel Smirnov
 */

public class SystemAdapter extends AbstractSystemAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SystemAdapter.class);

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...

        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter
    }

    public SystemAdapterDockerBuilder getBuilder() throws Exception {
        SystemAdapterDockerBuilder ret = new SystemAdapterDockerBuilder();
        return ret;
    }

    @Override
    public void receiveGeneratedData(byte[] data) {
        // handle the incoming data as described in the benchmark description
        logger.debug("receiveGeneratedData("+new String(data)+"): "+new String(data));
    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
        // handle the incoming task and create a result
        String result = "result_"+taskId;
        logger.debug("receiveGeneratedTask({})->{}",taskId, new String(data));

        // Send the result to the evaluation storage
        try {
            logger.debug("sendResultToEvalStorage({})->{}", taskId, result);
            sendResultToEvalStorage(taskId, result.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {

        sendToCmdQueue(SYSTEM_FINISHED_SIGNAL);
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }

}