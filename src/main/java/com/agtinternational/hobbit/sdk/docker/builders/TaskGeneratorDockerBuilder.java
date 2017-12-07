package com.agtinternational.hobbit.sdk.docker.builders;

import com.agtinternational.hobbit.sdk.CommonConstants;
import com.agtinternational.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import com.agtinternational.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

import static org.hobbit.core.Constants.*;


/**
 * @author Pavel Smirnov
 */

public class TaskGeneratorDockerBuilder extends BuildBasedDockersBuilder {
    public TaskGeneratorDockerBuilder(DynamicDockerFileBuilder builder){

        super("ExampleTaskGenDockerizer");

        imageName("example-task-generator");
        containerName("cont_name_example-task-generator");
        buildDirectory(builder.getBuildDirectory());
        dockerFileReader(builder.getDockerFileReader());

        addNetworks(CommonConstants.networks);
        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));

        addEnvironmentVariable(GENERATOR_ID_KEY, (String)System.getenv().get(GENERATOR_ID_KEY));
        addEnvironmentVariable(GENERATOR_COUNT_KEY, (String)System.getenv().get(GENERATOR_COUNT_KEY));
    }


}
