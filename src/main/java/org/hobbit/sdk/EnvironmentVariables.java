package org.hobbit.sdk;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.Constants;
import org.hobbit.core.rabbit.RabbitMQUtils;
//import org.junit.Rule;
import static org.hobbit.sdk.CommonConstants.*;


/**
 * @author Roman Katerinenko
 */
public class EnvironmentVariables {

    public final org.junit.contrib.java.lang.system.EnvironmentVariables environmentVariables = new org.junit.contrib.java.lang.system.EnvironmentVariables();


    public void setupCommunicationEnvironmentVariables(String rabbitHostName, String sesstionId) {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, rabbitHostName);
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, sesstionId);
    }

    public void setupBenchmarkEnvironmentVariables(String experimentId){
        Model emptyModel = ModelFactory.createDefaultModel();
        environmentVariables.set(Constants.BENCHMARK_PARAMETERS_MODEL_KEY, RabbitMQUtils.writeModel2String(emptyModel));
        environmentVariables.set(Constants.HOBBIT_EXPERIMENT_URI_KEY, "http://w3id.org/hobbit/experiments#" + experimentId);
    }

    public void setupGeneratorEnvironmentVariables(int generatorId, int generatorCount) {
        Model emptyModel = ModelFactory.createDefaultModel();
        environmentVariables.set(Constants.GENERATOR_ID_KEY, String.valueOf(generatorId));
        environmentVariables.set(Constants.GENERATOR_COUNT_KEY, String.valueOf(generatorCount));
    }

    public void setupSystemEnvironmentVariables(String systemUri) {
        Model emptyModel = ModelFactory.createDefaultModel();
        environmentVariables.set(Constants.SYSTEM_PARAMETERS_MODEL_KEY, RabbitMQUtils.writeModel2String(emptyModel));
        environmentVariables.set(Constants.SYSTEM_URI_KEY, systemUri);
    }

//    public void setupSdkEnvironmentVariables(String datagenContainerId,
//                                             String taskgenContainerId,
//                                             String evalStorageContainerId,
//                                             String systemContainerId,
//                                             String evalModuleContainerId
//                                             //Boolean dockerize,
//                                             //Boolean useCachedImages
//    ){
//        Model emptyModel = ModelFactory.createDefaultModel();
//        environmentVariables.set(LOCAL_DATAGEN_CONTAINER_KEY, datagenContainerId);
//        environmentVariables.set(LOCAL_TASKGEN_CONTAINER_KEY, taskgenContainerId);
//        environmentVariables.set(LOCAL_EVALSTORAGE_CONTAINER_KEY, evalStorageContainerId);
//        environmentVariables.set(LOCAL_SYSTEM_CONTAINER_KEY, systemContainerId);
//        environmentVariables.set(LOCAL_EVALMODULE_CONTAINER_KEY, evalModuleContainerId);
////        if(dockerize)
////            environmentVariables.set(DOCKERIZE_KEY, String.valueOf(useCachedImages));
////        if(useCachedImages)
////            environmentVariables.set(CACHED_IMAGES_KEY, String.valueOf(useCachedImages));
//
//    }
}