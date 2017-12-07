package com.agtinternational.hobbit.sdk.examples.dummybenchmark.docker;

import com.agtinternational.hobbit.sdk.CommonConstants;
import com.agtinternational.hobbit.sdk.docker.BuildBasedDockerizer;
import com.agtinternational.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;


/**
 * @author Pavel Smirnov
 */

public class DummyDockersBuilder extends DynamicDockerFileBuilder{

    public DummyDockersBuilder(Class value){
        super("DummyDockersBuilder");
        repoPath("git.project-hobbit.eu:4567/smirnp");
        jarFilePath("hobbit-java-sdk-1.0.jar");
        buildDirectory("target");
        dockerWorkDir("/usr/src/dummy-benchmark");
        runnerClass(org.hobbit.core.run.ComponentStarter.class, value);
    }

    @Override
    public BuildBasedDockerizer build() throws Exception{
        throw new Exception("Direct build is prohibited!");
    };

}
