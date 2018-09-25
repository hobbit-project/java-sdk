package org.hobbit.sdk.examples.dummybenchmark.test;

import org.hobbit.core.Constants;
import org.hobbit.core.components.Component;
import org.hobbit.sdk.utils.ComponentsExecutor;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.docker.MultiThreadedImageBuilder;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.hobbit.*;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.CommandReactionsBuilder;
import org.junit.Assert;

import java.util.Date;

import static org.hobbit.core.Constants.BENCHMARK_PARAMETERS_MODEL_KEY;
import static org.hobbit.core.Constants.HOBBIT_EXPERIMENT_URI_KEY;
import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;
import static org.hobbit.sdk.CommonConstants.EXPERIMENT_URI;
import static org.hobbit.sdk.examples.dummybenchmark.test.DummyDockersBuilder.*;

/**
 * @author Pavel Smirnov
 * This code is here just for testing and debugging SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyBenchmarkTestRunner extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BenchmarkDockerBuilder benchmarkBuilder;
    DataGenDockerBuilder dataGeneratorBuilder;
    TaskGenDockerBuilder taskGeneratorBuilder;
    EvalStorageDockerBuilder evalStorageBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;
    EvalModuleDockerBuilder evalModuleBuilder;
    String systemImageName;
    String sessionId;

    public DummyBenchmarkTestRunner(String systemImageName, String sessionId){
        this.systemImageName = systemImageName;
        this.sessionId = sessionId;
    }

    public void init(Boolean useCachedImages) throws Exception {
        benchmarkBuilder = new BenchmarkDockerBuilder(new DummyDockersBuilder(DummyBenchmarkController.class, DUMMY_BENCHMARK_IMAGE_NAME).useCachedImage(useCachedImages));
        dataGeneratorBuilder = new DataGenDockerBuilder(new DummyDockersBuilder(DummyDataGenerator.class, DUMMY_DATAGEN_IMAGE_NAME).useCachedImage(useCachedImages).addFileOrFolder("data/data.dat"));
        taskGeneratorBuilder = new TaskGenDockerBuilder(new DummyDockersBuilder(DummyTaskGenerator.class, DUMMY_TASKGEN_IMAGE_NAME).useCachedImage(useCachedImages));
        evalStorageBuilder = new EvalStorageDockerBuilder(new DummyDockersBuilder(DummyEvalStorage.class, DUMMY_EVAL_STORAGE_IMAGE_NAME).useCachedImage(useCachedImages));
        systemAdapterBuilder = new SystemAdapterDockerBuilder(new DummyDockersBuilder(DummySystemAdapter.class, DUMMY_SYSTEM_IMAGE_NAME).useCachedImage(useCachedImages));
        evalModuleBuilder = new EvalModuleDockerBuilder(new DummyDockersBuilder(DummyEvalModule.class, DUMMY_EVALMODULE_IMAGE_NAME).useCachedImage(useCachedImages));
    }


    public void buildImages() throws Exception {

        init(false);


        MultiThreadedImageBuilder builder = new MultiThreadedImageBuilder(5);
        builder.addTask(benchmarkBuilder);
        builder.addTask(dataGeneratorBuilder);
        builder.addTask(taskGeneratorBuilder);
        builder.addTask(evalStorageBuilder);
        builder.addTask(systemAdapterBuilder);
        builder.addTask(evalModuleBuilder);
        builder.build();

    }

    public static void main(String[] args) throws Exception {

        DummyBenchmarkTestRunner test = new DummyBenchmarkTestRunner(args[0], args[1]);
        test.checkHealth();
    }

    public void checkHealth() throws Exception{
        checkHealth(false);
    }


    public void checkHealthDockerized() throws Exception{
        checkHealth(true);
    }

    private void checkHealth(Boolean dockerize) throws Exception {

        Boolean useCachedImages = true;
        init(useCachedImages);

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, rabbitMqDockerizer.getHostName());
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, "session_"+String.valueOf(new Date().getTime()));
        //environmentVariables.set("DOCKER_HOST", "tcp://localhost:2376");

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor();

        rabbitMqDockerizer.run();

        Component benchmarkController = new DummyBenchmarkController();
        Component dataGen = new DummyDataGenerator();
        Component taskGen = new DummyTaskGenerator();
        Component evalStorage  = new DummyEvalStorage();
        Component systemAdapter = new DummySystemAdapter();
        Component evalModule = new DummyEvalModule();

        if(dockerize) {
            benchmarkController = benchmarkBuilder.build();
            dataGen = dataGeneratorBuilder.build();
            taskGen = taskGeneratorBuilder.build();
            evalStorage = evalStorageBuilder.build();
            systemAdapter = systemAdapterBuilder.build();
            evalModule = evalModuleBuilder.build();
        }

        //comment the .systemAdapter(systemAdapter) line below to use the code for running from python
        CommandReactionsBuilder commandReactionsBuilder = new CommandReactionsBuilder(componentsExecutor, commandQueueListener)
                                                                .benchmarkController(benchmarkController).benchmarkControllerImageName(DUMMY_BENCHMARK_IMAGE_NAME)
                                                                .dataGenerator(dataGen).dataGeneratorImageName(DUMMY_DATAGEN_IMAGE_NAME)
                                                                .taskGenerator(taskGen).taskGeneratorImageName(DUMMY_TASKGEN_IMAGE_NAME)
                                                                .evalStorage(evalStorage).evalStorageImageName(DUMMY_EVAL_STORAGE_IMAGE_NAME)
                                                                .evalModule(evalModule).evalModuleImageName(DUMMY_EVALMODULE_IMAGE_NAME)
                                                                .systemAdapter(systemAdapter).systemAdapterImageName(systemImageName)
                                                                //.customContainerImage(systemAdapter, DUMMY_SYSTEM_IMAGE_NAME)
                                                                ;

        commandQueueListener.setCommandReactions(
                commandReactionsBuilder.startCommandsReaction(),
                commandReactionsBuilder.terminateCommandsReaction(),
                commandReactionsBuilder.platformCommandsReaction()
                //commandReactionsBuilder.buildServiceLogsReaderReaction()
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        String benchmarkContainerId = "benchmark";
        String systemContainerId = "system";

        benchmarkContainerId = commandQueueListener.createContainer(benchmarkBuilder.getImageName(), "benchmark", new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ createBenchmarkParameters() });
        systemContainerId = commandQueueListener.createContainer(systemAdapterBuilder.getImageName(), "system" ,new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+ createSystemParameters() });
        //componentsExecutor.submit(benchmarkController, benchmarkContainerId, new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ createBenchmarkParameters() });
        //componentsExecutor.submit(systemAdapter, systemContainerId, new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+ createSystemParameters() });

        environmentVariables.set("BENCHMARK_CONTAINER_ID", benchmarkContainerId);
        environmentVariables.set("SYSTEM_CONTAINER_ID", systemContainerId);

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        //rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }


    public String createBenchmarkParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(BENCHMARK_URI+"#messages", 100000);
        kv.setValue(BENCHMARK_URI+"benchmarkParam2", 456);
        return kv.encodeToString();
    }

    public String createSystemParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(SYSTEM_URI+"systemParam1", 123);
        //kv.setValue(SYSTEM_URI+SYSTEM_CONTAINERS_COUNT_KEY, 2);
        return kv.encodeToString();
    }


}
