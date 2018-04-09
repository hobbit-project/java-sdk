package org.hobbit.sdk.examples.dummybenchmark.docker;

import org.hobbit.sdk.docker.builders.DynamicDockerFileBuilder;

import static org.hobbit.sdk.examples.dummybenchmark.Constants.PROJECT_NAME;


/**
 * @author Pavel Smirnov
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

//Common dockerizers builder for all components of your project
public class DummyDockersBuilder extends DynamicDockerFileBuilder {

    public DummyDockersBuilder(Class runnerClass, String imageName) {
        super("DummyDockersBuilder");
        imageName(imageName);
        buildDirectory(".");
        jarFilePath("target/hobbit-java-sdk-1.1.1.jar");
        dockerWorkDir("/usr/src/"+PROJECT_NAME);
        containerName(runnerClass.getSimpleName());
        runnerClass(org.hobbit.core.run.ComponentStarter.class, runnerClass);
    }


}
