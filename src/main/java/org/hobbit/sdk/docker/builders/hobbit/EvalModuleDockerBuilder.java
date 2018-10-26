package org.hobbit.sdk.docker.builders.hobbit;

import org.hobbit.sdk.docker.builders.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.BothTypesDockersBuilder;

import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.Constants.HOBBIT_NETWORKS;


/**
 * @author Pavel Smirnov
 */

public class EvalModuleDockerBuilder extends BothTypesDockersBuilder {
    private static final String name = "eval-module";

    public EvalModuleDockerBuilder(AbstractDockersBuilder builder) {
        super(builder);
    }


    @Override
    public void addEnvVars(AbstractDockersBuilder ret) {
        ret.addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        ret.addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        ret.addNetworks(HOBBIT_NETWORKS);

        ret.addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));
        //ret.addEnvironmentVariable(CONTAINER_NAME_KEY, ret.getContainerName());
    }

    @Override
    public String getName() {
        return name;
    }

}
