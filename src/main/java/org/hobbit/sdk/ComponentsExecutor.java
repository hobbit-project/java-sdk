package org.hobbit.sdk;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.CommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Roman Katerinenko
 */
public class ComponentsExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ComponentsExecutor.class);
    private final static int AWAIT_TERMINATION_MILLIS = 1;
    private final static int CORE_POOL_SIZE = 8;
    private final CommandQueueListener commandQueueListener;

    private final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor = new ThreadPoolExecutor(0, CORE_POOL_SIZE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    public ComponentsExecutor(){
        commandQueueListener = new CommandQueueListener();
    }

    public ComponentsExecutor(CommandQueueListener commandListener){
        commandQueueListener = commandListener;
    }


    public void submit(Object object) throws Exception {
        if(Runnable.class.isInstance(object))
            submit((Runnable) object);
        else if(Component.class.isInstance(object))
            submit((Component) object);
        else
            throw new Exception("Type is not supported!");
    }

    public void submit(Runnable runnable) {
        executor.submit(runnable);
    }

    public void submit(Component component){

        executor.submit(() -> {

            String containerName = component.getClass().getSimpleName();
            if(AbstractDockerizer.class.isInstance(component))
                containerName = ((AbstractDockerizer)component).getContainerName();
            int exitCode = 0;
            try {
                logger.debug("Initing "+containerName);
                component.init();
                logger.debug("Running "+containerName);
                component.run();
            } catch (Throwable e) {
                String message = containerName+" error: "+ e.getMessage();
                logger.error(message);
                exceptions.add(new Exception(message));
                exitCode = 1;
            } finally {
                    if(AbstractDockerizer.class.isInstance(component)){
                        final String finalContainerName = containerName;
                        final int finalExitCode = exitCode;
                        ((AbstractDockerizer)component).setOnTermination(new Callable() {
                            @Override
                            public Object call() throws Exception {
                                CommandSender.sendContainerTerminatedCommand(finalContainerName, (byte)finalExitCode);
                                return finalContainerName;
                            }
                        });
                    }else if(!CommandQueueListener.class.isInstance(component)){
                        try {
                            CommandSender.sendContainerTerminatedCommand(containerName, (byte)exitCode);
                            component.close();
                        } catch (Exception e) {
                            exceptions.add(e);
                        }

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