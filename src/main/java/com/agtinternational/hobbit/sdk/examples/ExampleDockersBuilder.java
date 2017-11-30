package com.agtinternational.hobbit.sdk.examples;

import com.agtinternational.hobbit.sdk.docker.DynamicDockerFileBuilder;

import static com.agtinternational.hobbit.sdk.CommonConstants.RABBIT_MQ_HOST_NAME;
import static org.hobbit.core.Constants.RABBIT_MQ_HOST_NAME_KEY;


/**
 * @author Pavel Smirnov
 */

public class ExampleDockersBuilder extends DynamicDockerFileBuilder {

    public static String REPO_PATH="git.project-hobbit.eu:4567/smirnp";

    public ExampleDockersBuilder(String dockerizerName){
        super(dockerizerName);
        jarFilePath("hobbit-java-sdk-1.0.jar");
        buildDirectory("target");
        dockerWorkDir("/usr/src/sdk-example-benchmark");
        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME));
    }

}
