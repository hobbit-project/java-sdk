package org.hobbit.sdk;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.examples.dummybenchmark.DummyBenchmarkController;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;

import org.junit.Assert;
import org.junit.Test;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkTest extends EnvironmentVariablesWrapper {

    private AbstractDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    Component benchmark = new DummyBenchmarkController();
    Component datagen = new DummyDataGenerator();
    Component taskgen = new DummyTaskGenerator();
    Component evalstorage = new LocalEvalStorage();
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
        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), HOBBIT_SESSION_ID);
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI);

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



}
