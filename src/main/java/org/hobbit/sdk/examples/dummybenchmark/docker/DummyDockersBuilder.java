package org.hobbit.sdk.examples.dummybenchmark.docker;

import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.Component;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;
import org.hobbit.sdk.utils.CommandSender;

import java.util.concurrent.Callable;


/**
 * @author Pavel Smirnov
 */

public class DummyDockersBuilder extends DynamicDockerFileBuilder {

    public static final String DATAGEN_IMAGE_NAME = "example-datagen";
    public static final String TASKGEN_IMAGE_NAME = "example-taskgen";
    public static final String EVAL_STORAGE_IMAGE_NAME = "git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.5";
    public static final String EVALMODULE_IMAGE_NAME = "example-eval-module";

    public DummyDockersBuilder(Class className){
        super("DummyDockersBuilder");
        imageNamePrefix("smirnp/dummybenchmark-");
        jarFilePath("hobbit-java-sdk-1.0.jar");
        buildDirectory("target");
        dockerWorkDir("/usr/src/dummy-benchmark");
        runnerClass(org.hobbit.core.run.ComponentStarter.class, className);

        String containerName = className.getSimpleName();
        containerName(containerName);

    }

    @Override
    public BuildBasedDockerizer build() throws Exception{
        throw new Exception("Direct build is prohibited!");
    };

}
