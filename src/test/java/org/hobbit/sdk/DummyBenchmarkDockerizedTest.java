package org.hobbit.sdk;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.EXPERIMENT_URI;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 * This code here is just for testing and debugging SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyBenchmarkDockerizedTest extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BenchmarkDockerBuilder benchmarkBuilder;
    DataGenDockerBuilder dataGeneratorBuilder;
    TaskGenDockerBuilder taskGeneratorBuilder;
    EvalStorageDockerBuilder evalStorageBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;
    EvalModuleDockerBuilder evalModuleBuilder;

    Component benchmarkController;
    Component dataGen;
    Component taskGen;
    Component evalStorage;
    Component evalModule;
    Component systemAdapter;

    public void init(Boolean useCachedImages) throws Exception {

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI, createBenchmarkParameters());
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI, createSystemParameters());

        benchmarkBuilder = new BenchmarkDockerBuilder(new DummyDockersBuilder(DummyBenchmarkController.class, DUMMY_BENCHMARK_IMAGE_NAME).useCachedImage(useCachedImages).init());
        dataGeneratorBuilder = new DataGenDockerBuilder(new DummyDockersBuilder(DummyDataGenerator.class, DUMMY_DATAGEN_IMAGE_NAME).useCachedImage(useCachedImages).addFileOrFolder("data").init());
        taskGeneratorBuilder = new TaskGenDockerBuilder(new DummyDockersBuilder(DummyTaskGenerator.class, DUMMY_TASKGEN_IMAGE_NAME).useCachedImage(useCachedImages).init());

        evalStorageBuilder = new EvalStorageDockerBuilder(new DummyDockersBuilder(InMemoryEvalStorage.class, DUMMY_EVAL_STORAGE_IMAGE_NAME).useCachedImage(useCachedImages).init());

        systemAdapterBuilder = new SystemAdapterDockerBuilder(new DummyDockersBuilder(DummySystemAdapter.class, DUMMY_SYSTEM_IMAGE_NAME).useCachedImage(useCachedImages).init());
        evalModuleBuilder = new EvalModuleDockerBuilder(new DummyDockersBuilder(DummyEvalModule.class, DUMMY_EVALMODULE_IMAGE_NAME).useCachedImage(useCachedImages).init());

        benchmarkController = benchmarkBuilder.build();
        dataGen = new DummyDataGenerator();
        //dataGen = dataGeneratorBuilder.build();
        taskGen = taskGeneratorBuilder.build();
        evalStorage = evalStorageBuilder.build();
        evalModule = evalModuleBuilder.build();
        systemAdapter = systemAdapterBuilder.build();
    }

    @Test
    @Ignore
    public void buildImages() throws Exception {

        init(false);

        ((AbstractDockerizer)benchmarkController).prepareImage();
        ((AbstractDockerizer)dataGen).prepareImage();
        ((AbstractDockerizer)taskGen).prepareImage();
        ((AbstractDockerizer)evalStorage).prepareImage();
        ((AbstractDockerizer)systemAdapter).prepareImage();
        ((AbstractDockerizer)evalModule).prepareImage();
    }

    @Test
    public void checkHealth() throws Exception {

        Boolean useCachedImages = true;

        init(useCachedImages);

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

        rabbitMqDockerizer.run();


        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(dataGen).dataGeneratorImageName(dataGeneratorBuilder.getImageName())
                        .taskGenerator(taskGen).taskGeneratorImageName(taskGeneratorBuilder.getImageName())
                        .evalStorage(evalStorage).evalStorageImageName(evalStorageBuilder.getImageName())
                        .evalModule(evalModule).evalModuleImageName(evalModuleBuilder.getImageName())
                        .systemContainerId(systemAdapterBuilder.getImageName())
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        //you can run clear java-code instead of dockerized one

        //benchmarkController = new DummyBenchmarkController();
        //systemAdapter = new DummySystemAdapter();

        componentsExecutor.submit(benchmarkController);
        componentsExecutor.submit(systemAdapter, systemAdapterBuilder.getImageName());

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }


    public JenaKeyValue createBenchmarkParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(BENCHMARK_URI+"benchmarkParam1", 123);
        kv.setValue(BENCHMARK_URI+"benchmarkParam2", 456);
        return kv;
    }

    public JenaKeyValue createSystemParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(SYSTEM_URI+"systemParam1", 123);
        return kv;
    }


}
