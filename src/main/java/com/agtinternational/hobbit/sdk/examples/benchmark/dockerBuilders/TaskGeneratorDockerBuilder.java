package com.agtinternational.hobbit.sdk.examples.benchmark.dockerBuilders;

import com.agtinternational.hobbit.sdk.docker.BuildBasedDockerizer;
import com.agtinternational.hobbit.sdk.examples.ExampleDockersBuilder;
import com.agtinternational.hobbit.sdk.examples.benchmark.TaskGenerator;

import java.nio.file.Paths;

import static com.agtinternational.hobbit.sdk.CommonConstants.RABBIT_MQ_HOST_NAME;
import static org.hobbit.core.Constants.RABBIT_MQ_HOST_NAME_KEY;


/**
 * @author Pavel Smirnov
 */

public class TaskGeneratorDockerBuilder extends ExampleDockersBuilder {
    public TaskGeneratorDockerBuilder() {

        super("ExampleTaskGenDockerizer");
        imageName(Paths.get(REPO_PATH,"example-task-generator").toString());
        containerName("cont_name_example-task-generator");
        runnerClass(TaskGenerator.class);
        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME));
    }

    @Override
    public BuildBasedDockerizer build() throws Exception {

        return super.build();
    }

}
