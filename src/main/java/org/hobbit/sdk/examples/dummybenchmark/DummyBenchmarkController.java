package org.hobbit.sdk.examples.dummybenchmark;

import org.apache.commons.lang3.ArrayUtils;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.hobbit.sdk.JenaKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.*;


/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyBenchmarkController extends AbstractBenchmarkController {
    private static final Logger logger = LoggerFactory.getLogger(DummyBenchmarkController.class);
    private static JenaKeyValue parameters;

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");

        // Your initialization code comes here...

        parameters = new JenaKeyValue.Builder().buildFrom(benchmarkParamModel);
        logger.debug("BenchmarkModel: "+parameters.encodeToString());
        // Create the other components

        // Create data generators
        logger.debug("createDataGenerators()");

        int numberOfDataGenerators = 1;
        String [] envVariables = parameters.mapToArray();
        createDataGenerators(DUMMY_DATAGEN_IMAGE_NAME, numberOfDataGenerators, envVariables);

        logger.debug("createTaskGenerators()");
        int numberOfTaskGenerators = 1;

        createTaskGenerators(DUMMY_TASKGEN_IMAGE_NAME, numberOfTaskGenerators, envVariables);

        // Create evaluation storage
        logger.debug("createEvaluationStorage()");
        envVariables = (String[])ArrayUtils.add(DEFAULT_EVAL_STORAGE_PARAMETERS, "HOBBIT_RABBIT_HOST=" + this.rabbitMQHostName);
        this.createEvaluationStorage(DUMMY_EVAL_STORAGE_IMAGE_NAME, envVariables);

        // Wait for all components to finish their initialization
        waitForComponentsInit();
    }

    private void waitForComponentsInit() {
        logger.debug("waitForComponentsInit()");
        //throw new NotImplementedException();
    }

    @Override
    protected void executeBenchmark() throws Exception {
        logger.debug("executeBenchmark(sending TASK_GENERATOR_START_SIGNAL & DATA_GENERATOR_START_SIGNAL)");
        // give the start signals
        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
        sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);

        // wait for the data generators to finish their work

        logger.debug("waitForDataGenToFinish() to send DATA_GENERATION_FINISHED_SIGNAL");
        waitForDataGenToFinish();
////
////        // wait for the task generators to finish their work

        logger.debug("waitForTaskGenToFinish() to finish to send TASK_GENERATION_FINISHED_SIGNAL");
        waitForTaskGenToFinish();

////
////        // wait for the system to terminate. Note that you can also use
////        // the method waitForSystemToFinish(maxTime) where maxTime is
////        // a long value defining the maximum amount of time the benchmark
////        // will wait for the system to terminate.

        logger.debug("waitForSystemToFinish() to finish to send TASK_GENERATION_FINISHED_SIGNAL");
        waitForSystemToFinish();

        // Create the evaluation module

        String [] envVariables = parameters.mapToArray();
        logger.debug("createEvaluationModule()");
        createEvaluationModule(DUMMY_EVALMODULE_IMAGE_NAME, envVariables);

        // wait for the evaluation to finish
        logger.debug("Waiting for the evaluation module to finish.");
        waitForEvalComponentsToFinish();

        // the evaluation module should have sent an RDF model containing the
        // results. We should add the configuration of the benchmark to this
        // model.
        // this.resultModel.add(...);

        // Send the resultModul to the platform controller and terminate
        logger.debug("sendResultModel()");
        sendResultModel(resultModel);
    }

    @Override
    public void close() throws IOException {
        logger.debug("close()");
        // Free the resources you requested here

        // Always close the super class after yours!
        super.close();
    }

}
