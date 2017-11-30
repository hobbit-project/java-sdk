package com.agtinternational.hobbit.sdk.examples.benchmark.dockerBuilders;

import com.agtinternational.hobbit.sdk.docker.BuildBasedDockerizer;
import com.agtinternational.hobbit.sdk.examples.ExampleDockersBuilder;
import com.agtinternational.hobbit.sdk.examples.benchmark.EvaluationModule;

import java.nio.file.Paths;

import static com.agtinternational.hobbit.sdk.CommonConstants.RABBIT_MQ_HOST_NAME;
import static org.hobbit.core.Constants.HOBBIT_EXPERIMENT_URI_KEY;
import static org.hobbit.core.Constants.RABBIT_MQ_HOST_NAME_KEY;


/**
 * @author Pavel Smirnov
 */

public class EvalModuleDockerBuilder extends ExampleDockersBuilder {
    public EvalModuleDockerBuilder() {
        super("ExampleEvalModuleDockerizer");
        imageName(Paths.get(REPO_PATH,"example-eval-module").toString());
        containerName("cont_name_example-eval-module");
        runnerClass(EvaluationModule.class);
        addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));

    }

    @Override
    public BuildBasedDockerizer build() throws Exception {
        return super.build();
    }

}
