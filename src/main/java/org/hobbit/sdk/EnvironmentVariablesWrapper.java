package org.hobbit.sdk;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.Constants;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
//import org.junit.Rule;
import static org.hobbit.sdk.CommonConstants.*;


/**
 * @author Roman Katerinenko
 */
public class EnvironmentVariablesWrapper {

    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();


    public void setupCommunicationEnvironmentVariables(String rabbitHostName, String sesstionId) {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, rabbitHostName);
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, sesstionId);
    }

    public void setupBenchmarkEnvironmentVariables(String experimentUri, JenaKeyValue benchmarkParameters){
        environmentVariables.set(Constants.HOBBIT_EXPERIMENT_URI_KEY, experimentUri);
        environmentVariables.set(Constants.BENCHMARK_PARAMETERS_MODEL_KEY, benchmarkParameters.encodeToString());
    }

    public void setupGeneratorEnvironmentVariables(int generatorId, int generatorCount) {
        environmentVariables.set(Constants.GENERATOR_ID_KEY, String.valueOf(generatorId));
        environmentVariables.set(Constants.GENERATOR_COUNT_KEY, String.valueOf(generatorCount));
    }

    public void setupSystemEnvironmentVariables(String systemUri, JenaKeyValue systemParameters) {
        environmentVariables.set(Constants.SYSTEM_PARAMETERS_MODEL_KEY, systemParameters.encodeToString());
        environmentVariables.set(Constants.SYSTEM_URI_KEY, systemUri);
    }

}