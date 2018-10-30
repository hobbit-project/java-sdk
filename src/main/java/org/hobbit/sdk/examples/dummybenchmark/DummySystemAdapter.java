package org.hobbit.sdk.examples.dummybenchmark;

import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.sdk.JenaKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummySystemAdapter extends AbstractSystemAdapter {
    private Logger logger = LoggerFactory.getLogger(DummySystemAdapter.class);;
    private static JenaKeyValue parameters;
    private String containerId;

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...

        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter
        parameters = new JenaKeyValue.Builder().buildFrom(systemParamModel);

       //containerId = createContainer("apiwise/allegrograph", new String[]{});
        containerId = createContainer("nginx", new String[]{});

        //String ret = execAsyncCommand(containerId, new String[]{"/bin/bash","/share/load.sh","/share/datasets/social_network_activity_0_0.ttl.gz","http://graph.version.0"});
//        String containerId = "6da41d318ce3";
//

//        if(!parameters.containsKey(BENCHMARK_URI+"#slaveNode")) {
//            JenaKeyValue slaveParameters = new JenaKeyValue(parameters);
//            slaveParameters.put(BENCHMARK_URI+"#slaveNode", "TRUE");
//            createContainer(DUMMY_SYSTEM_IMAGE_NAME, new String[]{ Constants.SYSTEM_PARAMETERS_MODEL_KEY+"="+ slaveParameters.encodeToString() });
//        }else
//            logger = LoggerFactory.getLogger(DummySystemAdapter.class+"_slave");

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
        logger.debug("receiveGeneratedTask({})->{}",taskId, new String(data));


        //Boolean succeed = execAsyncCommand(containerId, new String[]{"/usr/bin/date >> /share/result.log"});

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
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }

}

