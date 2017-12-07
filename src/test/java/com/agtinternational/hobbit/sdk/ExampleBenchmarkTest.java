package com.agtinternational.hobbit.sdk;

import com.agtinternational.hobbit.sdk.docker.AbstractDockerizer;
import com.agtinternational.hobbit.sdk.docker.RabbitMqDockerizer;
import com.agtinternational.hobbit.sdk.examples.dummybenchmark.docker.DummyDockersBuilder;
import com.agtinternational.hobbit.sdk.examples.dummybenchmark.BenchmarkController;
import com.agtinternational.hobbit.sdk.examples.dummybenchmark.DataGenerator;
import com.agtinternational.hobbit.sdk.examples.dummybenchmark.EvaluationModule;
import com.agtinternational.hobbit.sdk.examples.dummybenchmark.TaskGenerator;
import com.agtinternational.hobbit.sdk.docker.builders.BenchmarkDockerBuilder;
import com.agtinternational.hobbit.sdk.docker.builders.DataGeneratorDockerBuilder;
import com.agtinternational.hobbit.sdk.docker.builders.EvalModuleDockerBuilder;
import com.agtinternational.hobbit.sdk.docker.builders.TaskGeneratorDockerBuilder;
import com.agtinternational.hobbit.sdk.examples.dummybenchmark.SystemAdapter;
import com.agtinternational.hobbit.sdk.docker.builders.SystemAdapterDockerBuilder;
import com.agtinternational.hobbit.sdk.utils.CommandQueueListener;
import com.agtinternational.hobbit.sdk.utils.commandreactions.StartBenchmarkWhenSystemAndBenchmarkReady;
import com.agtinternational.hobbit.sdk.utils.commandreactions.TerminateServicesWhenBenchmarkControllerFinished;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkTest extends EnvironmentVariables{

    private AbstractDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    @Before
    public void before() throws Exception {

        componentsExecutor = new ComponentsExecutor();
        commandQueueListener = new CommandQueueListener();

        rabbitMqDockerizer = RabbitMqDockerizer.builder()
                            .build();
        rabbitMqDockerizer.run();

        String experimentId = "http://example.com/exp1";
        String systemUri = "http://agt.com/systems#sys122";

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session1");
        setupBenchmarkEnvironmentVariables(experimentId);
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(systemUri);

        String systemContainerId = "1234kj34k";

        commandQueueListener.setCommandReactions(
                new TerminateServicesWhenBenchmarkControllerFinished(commandQueueListener, componentsExecutor),
                new StartBenchmarkWhenSystemAndBenchmarkReady(systemContainerId));

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        componentsExecutor.submit(new LocalEvalStorage());

    }

    @Test
    public void checkSources() throws InterruptedException {

        componentsExecutor.submit(new BenchmarkController());
        componentsExecutor.submit(new DataGenerator());
        componentsExecutor.submit(new TaskGenerator());
        componentsExecutor.submit(new SystemAdapter());
        componentsExecutor.submit(new EvaluationModule());

        commandQueueListener.waitForTermination();
        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    @Test
    public void checkDockerized() throws InterruptedException {

        Boolean useCachedImage = false;
        Boolean useCachedContainer = false;
        Boolean skipLogsReading = false;

        try {
            componentsExecutor.submit(new BenchmarkDockerBuilder(new DummyDockersBuilder(BenchmarkController.class).init()).useCachedImage(useCachedImage).useCachedContainer(useCachedContainer).skipLogsReading(skipLogsReading).build());
            componentsExecutor.submit(new DataGeneratorDockerBuilder(new DummyDockersBuilder(DataGenerator.class).init()).useCachedImage(useCachedImage).useCachedContainer(useCachedContainer).skipLogsReading(skipLogsReading).build());
            componentsExecutor.submit(new TaskGeneratorDockerBuilder(new DummyDockersBuilder(TaskGenerator.class).init()).useCachedImage(useCachedImage).useCachedContainer(useCachedContainer).skipLogsReading(skipLogsReading).build());
            componentsExecutor.submit(new SystemAdapterDockerBuilder(new DummyDockersBuilder(SystemAdapter.class).init()).useCachedImage(useCachedImage).useCachedContainer(useCachedContainer).skipLogsReading(skipLogsReading).build());
            componentsExecutor.submit(new EvalModuleDockerBuilder(new DummyDockersBuilder(EvaluationModule.class).init()).useCachedImage(useCachedImage).useCachedContainer(false).skipLogsReading(skipLogsReading).build());
        }
        catch (Exception e){
            Assert.fail(e.getMessage());
        }

        commandQueueListener.waitForTermination();
        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    @After
    public void after() throws Exception {
        rabbitMqDockerizer.stop();
    }
}
