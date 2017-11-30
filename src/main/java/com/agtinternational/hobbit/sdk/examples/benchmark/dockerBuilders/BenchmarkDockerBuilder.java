package com.agtinternational.hobbit.sdk.examples.benchmark.dockerBuilders;

import com.agtinternational.hobbit.sdk.JenaKeyValue;
import com.agtinternational.hobbit.sdk.docker.BuildBasedDockerizer;
import com.agtinternational.hobbit.sdk.examples.ExampleDockersBuilder;
import com.agtinternational.hobbit.sdk.examples.benchmark.BenchmarkController;

import java.nio.file.Paths;

import static org.hobbit.core.Constants.*;


/**
 * @author Pavel Smirnov
 */

public class BenchmarkDockerBuilder extends ExampleDockersBuilder {

    public BenchmarkDockerBuilder() {
        super("ExampleBenchmarkDockerizer");
        runnerClass(BenchmarkController.class);
        imageName(Paths.get(REPO_PATH,"example-benchmark-controller").toString());
        containerName("cont_name_example-benchmark-controller");

        addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));
        addEnvironmentVariable(BENCHMARK_PARAMETERS_MODEL_KEY, createParameters());
    }

    @Override
    public BuildBasedDockerizer build() throws Exception {
        return super.build();
    }

    private String createParameters() {
        JenaKeyValue kv = new JenaKeyValue();
        //kv.setValue(SMLConstants.DATA_POINT_COUNT_INPUT_NAME, SMLConstants.EXPECTED_DATA_POINTS_COUNT);
        return kv.encodeToString();
    }

//    HobbitComponentsDockersBuilder parameters(String parameters) {
//        addEnvironmentVariable(BENCHMARK_PARAMETERS_MODEL_KEY, parameters);
//        HobbitComponentsDockersBuilder ret = this..builder();
//        return ret;
//    }

}
