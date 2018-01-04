package org.hobbit.sdk.dummybenchmark.docker;

import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;


/**
 * @author Pavel Smirnov
 */

//Common dockerizers builder for all components of your project
public class DummyDockersBuilder extends DynamicDockerFileBuilder {

    //public static String GIT_REPO_PATH = "git.project-hobbit.eu:4567/smirnp/";
    public static String GIT_REPO_PATH = "";
    public static String PROJECT_NAME = "dummy-benchmark";

    //use these constants within BenchmarkController
    public static final String DUMMY_DATAGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"-datagen";
    public static final String DUMMY_TASKGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"-taskgen";
    public static final String DUMMY_EVAL_STORAGE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"-eval-storage";
    public static final String DUMMY_EVALMODULE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"-eval-module";

    public DummyDockersBuilder(Class runnerClass){
        super("DummyDockersBuilder");

        imageNamePrefix(GIT_REPO_PATH+PROJECT_NAME);
        buildDirectory("target");
        jarFileName("hobbit-java-sdk-1.0.jar");
        dockerWorkDir("/usr/src/"+PROJECT_NAME);
        containerName(runnerClass.getSimpleName());
        runnerClass(org.hobbit.core.run.ComponentStarter.class, runnerClass);
    }

    @Override
    public BuildBasedDockerizer build() throws Exception{
        throw new Exception("Direct build is prohibited! Allowed to be used only as argument for other dockerBuilders");
    };

}
