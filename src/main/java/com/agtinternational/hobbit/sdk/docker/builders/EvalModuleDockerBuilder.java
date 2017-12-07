package com.agtinternational.hobbit.sdk.docker.builders;

import com.agtinternational.hobbit.sdk.CommonConstants;
import com.agtinternational.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import com.agtinternational.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

import static org.hobbit.core.Constants.*;


/**
 * @author Pavel Smirnov
 */

public class EvalModuleDockerBuilder extends BuildBasedDockersBuilder {
    public EvalModuleDockerBuilder(DynamicDockerFileBuilder builder) {
        super("ExampleEvalModuleDockerizer");
        imageName("dummy-eval-module");
        containerName("cont_name_example-eval-module");
        buildDirectory(builder.getBuildDirectory());
        dockerFileReader(builder.getDockerFileReader());

        addNetworks(CommonConstants.networks);
        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));

        addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
    }



}
