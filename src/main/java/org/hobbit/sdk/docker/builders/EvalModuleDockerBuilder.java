package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.CommonConstants;
import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.CommonConstants.HOBBIT_NETWORKS;


/**
 * @author Pavel Smirnov
 */

public class EvalModuleDockerBuilder extends BuildBasedDockersBuilder {
    public EvalModuleDockerBuilder(DynamicDockerFileBuilder builder) {
        super("EvalModuleDockerizer");
        imageName(builder.getImageNamePrefix()+"eval-module");

        containerName(builder.getContainerName());
        buildDirectory(builder.getBuildDirectory());
        dockerFileReader(builder.getDockerFileReader());
        onTermination(builder.getOnTermination());

        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        addNetworks(HOBBIT_NETWORKS);

        addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));
        addEnvironmentVariable(CONTAINER_NAME_KEY, getContainerName());
    }

}
