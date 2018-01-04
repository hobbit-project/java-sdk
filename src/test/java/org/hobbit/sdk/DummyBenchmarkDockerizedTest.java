package org.hobbit.sdk;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.hobbit.sdk.dummybenchmark.*;
import org.hobbit.sdk.dummybenchmark.docker.DummyDockersBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.*;
import static org.hobbit.sdk.dummybenchmark.docker.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 */

public class DummyBenchmarkDockerizedTest extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
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

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI);

        benchmarkDB = new BenchmarkDockerBuilder(new DummyDockersBuilder(DummyBenchmarkController.class).init());
        dataGeneratorDB = new DataGeneratorDockerBuilder(new DummyDockersBuilder(DummyDataGenerator.class).init());
        taskGeneratorDB = new TaskGeneratorDockerBuilder(new DummyDockersBuilder(DummyTaskGenerator.class).init());

        evalStorageDB = new EvalStorageDockerBuilder(new DummyDockersBuilder(LocalEvalStorage.class).init());

        systemAdapterDB = new SystemAdapterDockerBuilder(new DummyDockersBuilder(DummySystemAdapter.class).init());
        evalModuleDB = new EvalModuleDockerBuilder(new DummyDockersBuilder(DummyEvalModule.class).init());
    }

    @Test
    @Ignore
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

    @Test
    public void checkHealth() throws Exception {

        Boolean useCachedImages = true;
        Boolean useCachedContainer = false;

        init();

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

        rabbitMqDockerizer.run();


        Component datagen = dataGeneratorDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();
        Component taskgen = taskGeneratorDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();
        Component evalstorage = new LocalEvalStorage();
        Component evalmodule = evalModuleDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();

        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(datagen).dataGeneratorImageName(DUMMY_DATAGEN_IMAGE_NAME)
                        .taskGenerator(taskgen).taskGeneratorImageName(DUMMY_TASKGEN_IMAGE_NAME)
                        .evalStorage(evalstorage).evalStorageImageName(DUMMY_EVAL_STORAGE_IMAGE_NAME)
                        .systemContainerId(systemAdapterDB.getImageName())
                        .evalModule(evalmodule).evalModuleImageName(DUMMY_EVALMODULE_IMAGE_NAME)
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        Component benchmark = benchmarkDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();
        //Component benchmark = new DummyBenchmarkController();
        Component system = systemAdapterDB.useCachedImage(useCachedImages).useCachedContainer(useCachedContainer).build();
        //Component system = new DummySystemAdapter();

        componentsExecutor.submit(benchmark);
        componentsExecutor.submit(system, systemAdapterDB.getImageName());

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }






}
