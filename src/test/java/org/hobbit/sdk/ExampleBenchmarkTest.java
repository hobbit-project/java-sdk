package org.hobbit.sdk;

import org.apache.jena.atlas.lib.Callback;
import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder;
import org.hobbit.sdk.examples.dummybenchmark.BenchmarkController;
import org.hobbit.sdk.examples.dummybenchmark.DataGenerator;
import org.hobbit.sdk.examples.dummybenchmark.EvaluationModule;
import org.hobbit.sdk.examples.dummybenchmark.TaskGenerator;
import org.hobbit.sdk.docker.builders.BenchmarkDockerBuilder;
import org.hobbit.sdk.docker.builders.DataGeneratorDockerBuilder;
import org.hobbit.sdk.docker.builders.EvalModuleDockerBuilder;
import org.hobbit.sdk.docker.builders.TaskGeneratorDockerBuilder;
import org.hobbit.sdk.examples.dummybenchmark.SystemAdapter;
import org.hobbit.sdk.docker.builders.SystemAdapterDockerBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.StartBenchmarkWhenComponentsAreReady;
import org.hobbit.sdk.utils.commandreactions.StartStopContainersReaction;
import org.hobbit.sdk.utils.commandreactions.TerminateServicesReaction;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;

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

    public void before(){
        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener);

        rabbitMqDockerizer = RabbitMqDockerizer.builder()
                .build();
        rabbitMqDockerizer.run();
    }

    public void exec(Boolean dockerize) throws Exception {

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), HOBBIT_SESSION_ID);
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI);
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI);

        String dataGenContainerId = datagen.getClass().getSimpleName();
        String taskGenContainerId = taskgen.getClass().getSimpleName();
        String evalStorageContainerId = evalmodule.getClass().getSimpleName();
        String systemContainerId = system.getClass().getSimpleName();
        String evalModuleContainerId = evalmodule.getClass().getSimpleName();

        if(dockerize){
            Boolean useCachedImages = true;

            benchmark = new BenchmarkDockerBuilder(new DummyDockersBuilder(benchmark.getClass()).init()).useCachedImage(useCachedImages).build();
            datagen = new DataGeneratorDockerBuilder(new DummyDockersBuilder(datagen.getClass()).init()).useCachedImage(useCachedImages).build();
            taskgen = new TaskGeneratorDockerBuilder(new DummyDockersBuilder(taskgen.getClass()).init()).useCachedImage(useCachedImages).build();
            system = new SystemAdapterDockerBuilder(new DummyDockersBuilder(system.getClass()).init()).useCachedImage(useCachedImages).build();
            evalmodule = new EvalModuleDockerBuilder(new DummyDockersBuilder(evalmodule.getClass()).init()).useCachedImage(useCachedImages).build();

            dataGenContainerId = ((AbstractDockerizer)datagen).getContainerName();
            taskGenContainerId = ((AbstractDockerizer)taskgen).getContainerName();

            systemContainerId = ((AbstractDockerizer)system).getContainerName();
            evalModuleContainerId = ((AbstractDockerizer)evalmodule).getContainerName();
        }


        commandQueueListener.setCommandReactions(
                new StartBenchmarkWhenComponentsAreReady(systemContainerId),
                new StartStopContainersReaction(
                        DATAGEN_IMAGE_NAME,
                        TASKGEN_IMAGE_NAME,
                        EVAL_STORAGE_IMAGE_NAME,
                        EVALMODULE_IMAGE_NAME,
                        dataGenContainerId,
                        taskGenContainerId,
                        evalStorageContainerId,
                        systemContainerId,
                        evalModuleContainerId,
                        datagen,
                        taskgen,
                        evalstorage,
                        system,
                        evalmodule,
                        componentsExecutor,
                        commandQueueListener),

                new TerminateServicesReaction(dataGenContainerId,
                                              taskGenContainerId,
                                              evalStorageContainerId,
                                              systemContainerId,
                                              evalModuleContainerId, commandQueueListener, componentsExecutor)
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        componentsExecutor.submit(benchmark);
        componentsExecutor.submit(system);

        commandQueueListener.waitForTermination();
    }

    @Test
    public void buildImages() throws Exception {

        AbstractDockerizer dockerizer = new BenchmarkDockerBuilder(new DummyDockersBuilder(BenchmarkController.class).init()).build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = new DataGeneratorDockerBuilder(new DummyDockersBuilder(DataGenerator.class).init()).build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = new TaskGeneratorDockerBuilder(new DummyDockersBuilder(TaskGenerator.class).init()).build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = new SystemAdapterDockerBuilder(new DummyDockersBuilder(SystemAdapter.class).init()).build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());

        dockerizer = new EvalModuleDockerBuilder(new DummyDockersBuilder(EvaluationModule.class).init()).build();
        dockerizer.prepareImage();
        Assert.assertNull(dockerizer.anyExceptions());
    }

    @Test
    public void checkHealth() throws Exception {
        before();
        exec(false);
        Assert.assertFalse(componentsExecutor.anyExceptions());
        finish();
    }

    @Test
    public void checkHealthDockerized() throws Exception {
        before();
        exec(true);
        Assert.assertFalse(componentsExecutor.anyExceptions());
        finish();
    }


    public void finish() throws Exception {
        rabbitMqDockerizer.stop();
    }
}
