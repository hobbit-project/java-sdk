package org.hobbit.sdk.docker;

import org.hobbit.sdk.docker.builders.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.BothTypesDockersBuilder;
import org.hobbit.sdk.examples.dummybenchmark.DummyBenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThreadedImageBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadedImageBuilder.class);
    ExecutorService es;
    List<Callable<String>> tasks = new ArrayList();

    public MultiThreadedImageBuilder(int numThreads){
        es = Executors.newFixedThreadPool(numThreads);
    }

    public void addTask(BothTypesDockersBuilder dockersBuilder){
        tasks.add(new Callable<String>(){
            @Override
            public String call() throws Exception {
                try {
                    dockersBuilder.build().prepareImage();
                }
                catch (Exception e){
                    logger.error(e.getMessage());
                }
                return null;
            }
        });
    }

    public void addTask(AbstractDockersBuilder dockersBuilder){
        tasks.add(new Callable<String>(){
            @Override
            public String call() throws Exception {
                try {
                    dockersBuilder.build().prepareImage();
                }
                catch (Exception e){
                    logger.error(e.getMessage());
                }
                return null;
            }
        });
    }

    public void build() throws InterruptedException {

        long started = new Date().getTime();
        es.invokeAll(tasks);

        long took = (new Date().getTime()-started)/1000;
        System.out.println("Building took "+took+" seconds");
    }
}
