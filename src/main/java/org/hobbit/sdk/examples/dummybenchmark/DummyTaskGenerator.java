package org.hobbit.sdk.examples.dummybenchmark;

import org.hobbit.core.components.AbstractTaskGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hobbit.sdk.Constants.*;

/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyTaskGenerator extends AbstractTaskGenerator {
    //private static final Logger logger = LoggerFactory.getLogger(DummyTaskGenerator.class);
    private Logger logger;

    @Override
    public void init() throws Exception {
        // Always initFileReader the super class first!
        super.init();

        logger = LoggerFactory.getLogger(this.getClass().getName()+"_"+getGeneratorId());
        logger.debug("Init finished");

//        if(System.getenv().containsKey(EXPERIMENT_URI+"/benchmarkParam1")){
//            String param1 = System.getenv().get(EXPERIMENT_URI+"/benchmarkParam1");
//        }

        // Your initialization code comes here...
    }

    @Override
    protected void generateTask(byte[] data) throws Exception {
        String dataString = new String(data);
        logger.trace("generateTask()->{}",dataString);
        // Create tasks based on the incoming data inside this method.
        // You might want to use the id of this task generator and the
        // number of all task generators running in parallel.
        //logger.debug("generateTask()");
        int dataGeneratorId = getGeneratorId();
        int numberOfGenerators = getNumberOfGenerators();

        // Create an ID for the task
        String taskId = getNextTaskId();

        // Create the task and the expected answer
        String taskDataStr = "task_"+taskId+"_"+dataString;
        String expectedAnswerDataStr = "result_"+taskId;

        // Send the task to the system (and store the timestamp)
        long timestamp = System.currentTimeMillis();

        logger.trace("sendTaskToSystemAdapter({})->{}",taskId, taskDataStr);
        sendTaskToSystemAdapter(taskId, taskDataStr.getBytes());

        // Send the expected answer to the evaluation store
        logger.trace("sendTaskToEvalStorage({})->{}", taskId, expectedAnswerDataStr);
        sendTaskToEvalStorage(taskId, timestamp, expectedAnswerDataStr.getBytes());
    }

    @Override
    public void close() throws IOException {
        logger.debug("close()");
        // Always close the super class after yours!
        super.close();
    }

}