package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.CommonConstants.*;


/**
 * @author Pavel Smirnov
 */

public class BenchmarkDockerBuilder extends BuildBasedDockersBuilder {

    public BenchmarkDockerBuilder(DynamicDockerFileBuilder builder) {
        super("BenchmarkDockerizer");

        imageName(builder.getImageNamePrefix()+"benchmark-controller");

        containerName(builder.getContainerName());
        buildDirectory(builder.getBuildDirectory());
        dockerFileReader(builder.getDockerFileReader());
        onTermination(builder.getOnTermination());


        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        addNetworks(HOBBIT_NETWORKS);

        addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));
        addEnvironmentVariable(BENCHMARK_PARAMETERS_MODEL_KEY, createParameters());
        addEnvironmentVariable(CONTAINER_NAME_KEY, getContainerName());

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
