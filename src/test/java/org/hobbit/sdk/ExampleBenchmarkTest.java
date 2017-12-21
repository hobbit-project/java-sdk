package org.hobbit.sdk;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.examples.dummybenchmark.BenchmarkController;
import org.hobbit.sdk.examples.dummybenchmark.DataGenerator;
import org.hobbit.sdk.examples.dummybenchmark.EvaluationModule;
import org.hobbit.sdk.examples.dummybenchmark.TaskGenerator;
import org.hobbit.sdk.examples.dummybenchmark.SystemAdapter;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;

import org.junit.Assert;
import org.junit.Test;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkTest extends EnvironmentVariables{

    private AbstractDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    Component benchmark = new BenchmarkController();
    Component datagen = new DataGenerator();
    Component taskgen = new TaskGenerator();
    Component evalstorage = new LocalEvalStorage();
    Component system = new SystemAdapter();
    Component evalmodule = new EvaluationModule();


    @Test
    public void checkHealth() throws Exception {

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener);

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
                        .dataGenerator(datagen).dataGeneratorImageName(DATAGEN_IMAGE_NAME)
                        .taskGenerator(taskgen).taskGeneratorImageName(TASKGEN_IMAGE_NAME)
                        .evalStorage(evalstorage).evalStorageImageName(EVAL_STORAGE_IMAGE_NAME)
                        .evalModule(evalmodule).evalModuleImageName(EVALMODULE_IMAGE_NAME)
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
