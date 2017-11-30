package com.agtinternational.hobbit.sdk.utils.commandreactions;

import com.agtinternational.hobbit.sdk.utils.CommandQueueListener;
import com.agtinternational.hobbit.sdk.ComponentsExecutor;
import org.hobbit.core.Commands;
//import org.junit.Assert;

/**
 * @author Roman Katerinenko
 */
public class TerminateServicesWhenBenchmarkControllerFinished implements CommandReaction {
    private final CommandQueueListener commandQueueListener;
    private final ComponentsExecutor componentsExecutor;

    public TerminateServicesWhenBenchmarkControllerFinished(CommandQueueListener commandQueueListener,
                                                            ComponentsExecutor componentsExecutor) {
        this.commandQueueListener = commandQueueListener;
        this.componentsExecutor = componentsExecutor;
    }

    @Override
    public void accept(Byte command, byte[] data) {
        if (command == Commands.BENCHMARK_FINISHED_SIGNAL) {
            try {
                commandQueueListener.terminate();
                componentsExecutor.shutdown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                //Assert.fail(e.getMessage());
            }
        }
    }
}