package org.hobbit.sdk;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.docker.builders.common.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Test;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkDockerizedTest extends EnvironmentVariables{

    private AbstractDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BuildBasedDockersBuilder benchmarkDB;
    BuildBasedDockersBuilder dataGeneratorDB;
    BuildBasedDockersBuilder taskGeneratorDB;
    BuildBasedDockersBuilder evalStorageDB;
    BuildBasedDockersBuilder systemAdapterDB;
    BuildBasedDockersBuilder evalModuleDB;

    public void init() throws Exception {

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), HOBBIT_SESSION_ID);
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI);

        benchmarkDB = new BenchmarkDockerBuilder(new DummyDockersBuilder(BenchmarkController.class).init());
        dataGeneratorDB = new DataGeneratorDockerBuilder(new DummyDockersBuilder(DataGenerator.class).init());
        taskGeneratorDB = new TaskGeneratorDockerBuilder(new DummyDockersBuilder(TaskGenerator.class).init());

        evalStorageDB = new EvalStorageDockerBuilder(new DummyDockersBuilder(LocalEvalStorage.class).init());

        systemAdapterDB = new SystemAdapterDockerBuilder(new DummyDockersBuilder(SystemAdapter.class).init());
        evalModuleDB = new EvalModuleDockerBuilder(new DummyDockersBuilder(EvaluationModule.class).init());
    }

    @Test
    public void buildImages() throws Exception {

        init();

        AbstractDockerizer dockerizer = benchmarkDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = dataGeneratorDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = taskGeneratorDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = systemAdapterDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = evalModuleDB.build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());
    }

    public void exec(Boolean useCachedImages, Boolean useCachedContainer) throws Exception {

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener);

        rabbitMqDockerizer.run();

        Component datagen = dataGeneratorDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();
        Component taskgen = taskGeneratorDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();
        Component evalstorage = new LocalEvalStorage();
        Component evalmodule = evalModuleDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();

        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(datagen).dataGeneratorImageName(DATAGEN_IMAGE_NAME)
                        .taskGenerator(taskgen).taskGeneratorImageName(TASKGEN_IMAGE_NAME)
                        .evalStorage(evalstorage).evalStorageImageName(EVAL_STORAGE_IMAGE_NAME)
                        .systemContainerId(systemAdapterDB.getImageName())
                        .evalModule(evalmodule).evalModuleImageName(EVALMODULE_IMAGE_NAME)
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        Component benchmark = benchmarkDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();
        //Component benchmark = new BenchmarkController();
        Component system = systemAdapterDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();
        //Component system = new SystemAdapter();

        componentsExecutor.submit(benchmark);
        componentsExecutor.submit(system, systemAdapterDB.getImageName());

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        rabbitMqDockerizer.stop();
    }


    @Test
    public void checkHealth() throws Exception {

        Boolean useCachedImages = true;
        Boolean useCachedContainers = false;

        init();
        exec(useCachedImages, useCachedContainers);
        Assert.assertFalse(componentsExecutor.anyExceptions());
    }



}
