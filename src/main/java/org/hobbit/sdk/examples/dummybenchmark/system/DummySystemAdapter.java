package org.hobbit.sdk.examples.dummybenchmark.system;

import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.sdk.JenaKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;
import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.dummybenchmark.Constants.*;

/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummySystemAdapter extends AbstractSystemAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DummySystemAdapter.class);
    private static JenaKeyValue parameters;
    int systemContainerId = 0;
    String[] additionalContainers;
    private Semaphore terminateMutex = new Semaphore(0);

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...

        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter
        parameters = new JenaKeyValue.Builder().buildFrom(systemParamModel);

        logger.debug("SystemModel: "+parameters.encodeToString());

        if(System.getenv().containsKey(SYSTEM_CONTAINER_ID_KEY))
            systemContainerId = Integer.parseInt(System.getenv().get(SYSTEM_CONTAINER_ID_KEY));
        else if(parameters.containsKey(SYSTEM_CONTAINERS_COUNT_KEY)){
            int addCont = parameters.getIntValueFor(SYSTEM_CONTAINERS_COUNT_KEY);
            additionalContainers = new String[addCont];
            for(int i=0; i<addCont; i++)
                additionalContainers[i] = DUMMY_SYSTEM_SLAVE_IMAGE_NAME;


            startAdditionalContainers(additionalContainers, parameters);
        }

    }



    @Override
    public void receiveGeneratedData(byte[] data) {
        // handle the incoming data as described in the benchmark description
        logger.trace("receiveGeneratedData("+new String(data)+"): "+new String(data));
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

    private List<String> startAdditionalContainers(String[] imageNames, JenaKeyValue systemParamsModel){
        logger.debug("Start additional containers");
        List<String> containerIds = new ArrayList();

        //String[] variables = envVariables != null ? (String[]) Arrays.copyOf(envVariables, envVariables.length + 1) : new String[2];

        for(int i = 0; i < imageNames.length; ++i) {
            String[] variables = new String[]{
                    SYSTEM_PARAMETERS_MODEL_KEY+"="+systemParamsModel.encodeToString(),
                    SYSTEM_CONTAINER_ID_KEY+"="+ (i+1)
            };

            String containerId = this.createContainer(imageNames[i], variables);
            if (containerId == null) {
                String errorMsg = "Couldn't create generator component. Aborting.";
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            containerIds.add(containerId);
        }
        logger.debug("Additional containers started");
        return containerIds;
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }

}

