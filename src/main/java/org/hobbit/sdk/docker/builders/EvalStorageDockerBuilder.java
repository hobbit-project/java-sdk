package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.docker.builders.common.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.common.BothTypesDockersBuilder;
import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.CommonConstants.HOBBIT_NETWORKS;


/**
 * @author Pavel Smirnov
 */

public class EvalStorageDockerBuilder extends BothTypesDockersBuilder {
    private static final String name = "eval-storage";

    public EvalStorageDockerBuilder(AbstractDockersBuilder builder) {
        super(builder);
    }

    @Override
    public void addEnvVars(AbstractDockersBuilder ret) {
        ret.addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        ret.addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        ret.addNetworks(HOBBIT_NETWORKS);

        ret.addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));
        ret.addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        ret.addEnvironmentVariable(CONTAINER_NAME_KEY, ret.getContainerName());
    }

    @Override
    public String getName() {
        return name;
    }

}
