package org.hobbit.sdk.docker.builders.hobbit;

import org.hobbit.sdk.docker.builders.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.BothTypesDockersBuilder;

import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.CommonConstants.HOBBIT_NETWORKS;


/**
 * @author Pavel Smirnov
 */

public class TaskGenDockerBuilder extends BothTypesDockersBuilder {
    private static final String name = "task-generator";

    public TaskGenDockerBuilder(AbstractDockersBuilder builder) {
        super(builder);
    }

    @Override
    public void addEnvVars(AbstractDockersBuilder ret) {
        ret.addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        ret.addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        ret.addNetworks(HOBBIT_NETWORKS);

        ret.addEnvironmentVariable(GENERATOR_ID_KEY, (String)System.getenv().get(GENERATOR_ID_KEY));
        ret.addEnvironmentVariable(GENERATOR_COUNT_KEY, (String)System.getenv().get(GENERATOR_COUNT_KEY));
        //ret.addEnvironmentVariable(CONTAINER_NAME_KEY, ret.getContainerName());
    }

    @Override
    public String getName() {
        return name;
    }
}



