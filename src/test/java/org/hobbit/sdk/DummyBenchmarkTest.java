package org.hobbit.sdk;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.MultiThreadedImageBuilder;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.hobbit.*;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.examples.dummybenchmark.docker.*;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.EXPERIMENT_URI;
import static org.hobbit.sdk.CommonConstants.SYSTEM_CONTAINERS_COUNT_KEY;
import static org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 * This code here is just for testing and debugging SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyBenchmarkTest extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BenchmarkDockerBuilder benchmarkBuilder;
    DataGenDockerBuilder dataGeneratorBuilder;
    TaskGenDockerBuilder taskGeneratorBuilder;
    EvalStorageDockerBuilder evalStorageBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;
    EvalModuleDockerBuilder evalModuleBuilder;


    public void init(Boolean useCachedImages) throws Exception {
        benchmarkBuilder = new BenchmarkDockerBuilder(new DummyDockersBuilder(DummyBenchmarkController.class, DUMMY_BENCHMARK_IMAGE_NAME).useCachedImage(useCachedImages));
        dataGeneratorBuilder = new DataGenDockerBuilder(new DummyDockersBuilder(DummyDataGenerator.class, DUMMY_DATAGEN_IMAGE_NAME).useCachedImage(useCachedImages).addFileOrFolder("data/data.dat"));
        taskGeneratorBuilder = new TaskGenDockerBuilder(new DummyDockersBuilder(DummyTaskGenerator.class, DUMMY_TASKGEN_IMAGE_NAME).useCachedImage(useCachedImages));
        evalStorageBuilder = new EvalStorageDockerBuilder(new DummyDockersBuilder(DummyEvalStorage.class, DUMMY_EVAL_STORAGE_IMAGE_NAME).useCachedImage(useCachedImages));
        systemAdapterBuilder = new SystemAdapterDockerBuilder(new DummyDockersBuilder(DummySystemAdapter.class, DUMMY_SYSTEM_IMAGE_NAME).useCachedImage(useCachedImages));
        evalModuleBuilder = new EvalModuleDockerBuilder(new DummyDockersBuilder(DummyEvalModule.class, DUMMY_EVALMODULE_IMAGE_NAME).useCachedImage(useCachedImages));
    }

    @Test
    @Ignore
    public void buildImages() throws Exception {

        init(false);
        MultiThreadedImageBuilder builder = new MultiThreadedImageBuilder(5);
        builder.addTask(benchmarkBuilder);
        builder.addTask(dataGeneratorBuilder);
        builder.addTask(taskGeneratorBuilder);
        builder.addTask(evalStorageBuilder);
        builder.addTask(systemAdapterBuilder);
        builder.build();

    }

    @Test
    public void checkHealth() throws Exception{
        checkHealth(false);
    }

    @Test
    public void checkHealthDockerized() throws Exception{
        checkHealth(true);
    }

    private void checkHealth(Boolean dockerize) throws Exception {

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI, createBenchmarkParameters());
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI, createSystemParameters());

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor();

        rabbitMqDockerizer.run();

        Component benchmarkController = new DummyBenchmarkController();
        Component dataGen = new DummyDataGenerator();
        Component taskGen = new DummyTaskGenerator();
        Component evalStorage  = new DummyEvalStorage();
        Component evalModule = new DummyEvalModule();
        Component systemAdapter = new DummySystemAdapter();

        if(dockerize) {
            Boolean useCachedImages = true;
            init(useCachedImages);
            benchmarkController = benchmarkBuilder.build();
            dataGen = dataGeneratorBuilder.build();
            taskGen = taskGeneratorBuilder.build();
            evalStorage = evalStorageBuilder.build();
            evalModule = evalModuleBuilder.build();
            systemAdapter = systemAdapterBuilder.build();
        }

        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(dataGen).dataGeneratorImageName(DUMMY_DATAGEN_IMAGE_NAME)
                        .taskGenerator(taskGen).taskGeneratorImageName(DUMMY_TASKGEN_IMAGE_NAME)
                        .evalStorage(evalStorage).evalStorageImageName(DUMMY_EVAL_STORAGE_IMAGE_NAME)
                        .evalModule(evalModule).evalModuleImageName(DUMMY_EVALMODULE_IMAGE_NAME)
                        .systemContainerId(DUMMY_SYSTEM_IMAGE_NAME)
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        componentsExecutor.submit(benchmarkController);
        componentsExecutor.submit(systemAdapter, DUMMY_SYSTEM_IMAGE_NAME);

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
        kv.setValue(SYSTEM_URI+SYSTEM_CONTAINERS_COUNT_KEY, 2);
        return kv;
    }


}
