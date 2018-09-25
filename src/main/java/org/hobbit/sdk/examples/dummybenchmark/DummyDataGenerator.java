package org.hobbit.sdk.examples.dummybenchmark;

import org.hobbit.core.components.AbstractDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;

import static org.hobbit.sdk.CommonConstants.EXPERIMENT_URI;

/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyDataGenerator extends AbstractDataGenerator {
    //private static final Logger logger = LoggerFactory.getLogger(DummyDataGenerator.class);
    private Logger logger;
    int messages = 1000;

    @Override
    public void init() throws Exception {
        // Always initFileReader the super class first!
        super.init();
        logger = LoggerFactory.getLogger(DummyDataGenerator.class.getName()+"_"+getGeneratorId());
        logger.debug("Init finished");


        //FileReader fileReader = new FileReader("data/data.dat");

        if(System.getenv().containsKey(EXPERIMENT_URI+"#messages")){
            messages = Integer.parseInt(System.getenv().get(EXPERIMENT_URI+"#messages"));
        }

        // Your initialization code comes here...
    }

    @Override
    protected void generateData() throws Exception {
        // Create your data inside this method. You might want to use the
        // id of this data generator and the number of all data generators
        // running in parallel.
        int dataGeneratorId = getGeneratorId();
        int numberOfGenerators = getNumberOfGenerators();

        logger.debug("generateData()");
        String data;
        int i=0;
        while(i<messages){
            i++;
            // Create your data here
            data = new String("data_"+String.valueOf(i));

            // the data can be sent to the task generator(s) ...
            logger.trace("sendDataToTaskGenerator()->{}",data);
            sendDataToTaskGenerator(data.getBytes());

            // an to system adapter
            //logger.debug("sendDataToSystemAdapter()->{}",data);
            //sendDataToSystemAdapter(data.getBytes());
            //Thread.sleep(1000);
        }

    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        super.close();
    }

}