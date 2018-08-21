package org.hobbit.sdk;

import org.hobbit.sdk.examples.dummybenchmark.test.DummyBenchmarkTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.examples.dummybenchmark.test.DummyDockersBuilder.*;

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
