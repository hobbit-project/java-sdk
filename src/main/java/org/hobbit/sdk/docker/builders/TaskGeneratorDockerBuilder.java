package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.CommonConstants;
import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.CommonConstants.HOBBIT_NETWORKS;


/**
 * @author Pavel Smirnov
 */

public class TaskGeneratorDockerBuilder extends BuildBasedDockersBuilder {
    public TaskGeneratorDockerBuilder(DynamicDockerFileBuilder builder){

        super("ExampleTaskGenDockerizer");

        imageName("task-generator");
        containerName(builder.getContainerName());
        buildDirectory(builder.getBuildDirectory());
        dockerFileReader(builder.getDockerFileReader());
        onTermination(builder.getOnTermination());

        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        addNetworks(HOBBIT_NETWORKS);

        addEnvironmentVariable(GENERATOR_ID_KEY, (String)System.getenv().get(GENERATOR_ID_KEY));
        addEnvironmentVariable(GENERATOR_COUNT_KEY, (String)System.getenv().get(GENERATOR_COUNT_KEY));
    }


}
