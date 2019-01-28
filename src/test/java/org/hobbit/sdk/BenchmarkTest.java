package org.hobbit.sdk;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.util.Date;

import static org.hobbit.sdk.Constants.DUMMY_SYSTEM_IMAGE_NAME;
import static org.hobbit.sdk.Constants.*;
import static org.hobbit.sdk.DummyBenchmarkTestRunner.createBenchmarkParameters;

/**
 * @author Pavel Smirnov
 * This code here is just for testing and debugging SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class BenchmarkTest {

    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    DummyBenchmarkTestRunner sampleSystemTestRunner;

    @Before
    public void init(){
        environmentVariables.set(org.hobbit.core.Constants.RABBIT_MQ_HOST_NAME_KEY, "rabbit");
        environmentVariables.set(org.hobbit.core.Constants.HOBBIT_SESSION_ID_KEY, "session_"+String.valueOf(new Date().getTime()));
        //environmentVariables.set("DOCKER_HOST", "tcp://localhost:2376");

        sampleSystemTestRunner = new DummyBenchmarkTestRunner(DUMMY_SYSTEM_IMAGE_NAME, "session_"+String.valueOf(new Date().getTime()));
    }

    @Test
    public void buildImages() throws Exception {
        sampleSystemTestRunner.buildImages();
    }

    @Test
    public void checkHealth() throws Exception {
        sampleSystemTestRunner.checkHealth();
    }

    @Test
    public void checkHealthDockerized() throws Exception {
        sampleSystemTestRunner.checkHealthDockerized();
    }

    //Flush a queue of a locally running platform
    @Test
    @Ignore
    public void flushQueue(){
        QueueClient queueClient = new QueueClient(GIT_USERNAME);
        queueClient.flushQueue();
    }

    //Submit benchmark to a queue of a locally running platform
    @Test
    @Ignore
    public void submitToQueue() throws Exception {
        QueueClient queueClient = new QueueClient(GIT_USERNAME);
        queueClient.submitToQueue(BENCHMARK_URI, SYSTEM_URI, createBenchmarkParameters());

    }

}
