package org.hobbit.sdk.examples.dummybenchmark.docker;

import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;


/**
 * @author Pavel Smirnov
 */

public class DummyDockersBuilder extends DynamicDockerFileBuilder {

    public static final String DUMMY_DATAGEN_IMAGE_NAME = "example-datagen";
    public static final String DUMMY_TASKGEN_IMAGE_NAME = "example-taskgen";
    public static final String DUMMY_EVAL_STORAGE_IMAGE_NAME = "example-eval-storage";
    public static final String DUMMY_EVALMODULE_IMAGE_NAME = "example-eval-module";

    public DummyDockersBuilder(Class runnerClass){
        super("DummyDockersBuilder");
        imageNamePrefix("smirnp/dummy-");
        jarFileName("hobbit-java-sdk-1.0.jar");
        buildDirectory("target");
        dockerWorkDir("/usr/src/dummy-benchmark");
        containerName(runnerClass.getSimpleName());
        runnerClass(org.hobbit.core.run.ComponentStarter.class, runnerClass);


    }

    @Override
    public BuildBasedDockerizer build() throws Exception{
        throw new Exception("Direct build is prohibited!");
    };

}
