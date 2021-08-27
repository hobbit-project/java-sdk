package org.hobbit.sdk;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.Constants;
import org.hobbit.core.components.Component;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;
import org.hobbit.sdk.utils.ComponentsExecutor;
import org.hobbit.sdk.utils.ModelsHandler;
import org.hobbit.sdk.utils.MultiThreadedImageBuilder;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.hobbit.*;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.CommandReactionsBuilder;
import org.hobbit.vocab.HOBBIT;
import org.junit.Assert;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.io.IOException;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.Constants.*;

/**
 * @author Pavel Smirnov
 * This code is here just for testing and debugging SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyBenchmarkTestRunner {

    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private AbstractDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BenchmarkDockerBuilder benchmarkBuilder;
    DataGenDockerBuilder dataGeneratorBuilder;
    TaskGenDockerBuilder taskGeneratorBuilder;
    EvalStorageDockerBuilder evalStorageBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;
    EvalModuleDockerBuilder evalModuleBuilder;
    BuildBasedDockersBuilder customComponentBuilder;

    String systemImageName;
    String sessionId;

    public DummyBenchmarkTestRunner(String systemImageName, String sessionId){
        this.systemImageName = systemImageName;
        this.sessionId = sessionId;
    }

    public void init(Boolean useCachedImages) throws Exception {
        benchmarkBuilder = new BenchmarkDockerBuilder(new DummyDockersBuilder(DummyBenchmarkController.class, DUMMY_BENCHMARK_IMAGE_NAME).useCachedImage(useCachedImages));
        dataGeneratorBuilder = new DataGenDockerBuilder(new DummyDockersBuilder(DummyDataGenerator.class, DUMMY_DATAGEN_IMAGE_NAME).useCachedImage(useCachedImages).addFileOrFolder("data"));
        taskGeneratorBuilder = new TaskGenDockerBuilder(new DummyDockersBuilder(DummyTaskGenerator.class, DUMMY_TASKGEN_IMAGE_NAME).useCachedImage(useCachedImages));
        evalStorageBuilder = new EvalStorageDockerBuilder(new DummyDockersBuilder(DummyEvalStorage.class, DUMMY_EVAL_STORAGE_IMAGE_NAME).useCachedImage(useCachedImages));
        systemAdapterBuilder = new SystemAdapterDockerBuilder(new DummyDockersBuilder(DummySystemAdapter.class, DUMMY_SYSTEM_IMAGE_NAME).useCachedImage(useCachedImages));
        evalModuleBuilder = new EvalModuleDockerBuilder(new DummyDockersBuilder(DummyEvalModule.class, DUMMY_EVALMODULE_IMAGE_NAME).useCachedImage(useCachedImages));
        customComponentBuilder = new DummyDockersBuilder(DummyCustomComponent.class, DUMMY_CUSTOM_COMPONENT_IMAGE_NAME)
                .addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY))
                .addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY))
                .addNetworks(HOBBIT_NETWORKS)
                .addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY))
                .useCachedImage(useCachedImages);
    }


    public void buildImages() throws Exception {

        init(false);

        MultiThreadedImageBuilder builder = new MultiThreadedImageBuilder(1);
        builder.addTask(benchmarkBuilder);
        builder.addTask(dataGeneratorBuilder);
        builder.addTask(taskGeneratorBuilder);
        builder.addTask(evalStorageBuilder);
        builder.addTask(systemAdapterBuilder);
        builder.addTask(evalModuleBuilder);
        builder.addTask(customComponentBuilder);
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

        rabbitMqDockerizer = RabbitMqDockerizer.builder().useCachedContainer().build();


        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor();

        rabbitMqDockerizer.run();

        Component benchmarkController = new DummyBenchmarkController();
        Component dataGen = new DummyDataGenerator();
        Component taskGen = new DummyTaskGenerator();
        Component evalStorage  = new DummyEvalStorage();
        Component systemAdapter = new DummySystemAdapter();
        Component customComponent = new DummyCustomComponent();
        Component evalModule = new DummyEvalModule();

        if(dockerize) {
            benchmarkController = benchmarkBuilder.build();
            dataGen = dataGeneratorBuilder.build();
            taskGen = taskGeneratorBuilder.build();
            evalStorage = evalStorageBuilder.build();
            systemAdapter = systemAdapterBuilder.build();
            customComponent = customComponentBuilder.build();
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
                                                                .customContainerImage(customComponent, DUMMY_CUSTOM_COMPONENT_IMAGE_NAME)
                                                                ;

        commandQueueListener.setCommandReactions(
                commandReactionsBuilder.containerCommandsReaction(),
                commandReactionsBuilder.benchmarkSignalsReaction()
                //commandReactionsBuilder.serviceLogsReaderReaction()
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        String benchmarkContainerId = "benchmark";
        String systemContainerId = "system";

        String[] benchmarkParamsStr = new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ RabbitMQUtils.writeModel2String(ModelsHandler.createMergedParametersModel(createBenchmarkParameters(), ModelsHandler.readModelFromFile("benchmark.ttl"))) };
        String [] systemParamsStr = new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+  RabbitMQUtils.writeModel2String(ModelsHandler.createMergedParametersModel(createSystemParameters(), ModelsHandler.readModelFromFile("system.ttl"))) };

        benchmarkContainerId = commandQueueListener.createContainer(benchmarkBuilder.getImageName(), "benchmark", benchmarkParamsStr);
        systemContainerId = commandQueueListener.createContainer(systemAdapterBuilder.getImageName(), "system" , systemParamsStr);

        //componentsExecutor.submit(benchmarkController, benchmarkContainerId, new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ createBenchmarkParameters() });
        //componentsExecutor.submit(systemAdapter, systemContainerId, new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+ createSystemParameters() });

        environmentVariables.set("BENCHMARK_CONTAINER_ID", benchmarkContainerId);
        environmentVariables.set("SYSTEM_CONTAINER_ID", systemContainerId);

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    public static Model createBenchmarkParameters() throws IOException {
        Model model = createDefaultModel();
        Resource experimentResource = model.createResource(Constants.NEW_EXPERIMENT_URI);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);
        model.add(experimentResource, model.createProperty(BENCHMARK_URI+"#messages"),"100");
        return model;

    }

    public static Model createSystemParameters() throws IOException {
        Model model = createDefaultModel();
        Resource experimentResource = model.createResource(Constants.NEW_EXPERIMENT_URI);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);
        model.add(experimentResource, model.createProperty(BENCHMARK_URI+"#systemParam123"),"100");
        return model;
    }


}
