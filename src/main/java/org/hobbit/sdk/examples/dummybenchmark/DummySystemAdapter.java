package org.hobbit.sdk.examples.dummybenchmark;

import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.sdk.JenaKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.BENCHMARK_URI;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.DUMMY_SYSTEM_IMAGE_NAME;

/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummySystemAdapter extends AbstractSystemAdapter {
    private Logger logger = LoggerFactory.getLogger(DummySystemAdapter.class);;
    private static JenaKeyValue parameters;

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...

        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter
        parameters = new JenaKeyValue.Builder().buildFrom(systemParamModel);

        if(!parameters.containsKey(BENCHMARK_URI+"#slaveNode")) {
            JenaKeyValue slaveParameters = new JenaKeyValue(parameters);
            slaveParameters.put(BENCHMARK_URI+"#slaveNode", "TRUE");
            createContainer(DUMMY_SYSTEM_IMAGE_NAME, new String[]{ Constants.SYSTEM_PARAMETERS_MODEL_KEY+"="+ slaveParameters.encodeToString() });
        }else
            logger = LoggerFactory.getLogger(DummySystemAdapter.class+"_slave");


        logger.debug("SystemModel: "+parameters.encodeToString());
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
        logger.trace("receiveGeneratedTask({})->{}",taskId, new String(data));

        // Send the result to the evaluation storage
        try {
            logger.trace("sendResultToEvalStorage({})->{}", taskId, result);
            sendResultToEvalStorage(taskId, result.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }

}

