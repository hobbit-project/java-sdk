package com.agtinternational.hobbit.sdk.utils.commandreactions;

import com.agtinternational.hobbit.sdk.utils.CommandSender;
import org.hobbit.core.Commands;
//import org.junit.Assert;

/**
 * @author Roman Katerinenko
 */
public class StartBenchmarkWhenSystemAndBenchmarkReady implements CommandReaction {
    private final String systemContainerId;

    private boolean benchmarkReady = false;
    private boolean systemReady = false;
    private boolean commandSent = false;

    public StartBenchmarkWhenSystemAndBenchmarkReady(String systemContainerId) {
        this.systemContainerId = systemContainerId;
    }

    @Override
    public void accept(Byte command, byte[] bytes) {
        if (command == Commands.BENCHMARK_READY_SIGNAL) {
            benchmarkReady = true;
        }
        if (command == Commands.SYSTEM_READY_SIGNAL) {
            systemReady = true;
        }
        synchronized (this) {
            if (systemReady && benchmarkReady && !commandSent) {
                commandSent = true;
                try {
                    new CommandSender(Commands.START_BENCHMARK_SIGNAL, systemContainerId).send();
                } catch (Exception e) {
                    //Assert.fail(e.getMessage());
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}