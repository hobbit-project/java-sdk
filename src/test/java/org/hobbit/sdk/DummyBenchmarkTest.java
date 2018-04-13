package org.hobbit.sdk;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.MultiThreadedImageBuilder;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.hobbit.*;
import org.hobbit.sdk.examples.dummybenchmark.*;
import org.hobbit.sdk.examples.dummybenchmark.docker.*;
import org.hobbit.sdk.examples.dummybenchmark.test.DummyBenchmarkTestRunner;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Before;
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

public class DummyBenchmarkTest extends EnvironmentVariablesWrapper {

    DummyBenchmarkTestRunner sampleSystemTestRunner;

    @Before
    public void init(){
        sampleSystemTestRunner = new DummyBenchmarkTestRunner(DUMMY_SYSTEM_IMAGE_NAME, "session_"+String.valueOf(new Date().getTime()));
    }

    @Test
    @Ignore
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



}
