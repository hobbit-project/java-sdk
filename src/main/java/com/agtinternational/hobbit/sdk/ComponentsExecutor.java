package com.agtinternational.hobbit.sdk;

import com.agtinternational.hobbit.sdk.examples.system.SystemAdapter;
import com.fasterxml.jackson.databind.node.BooleanNode;
import org.hobbit.core.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Katerinenko
 */
public class ComponentsExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ComponentsExecutor.class);
    private final static int AWAIT_TERMINATION_MILLIS = 1;
    private final static int CORE_POOL_SIZE = 100;

    private final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor = new ThreadPoolExecutor(0, CORE_POOL_SIZE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());



    public void submit(Runnable runnable) {
        executor.submit(runnable);
    }

//    public void submit(Component component){
//        submit(component, false);
//    }

    public void submit(Component component){

        executor.submit(() -> {
            String compName = component.getClass().getSimpleName();
            try {
                logger.debug("Initing "+compName);
                component.init();
                logger.debug("Running "+compName);
                component.run();
            } catch (Throwable e) {
                String message = compName+" error: "+ e.getMessage();
                logger.error(message);
                exceptions.add(new Exception(message));
            } finally {
                try {
                    component.close();
                } catch (IOException e) {
                    exceptions.add(e);
                }
            }
        });
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();
        while (executor.isTerminated()) {
            executor.awaitTermination(AWAIT_TERMINATION_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    public Collection<Throwable> getExceptions() {
        return exceptions;
    }

    public boolean anyExceptions() {
        return !getExceptions().isEmpty();
    }
}