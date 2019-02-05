package org.hobbit.sdk.utils;

import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.builders.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.BothTypesDockersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class MultiThreadedImageBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadedImageBuilder.class);
    ExecutorService es;
    List<Callable<String>> tasks = new ArrayList();

    public MultiThreadedImageBuilder(int numThreads){
        if(numThreads>1)
            es = Executors.newFixedThreadPool(numThreads);
    }

    public void addTask(BothTypesDockersBuilder dockersBuilder){
        AbstractDockerizer dockerizer = null;
        try {
            dockerizer = dockersBuilder.build();
            tasks.add(createTask(dockerizer));
        } catch (Exception e) {
            logger.error("Failed to build dockerizer {}: {}", dockersBuilder.getName(), e.getMessage());
        }
    }

    public void addTask(AbstractDockersBuilder dockersBuilder){
        AbstractDockerizer dockerizer = null;
        try {
            dockerizer = dockersBuilder.build();
            tasks.add(createTask(dockerizer));
        } catch (Exception e) {
            logger.error("Failed to build dockerizer {}: {}", dockersBuilder.getName(), e.getMessage());
        }
    }

    public Callable<String> createTask(AbstractDockerizer dockerizer){
       return new Callable<String>(){
            @Override
            public String call() throws Exception {
                try {
                    dockerizer.prepareImage();
                }
                catch (Exception e){
                    logger.error("Failed to build image {}: {}", dockerizer.getName(), e.getMessage());
                }
                return null;
            }
        };

    }

    public void build() throws Exception {

        double started = new Date().getTime();
        if(es!=null){
            List res = es.invokeAll(tasks);
            for(Object task : res){
                FutureTask t = ((FutureTask)task);
                if(!t.isDone())
                    throw new Exception("Task "+t.get().toString()+" not finished");
            }
        }else
            for(Callable task : tasks)
                task.call();


        double took = (new Date().getTime()-started)/1000;
        System.out.println("Building took "+took+" seconds");
    }
}
