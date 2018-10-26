package org.hobbit.sdk.utils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.hobbit.controller.data.ExperimentConfiguration;
import org.hobbit.controller.queue.ExperimentQueueImpl;
import org.hobbit.core.Constants;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.examples.dummybenchmark.DummyBenchmarkController;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author Pavel Smirnov. (psmirnov@agtinternational.com / smirnp@gmail.com)
 */
public class QueueClient extends EnvironmentVariablesWrapper {
    private static final Logger logger = LoggerFactory.getLogger(DummyBenchmarkController.class);
    ExperimentQueueImpl queue;
    String username;

    public QueueClient(String username){
        queue = new ExperimentQueueImpl();
        this.username = username;
    }

    public QueueClient(String host, String username){
        environmentVariables.set("HOBBIT_REDIS_HOST", host);

        queue = new ExperimentQueueImpl();
        this.username = username;
    }

    public void flushQueue(){
        int deleted=0;
        for(ExperimentConfiguration configuration :  queue.listAll()){
            queue.remove(configuration);
            deleted++;
        }
        logger.info(String.valueOf(deleted)+" experiments deleted");
    }


    public void submitToQueue(String benchmarkUri, String systemUri, JenaKeyValue parameters) throws Exception {

        ExperimentConfiguration cfg = new ExperimentConfiguration();
        String id = String.valueOf(String.valueOf(new Date().getTime()));
        cfg.id = id;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cfg.executionDate = cal;
        cfg.benchmarkUri = benchmarkUri;
        cfg.systemUri = systemUri;
        cfg.userName = username;

        Model model = ModelFactory.createDefaultModel();

        String benchmarkInstanceId = Constants.NEW_EXPERIMENT_URI;
        Resource benchmarkInstanceResource = model.createResource(benchmarkInstanceId);
        model.add(benchmarkInstanceResource, RDF.type, HOBBIT.Experiment);
        model.add(benchmarkInstanceResource, HOBBIT.involvesBenchmark, model.createResource(benchmarkUri));
        model.add(benchmarkInstanceResource, HOBBIT.involvesSystemInstance, model.createResource(systemUri));

        addParameters(model, benchmarkInstanceResource, parameters);

        //JenaKeyValue benchmarkParams = BenchmarkTest.createBenchmarkParameters();
        //benchmarkParams.putAll(BenchmarkTest.createSystemParameters());

        //if(!cfg.systemUri.equals(NEPTUNE_SYSTEM_URI))
        //benchmarkParams.put(BENCHMARK_URI+"#clusterConfig", configStr);

        cfg.serializedBenchParams = RabbitMQUtils.writeModel2String(model);

        queue.add(cfg);

        logger.info(benchmarkUri+" submitted with "+systemUri);
    }

    protected static Model addParameters(Model model, Resource benchmarkInstanceResource, Map<String, Object> params) throws Exception {
        //Property valueProperty = model.getProperty("http://w3id.org/hobbit/vocab#defaultValue");
//        Property valueProperty = model.getProperty("hobbit:defaultValue");
//        for(String key: params.keySet()){
//            ResIterator iterator = model.listResourcesWithProperty(RDF.type, HOBBIT.Parameter);
//            while (iterator.hasNext()) {
//                Resource resource = iterator.nextResource();
//                if(resource.getURI().equals(key)){
//                    String abc="123";
//                }
//
//            }
//            Resource resource = model.getResource(key);
//            Statement st = resource.getProperty(valueProperty);
//            if(st!=null){
//                RDFNode obj = st.getObject();
//                String ac ="123";
//                //st.changeLiteralObject();
//            }
//        }

        for(String uri: params.keySet()){
//            String uri = paramValue.getId();
//            String datatype = Datatype.getValue(paramValue.getDatatype());
            Object value = params.get(uri);
//            String range = paramValue.getRange();
            String datatype=null;
            if(value instanceof String)
                datatype = "xsd:string";
            if(value instanceof Integer)
                datatype = "xsd:unsignedInt";
            if(value instanceof Boolean)
                datatype = "xsd:boolean";
            if(datatype==null)
                throw new Exception("Cannot define datatype for "+uri);
            model.add(benchmarkInstanceResource, model.createProperty(uri), model.createTypedLiteral(value, expandedXsdId(datatype)));

//            if (range == null) {
//                model.add(benchmarkInstanceResource, model.createProperty(uri),
//                        model.createTypedLiteral(value, expandedXsdId(datatype)));
//            } else {
//                if (range.startsWith(XSD.NS)) {
//                    model.add(benchmarkInstanceResource, model.createProperty(uri),
//                            model.createTypedLiteral(value, range));
//                } else {
//                    model.add(benchmarkInstanceResource, model.createProperty(uri), model.createResource(value));
//                }
//            }
        }

        StringWriter writer = new StringWriter();
        model.write(writer, "Turtle");

        return model;
    }

    private static String expandedXsdId(String id) {
        if (!id.startsWith("http:")){
            String prefix = id.substring(0, id.indexOf(":"));
            return id.replace(prefix + ":", XSD.NS);
        } else {
            return id;
        }
    }

//    public static Model createModel(String benchmarkUri, String systemUri){
//
//
//        try {
//            model = modifyParameters(model, benchmarkInstanceResource, benchmarkConf.getConfigurationParams());
//        } catch (Exception e) {
//            LOGGER.error("Got an exception while processing the parameters.", e);
//            throw new GUIBackendException("Please check your parameter definitions.");
//        }
//
//        byte[] data = RabbitMQUtils.writeByteArrays(new byte[] { FrontEndApiCommands.ADD_EXPERIMENT_CONFIGURATION },
//                new byte[][] { RabbitMQUtils.writeString(benchmarkUri), RabbitMQUtils.writeString(systemUri),
//                        RabbitMQUtils.writeModel(model), RabbitMQUtils.writeString(userName) },
//                null);
//
//    }
}