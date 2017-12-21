package org.hobbit.sdk.examples.dummybenchmark;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.NodeIterator;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.LocalEvalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.DATAGEN_IMAGE_NAME;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.EVALMODULE_IMAGE_NAME;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.TASKGEN_IMAGE_NAME;

/**
 * @author Pavel Smirnov
 */

public class BenchmarkController extends AbstractBenchmarkController {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkController.class);

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");

        // Your initialization code comes here...

        // You might want to load parameters from the benchmarks parameter model
        NodeIterator iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
                .getProperty("http://example.org/myParameter"));

        // Create the other components

        // Create data generators
        logger.debug("createDataGenerators()");

        int numberOfDataGenerators = 1;
        String[] envVariables = new String[]{"key1=value1" };
        createDataGenerators(DATAGEN_IMAGE_NAME, numberOfDataGenerators, envVariables);

        logger.debug("createTaskGenerators()");
        int numberOfTaskGenerators = 1;
        envVariables = new String[]{"key1=value1" };
        createTaskGenerators(TASKGEN_IMAGE_NAME, numberOfTaskGenerators, envVariables);

        // Create evaluation storage
        logger.debug("createEvaluationStorage()");
        createEvaluationStorage();

        // Wait for all components to finish their initialization
        waitForComponentsInit();
    }

    private void waitForComponentsInit() {
        logger.debug("waitForComponentsInit()");
        //throw new NotImplementedException();
    }

//    @Override
//    protected void createEvaluationStorage(){
//        String[] envVariables = (String[]) ArrayUtils.add(DEFAULT_EVAL_STORAGE_PARAMETERS, "HOBBIT_RABBIT_HOST=" + this.rabbitMQHostName);
//        this.createEvaluationStorage("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.5", envVariables);
//    }

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

        String[] envVariables = new String[]{"key1=value1"};
        logger.debug("createEvaluationModule()");
        createEvaluationModule(EVALMODULE_IMAGE_NAME, envVariables);

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
