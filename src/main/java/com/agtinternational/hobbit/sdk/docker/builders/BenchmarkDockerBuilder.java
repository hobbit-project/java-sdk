package com.agtinternational.hobbit.sdk.docker.builders;

import com.agtinternational.hobbit.sdk.CommonConstants;
import com.agtinternational.hobbit.sdk.JenaKeyValue;
import com.agtinternational.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import com.agtinternational.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

import static com.agtinternational.hobbit.sdk.CommonConstants.RUN_LOCAL;
import static org.hobbit.core.Constants.*;


/**
 * @author Pavel Smirnov
 */

public class BenchmarkDockerBuilder extends BuildBasedDockersBuilder {

    public BenchmarkDockerBuilder(DynamicDockerFileBuilder builder) {
        super("ExampleBenchmarkDockerizer");

        imageName("dummy-benchmark-controller");
        containerName("cont_dummy-benchmark-controller");
        buildDirectory(builder.getBuildDirectory());
        dockerFileReader(builder.getDockerFileReader());

        addNetworks(CommonConstants.networks);
        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));

        addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));
        addEnvironmentVariable(BENCHMARK_PARAMETERS_MODEL_KEY, createParameters());
        addEnvironmentVariable(RUN_LOCAL, (String)System.getenv().get(RUN_LOCAL));
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
