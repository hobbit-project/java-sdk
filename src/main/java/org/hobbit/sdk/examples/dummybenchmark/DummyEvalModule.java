package org.hobbit.sdk.examples.dummybenchmark;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyEvalModule extends AbstractEvaluationModule {
    private static final Logger logger = LoggerFactory.getLogger(DummyEvalModule.class);


    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp, long responseReceivedTimestamp) throws Exception {
        // evaluate the given response and store the result, e.g., increment internal counters
        logger.trace("evaluateResponse({}, {})", new String(expectedData), new String(expectedData));
    }

    @Override
    protected Model summarizeEvaluation() throws Exception {
        logger.debug("summarizeEvaluation()");
        // All tasks/responsens have been evaluated. Summarize the results,
        // write them into a Jena model and send it to the benchmark controller.
        Model model = createDefaultModel();
        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource , RDF.type, HOBBIT.Experiment);

        return model;
    }

    @Override
    public void close(){
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        try {
            super.close();
        }
        catch (Exception e){

        }
    }

}
