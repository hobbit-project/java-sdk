package org.hobbit.sdk.utils;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Roman Katerinenko
 */
public class ComponentsExecutor {
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private static final Logger logger = LoggerFactory.getLogger(ComponentsExecutor.class);
    private final static int AWAIT_TERMINATION_MILLIS = 1;
    private final static int CORE_POOL_SIZE = 8;

    private final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor;

    public ComponentsExecutor(){
        this(CORE_POOL_SIZE);
    }

    public ComponentsExecutor(int poolSize){
        executor = new ThreadPoolExecutor(0, poolSize, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

//    public ComponentsExecutor(EnvironmentVariables environmentVariables){
//        this.environmentVariables = environmentVariables;
//    }

//    public void submit(Object object) throws Exception {
//        if(Runnable.class.isInstance(object))
//            submit((Runnable) object);
//        else if(Component.class.isInstance(object))
//            submit((Component) object);
//        else
//            throw new Exception("Type is not supported!");
//    }



    public void submit(Runnable runnable) {
        executor.submit(runnable);
    }

    public void submit(Component component){
        submit(component, null, null);
    }

    public void submit(Component component, String containerId){
        submit(component, containerId, null);
    }


    public void submit(Component component, String containerId, String[] envVariables){

        executor.submit(() -> {
        //new Thread(() -> {
            String componentName = component.getClass().getSimpleName();
            if(AbstractDockerizer.class.isInstance(component)) {
                componentName = ((AbstractDockerizer) component).getName();
                if (envVariables!=null)
                    for (String pair : envVariables)
                        ((AbstractDockerizer)component).addEnvironmentVariable(pair);
            }else{
                if (envVariables!=null)
                    for (String pair : envVariables){
                        String[] splitted = pair.split("=");
                        environmentVariables.set(splitted[0], splitted[1]);
                    }
            }
            int exitCode = 0;
            try {
                logger.debug("Initing "+componentName /*+" "+component.hashCode()*/);
                component.init();
                logger.debug("Running "+componentName /*+" "+component.hashCode() */ );
                component.run();
            } catch (Throwable e) {
                String message = componentName+" error: "+ e.getMessage();
                logger.error(message);
                exceptions.add(new Exception(message));
                exitCode = 1;
            } finally {
                    if (containerId!=null){
                        if (AbstractDockerizer.class.isInstance(component)) {
                            final String finalContainerName = componentName;
                            final int finalExitCode = exitCode;
                            ((AbstractDockerizer) component).setOnTermination(new Callable() {
                                @Override
                                public Object call() throws Exception {
                                    CommandSender.sendContainerTerminatedCommand(containerId, (byte) finalExitCode);
                                    return finalContainerName;
                                }
                            });
                        } else
                            //if (!CommandQueueListener.class.isInstance(component)){
                            try {
                                CommandSender.sendContainerTerminatedCommand(containerId, (byte) exitCode);
                                component.close();
                            } catch (Exception e) {
                                exceptions.add(e);
                            }
                        //}
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