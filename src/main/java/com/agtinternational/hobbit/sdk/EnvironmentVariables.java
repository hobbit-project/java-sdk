package com.agtinternational.hobbit.sdk;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.Constants;
import org.hobbit.core.rabbit.RabbitMQUtils;
//import org.junit.Rule;

import static com.agtinternational.hobbit.sdk.CommonConstants.RUN_LOCAL;

/**
 * @author Roman Katerinenko
 */
public class EnvironmentVariables {

    public final org.junit.contrib.java.lang.system.EnvironmentVariables environmentVariables = new org.junit.contrib.java.lang.system.EnvironmentVariables();


    protected void setupBenchmarkEnvironmentVariables(String experimentId) {
        Model emptyModel = ModelFactory.createDefaultModel();
        environmentVariables.set(Constants.BENCHMARK_PARAMETERS_MODEL_KEY, RabbitMQUtils.writeModel2String(emptyModel));
        environmentVariables.set(Constants.HOBBIT_EXPERIMENT_URI_KEY, "http://w3id.org/hobbit/experiments#" + experimentId);
        environmentVariables.set(RUN_LOCAL, "True");
    }

    protected void setupGeneratorEnvironmentVariables(int generatorId, int generatorCount) {
        Model emptyModel = ModelFactory.createDefaultModel();
        environmentVariables.set(Constants.GENERATOR_ID_KEY, String.valueOf(generatorId));
        environmentVariables.set(Constants.GENERATOR_COUNT_KEY, String.valueOf(generatorCount));
    }

    protected void setupSystemEnvironmentVariables(String systemUri) {
        Model emptyModel = ModelFactory.createDefaultModel();
        environmentVariables.set(Constants.SYSTEM_PARAMETERS_MODEL_KEY, RabbitMQUtils.writeModel2String(emptyModel));
        environmentVariables.set(Constants.SYSTEM_URI_KEY, systemUri);
    }

    protected void setupCommunicationEnvironmentVariables(String rabbitHostName, String sesstionId) {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, rabbitHostName);
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, sesstionId);
    }
}