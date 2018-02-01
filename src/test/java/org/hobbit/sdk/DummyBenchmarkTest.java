package org.hobbit.sdk;

import org.hobbit.core.Constants;
import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.core.Constants.BENCHMARK_PARAMETERS_MODEL_KEY;
import static org.hobbit.core.Constants.EXPERIMENT_URI_NS;
import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;
import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 * This code here is just for testing and debugging SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyBenchmarkTest extends EnvironmentVariablesWrapper {

    private AbstractDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    Component benchmark = new DummyBenchmarkController();
    Component datagen = new DummyDataGenerator();
    Component taskgen = new DummyTaskGenerator();
    Component evalstorage = new InMemoryEvalStorage();
    Component system = new DummySystemAdapter();
    Component evalmodule = new DummyEvalModule();


    @Test
    public void checkHealth() throws Exception {

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

        rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .build();
        rabbitMqDockerizer.run();

        String systemContainerId = "exampleSystem";
        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI, createBenchmarkParameters());
        setupSystemEnvironmentVariables(SYSTEM_URI, createSystemParameters());

        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(datagen).dataGeneratorImageName(DUMMY_DATAGEN_IMAGE_NAME)
                        .taskGenerator(taskgen).taskGeneratorImageName(DUMMY_TASKGEN_IMAGE_NAME)
                        .evalStorage(evalstorage).evalStorageImageName(DUMMY_EVAL_STORAGE_IMAGE_NAME)
                        .evalModule(evalmodule).evalModuleImageName(DUMMY_EVALMODULE_IMAGE_NAME)
                        .systemContainerId(systemContainerId)
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        componentsExecutor.submit(benchmark);
        componentsExecutor.submit(system, systemContainerId);

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        Assert.assertFalse(componentsExecutor.anyExceptions());
        rabbitMqDockerizer.stop();
    }


    public JenaKeyValue createBenchmarkParameters(){
        JenaKeyValue kv = new JenaKeyValue(EXPERIMENT_URI);
        kv.setValue(BENCHMARK_URI+"/benchmarkParam1", 123);
        kv.setValue(BENCHMARK_URI+"/benchmarkParam2", 456);
        return kv;
    }

    public JenaKeyValue createSystemParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(SYSTEM_URI+"/systemParam1", 123);
        return kv;
    }
}
