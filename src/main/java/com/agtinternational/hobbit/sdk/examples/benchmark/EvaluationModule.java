package com.agtinternational.hobbit.sdk.examples.benchmark;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;


/**
 * @author Pavel Smirnov
 */

public class EvaluationModule extends AbstractEvaluationModule {
    private static final Logger logger = LoggerFactory.getLogger(EvaluationModule.class);
    private Semaphore startMutex = new Semaphore(0);

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        logger.debug("Init()");
        sendToCmdQueue(Commands.EVAL_MODULE_READY_SIGNAL);
        // Your initialization code comes here...
    }

    @Override
    public void run() throws Exception {
        logger.debug("run()");
        startMutex.acquire();

        logger.debug("collectResponces()");
        collectResponses();
        Model model = summarizeEvaluation();
        logger.info("The result model has " + model.size() + " triples.");
        sendResultModel(model);


    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        // If this is the signal that a container stopped (and we have a class that we need to notify)
        if (command == Commands.EVAL_STORAGE_TERMINATE) {
            // release the mutex
            startMutex.release();
        }

        if ((command == Commands.DOCKER_CONTAINER_TERMINATED) ) {
            super.receiveCommand(Commands.EVAL_STORAGE_TERMINATE, null);
        } else {
            super.receiveCommand(command, data);
        }
    }


    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp, long responseReceivedTimestamp) throws Exception {
        // evaluate the given response and store the result, e.g., increment internal counters
        logger.debug("evaluateResponse()");
    }

    //ToDo: check that model coreectly parsed
    private void sendResultModel(Model model) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "JSONLD");
        sendToCmdQueue(Commands.EVAL_MODULE_FINISHED_SIGNAL, outputStream.toByteArray());
    }

    @Override
    //ToDo: fill model with results
    protected Model summarizeEvaluation() throws Exception {
        // All tasks/responsens have been evaluated. Summarize the results,
        // write them into a Jena model and send it to the benchmark controller.
        Model model = createDefaultModel();
        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource , RDF.type, HOBBIT.Experiment);

        logger.debug("summarizeEvaluation()");

        return model;
    }

    @Override
    public void close() throws IOException {
        //byte data[] = RabbitMQUtils.writeString("00");
        byte data[] = new byte[]{ 0 };
//        byte data[] = RabbitMQUtils.writeString(
//                new Gson().toJson(new StopCommandData("system")));
        sendToCmdQueue(Commands.EVAL_MODULE_FINISHED_SIGNAL);

        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        super.close();
    }

}
