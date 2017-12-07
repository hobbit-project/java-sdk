package com.agtinternational.hobbit.sdk.docker.builders;

import com.agtinternational.hobbit.sdk.CommonConstants;
import com.agtinternational.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import com.agtinternational.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

import static org.hobbit.core.Constants.*;


/**
 * @author Pavel Smirnov
 */

public class SystemAdapterDockerBuilder extends BuildBasedDockersBuilder {
    public SystemAdapterDockerBuilder(DynamicDockerFileBuilder builder) {

        super("DummySystemAdapterDockerizer");

        imageName("dummy-system-adapter");
        containerName("cont_dummy-system-adapter");
        buildDirectory(builder.getBuildDirectory());
        dockerFileReader(builder.getDockerFileReader());

        addNetworks(CommonConstants.networks);
        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));

        addEnvironmentVariable(SYSTEM_PARAMETERS_MODEL_KEY, (String)System.getenv().get(SYSTEM_PARAMETERS_MODEL_KEY));
        addEnvironmentVariable(CONTAINER_NAME_KEY, getContainerName());

    }


}
