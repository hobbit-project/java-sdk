package org.hobbit.sdk.examples.dummybenchmark.docker;

import org.hobbit.sdk.docker.builders.DynamicDockerFileBuilder;


/**
 * @author Pavel Smirnov
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

//Common dockerizers builder for all components of your project
public class DummyDockersBuilder extends DynamicDockerFileBuilder {

    //public static final String GIT_REPO_PATH = "git.project-hobbit.eu:4567/smirnp/";
    public static final String GIT_REPO_PATH = "";
    public static final String PROJECT_NAME = "dummybenchmark/";

    public static final String DUMMY_BENCHMARK_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"benchmark-controller";
    public static final String DUMMY_SYSTEM_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"system-adapter";

    //use these constants within BenchmarkController
    public static final String DUMMY_DATAGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"datagen";
    public static final String DUMMY_TASKGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"taskgen";
    public static final String DUMMY_EVAL_STORAGE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"eval-storage";
    public static final String DUMMY_EVALMODULE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"eval-module";

    public static final String BENCHMARK_URI = "http://project-hobbit.eu/"+PROJECT_NAME;
    public static final String SYSTEM_URI = "http://project-hobbit.eu/"+PROJECT_NAME+"system";

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
