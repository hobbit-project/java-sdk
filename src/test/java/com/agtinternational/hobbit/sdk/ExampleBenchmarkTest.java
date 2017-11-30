package com.agtinternational.hobbit.sdk;

import com.agtinternational.hobbit.sdk.docker.RabbitMqDockerizer;
import com.agtinternational.hobbit.sdk.examples.benchmark.BenchmarkController;
import com.agtinternational.hobbit.sdk.examples.benchmark.DataGenerator;
import com.agtinternational.hobbit.sdk.examples.benchmark.EvaluationModule;
import com.agtinternational.hobbit.sdk.examples.benchmark.TaskGenerator;
import com.agtinternational.hobbit.sdk.examples.benchmark.dockerBuilders.BenchmarkDockerBuilder;
import com.agtinternational.hobbit.sdk.examples.benchmark.dockerBuilders.DataGeneratorDockerBuilder;
import com.agtinternational.hobbit.sdk.examples.benchmark.dockerBuilders.EvalModuleDockerBuilder;
import com.agtinternational.hobbit.sdk.examples.benchmark.dockerBuilders.TaskGeneratorDockerBuilder;
import com.agtinternational.hobbit.sdk.examples.system.SystemAdapter;
import com.agtinternational.hobbit.sdk.examples.system.SystemAdapterDockerBuilder;
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

    private RabbitMqDockerizer rabbitMqDockerizer;

    @Before
    public void before() throws Exception {
        //ToDo: implement rabbit host resolve via docker DNS
        rabbitMqDockerizer = RabbitMqDockerizer.builder()
                            .hostName("172.22.0.2")
                            .useCachedContainer(true)
                            .build();
        rabbitMqDockerizer.run();


        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "");

    }

    @Test
    public void startStop() throws InterruptedException {

        String experimentId = "http://example.com/exp1";
        String systemUri = "http://agt.com/systems#sys122";

         setupBenchmarkEnvironmentVariables(experimentId);
         setupGeneratorEnvironmentVariables(1,1);
         setupSystemEnvironmentVariables(systemUri);

        ComponentsExecutor componentsExecutor = new ComponentsExecutor();
        CommandQueueListener commandQueueListener = new CommandQueueListener();

        String systemContainerId = "1234kj34k";

        commandQueueListener.setCommandReactions(
                new TerminateServicesWhenBenchmarkControllerFinished(commandQueueListener, componentsExecutor),
                new StartBenchmarkWhenSystemAndBenchmarkReady(systemContainerId));

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        componentsExecutor.submit(new LocalEvalStorage());

        Boolean dockerize = false;

        if (dockerize) {

            Boolean useCachedContainer = true;
            Boolean skipLogsReading = false;

            try {
                componentsExecutor.submit(new BenchmarkDockerBuilder().skipLogsReading(skipLogsReading).useCachedContainer(useCachedContainer).build());
                componentsExecutor.submit(new DataGeneratorDockerBuilder().skipLogsReading(skipLogsReading).useCachedContainer(useCachedContainer).build());
                componentsExecutor.submit(new TaskGeneratorDockerBuilder().skipLogsReading(skipLogsReading).useCachedContainer(useCachedContainer).build());
                componentsExecutor.submit(new SystemAdapterDockerBuilder().skipLogsReading(skipLogsReading).useCachedContainer(useCachedContainer).build());
                componentsExecutor.submit(new EvalModuleDockerBuilder().skipLogsReading(skipLogsReading).useCachedContainer(false).build());
            }
            catch (Exception e){
                Assert.fail(e.getMessage());
            }

        } else {
            componentsExecutor.submit(new BenchmarkController());
            componentsExecutor.submit(new DataGenerator());
            componentsExecutor.submit(new TaskGenerator());
            componentsExecutor.submit(new EvaluationModule());
            componentsExecutor.submit(new SystemAdapter());
        }

        commandQueueListener.waitForTermination();
        Assert.assertFalse(componentsExecutor.anyExceptions());
    }

    @After
    public void after() throws Exception {
        rabbitMqDockerizer.stop();
    }
}
