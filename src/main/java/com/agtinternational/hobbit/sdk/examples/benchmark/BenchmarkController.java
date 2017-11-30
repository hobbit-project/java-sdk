package com.agtinternational.hobbit.sdk.examples.benchmark;

import org.apache.jena.rdf.model.NodeIterator;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.agtinternational.hobbit.sdk.CommonConstants.RUN_LOCAL;

/**
 * @author Pavel Smirnov
 */

public class BenchmarkController extends AbstractBenchmarkController {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkController.class);
    private Boolean runLocal;

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
        String dataGeneratorImageName = "example-data-generator";
        int numberOfDataGenerators = 1;
        String[] envVariables = new String[]{"key1=value1" };

        runLocal = (System.getenv().containsKey(RUN_LOCAL)?true:false);
        if(!runLocal) {
            logger.debug("createDataGenerators()");
            createDataGenerators(dataGeneratorImageName, numberOfDataGenerators, envVariables);
        }

        // Create task generators
        String taskGeneratorImageName = "example-task-generator";
        int numberOfTaskGenerators = 1;
        envVariables = new String[]{"key1=value1" };

        if(!runLocal){
            logger.debug("createTaskGenerators()");
            createTaskGenerators(taskGeneratorImageName, numberOfTaskGenerators, envVariables);
        }

        // Create evaluation storage
        if(!runLocal) {
            logger.debug("createEvaluationStorage()");
            createEvaluationStorage();
        }

        // Wait for all components to finish their initialization
        waitForComponents();
    }

    private void waitForComponents() {
        logger.debug("waitForComponents()");
        //throw new NotImplementedException();
    }

    @Override
    protected void executeBenchmark() throws Exception {
        // give the start signals
        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
        sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);

        // wait for the data generators to finish their work
        if(!runLocal)
            waitForDataGenToFinish();
////
////        // wait for the task generators to finish their work
//        waitForTaskGenToFinish();
////
////        // wait for the system to terminate. Note that you can also use
////        // the method waitForSystemToFinish(maxTime) where maxTime is
////        // a long value defining the maximum amount of time the benchmark
////        // will wait for the system to terminate.
        //taskGenContainerIds.add("system");
        systemContainerId = "";

        waitForSystemToFinish();

        // Create the evaluation module
        String evalModuleImageName = "example-eval-module";
        String[] envVariables = new String[]{"key1=value1" };
        //createEvaluationModule(evalModuleImageName, envVariables);


        // wait for the evaluation to finish
        waitForEvalComponentsToFinish();

        // the evaluation module should have sent an RDF model containing the
        // results. We should add the configuration of the benchmark to this
        // model.
        // this.resultModel.add(...);

        // Send the resultModul to the platform controller and terminate
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
