package com.agtinternational.hobbit.sdk.examples.benchmark.dockerBuilders;

import com.agtinternational.hobbit.sdk.docker.BuildBasedDockerizer;
import com.agtinternational.hobbit.sdk.examples.ExampleDockersBuilder;
import com.agtinternational.hobbit.sdk.examples.benchmark.DataGenerator;

import java.nio.file.Paths;

import static com.agtinternational.hobbit.sdk.CommonConstants.RABBIT_MQ_HOST_NAME;
import static org.hobbit.core.Constants.RABBIT_MQ_HOST_NAME_KEY;


/**
 * @author Pavel Smirnov
 */

public class DataGeneratorDockerBuilder extends ExampleDockersBuilder {
    public DataGeneratorDockerBuilder() {
        super("ExampleDataGenDockerizer");
        imageName(Paths.get(REPO_PATH,"example-data-generator").toString());
        containerName("cont_name_example-data-generator");
        runnerClass(DataGenerator.class);
    }

    @Override
    public BuildBasedDockerizer build() throws Exception {
        return super.build();
    }

}
