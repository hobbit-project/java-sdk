//package org.hobbit.sdk;
//
//import com.google.gson.JsonObject;
//import org.hobbit.controller.data.ExperimentConfiguration;
//import org.hobbit.controller.queue.ExperimentQueueImpl;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Calendar;
//
//public class ExperimentQueueTest {
//
//    private ExperimentQueueImpl queue;
//
//    @Before
//    public void init() {
//        queue = new ExperimentQueueImpl();
//    }
//
//    @Test
//    public void addToQueue(){
//        // create test config
//        ExperimentConfiguration cfg = new ExperimentConfiguration();
//        cfg.id = "1";
//
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.YEAR, 2016);
//        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
//        cal.set(Calendar.DAY_OF_MONTH, 5);
//        cfg.benchmarkUri = "http://project-hobbit.eu/sml-benchmark-v2-mt/benchmark";
//        cfg.systemUri = "http://project-hobbit.eu/sml-benchmark-v2/system-adapter2";
//        cfg.serializedBenchParams = "{\n" +
//                "  \"@id\" : \"http://w3id.org/hobbit/experiments#New\",\n" +
//                "  \"@type\" : \"http://w3id.org/hobbit/vocab#Experiment\",\n" +
//                "  \"involvesBenchmark\" : "+cfg.benchmarkUri+",\n" +
//                "  \"involvesSystemInstance\" : \""+cfg.systemUri+"\",\n" +
//                "  \"@context\" : {\n" +
//                "    \"involvesSystemInstance\" : {\n" +
//                "      \"@id\" : \"http://w3id.org/hobbit/vocab#involvesSystemInstance\",\n" +
//                "      \"@type\" : \"@id\"\n" +
//                "    },\n" +
//                "    \"involvesBenchmark\" : {\n" +
//                "      \"@id\" : \"http://w3id.org/hobbit/vocab#involvesBenchmark\",\n" +
//                "      \"@type\" : \"@id\"\n" +
//                "    }\n" +
//                "  }\n" +
//                "}\n";
//        cfg.executionDate = cal;
//
//        queue.add(cfg);
//
//
//    }
//
//}
