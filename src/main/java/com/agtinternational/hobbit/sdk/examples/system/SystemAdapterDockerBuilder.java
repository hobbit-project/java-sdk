package com.agtinternational.hobbit.sdk.examples.system;

import com.agtinternational.hobbit.sdk.docker.BuildBasedDockerizer;
import com.agtinternational.hobbit.sdk.examples.ExampleDockersBuilder;
import com.agtinternational.hobbit.sdk.examples.system.SystemAdapter;

import java.nio.file.Paths;

import static com.agtinternational.hobbit.sdk.CommonConstants.RABBIT_MQ_HOST_NAME;
import static org.hobbit.core.Constants.RABBIT_MQ_HOST_NAME_KEY;


/**
 * @author Pavel Smirnov
 */

public class SystemAdapterDockerBuilder extends ExampleDockersBuilder {
    public SystemAdapterDockerBuilder() {

        super("ExampleSystemAdapterDockerizer");
        imageName(Paths.get(REPO_PATH,"example-system-adapter").toString());
        containerName("cont_name_example-system-adapter");
        runnerClass(SystemAdapter.class);
        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME));
    }

    @Override
    public BuildBasedDockerizer build() throws Exception {
        return super.build();
    }

}
