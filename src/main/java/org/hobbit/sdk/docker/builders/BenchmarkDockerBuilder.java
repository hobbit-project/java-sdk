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
        super("ExampleBenchmarkDockerizer");

        imageName("benchmark-controller");
        containerName(builder.getContainerName());
        buildDirectory(builder.getBuildDirectory());
        dockerFileReader(builder.getDockerFileReader());
        onTermination(builder.getOnTermination());


        addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        addNetworks(HOBBIT_NETWORKS);

        addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY));
        addEnvironmentVariable(BENCHMARK_PARAMETERS_MODEL_KEY, createParameters());

//        //SDK's properties
//
//        addEnvironmentVariable(LOCAL_DATAGEN_CONTAINER_KEY, (String)System.getenv().get(LOCAL_DATAGEN_CONTAINER_KEY));
//        addEnvironmentVariable(LOCAL_TASKGEN_CONTAINER_KEY, (String)System.getenv().get(LOCAL_TASKGEN_CONTAINER_KEY));
//        addEnvironmentVariable(LOCAL_EVALSTORAGE_CONTAINER_KEY, (String)System.getenv().get(LOCAL_EVALSTORAGE_CONTAINER_KEY));
//        addEnvironmentVariable(LOCAL_SYSTEM_CONTAINER_KEY, (String)System.getenv().get(LOCAL_SYSTEM_CONTAINER_KEY));
//        addEnvironmentVariable(LOCAL_EVALMODULE_CONTAINER_KEY, (String)System.getenv().get(LOCAL_EVALMODULE_CONTAINER_KEY));
//
//        //addEnvironmentVariable(DOCKERIZE_KEY, (String)System.getenv().get(DOCKERIZE_KEY));
//        //addEnvironmentVariable(CACHED_IMAGES_KEY, (String)System.getenv().get(CACHED_IMAGES_KEY));


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
