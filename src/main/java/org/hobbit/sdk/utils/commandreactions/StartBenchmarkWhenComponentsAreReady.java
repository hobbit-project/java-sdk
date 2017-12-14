package org.hobbit.sdk.utils.commandreactions;

import org.hobbit.sdk.utils.CommandSender;
import org.hobbit.core.Commands;
//import org.junit.Assert;

/**
 * @author Roman Katerinenko
 */
public class StartBenchmarkWhenComponentsAreReady implements CommandReaction {
    private final String systemContainerId;

    private boolean benchmarkReady = false;
    private boolean dataGenReady = false;
    private boolean taskGenReady = false;
    private boolean evalStorageReady = false;

    private boolean systemReady = false;
    private boolean commandSent = false;

    public StartBenchmarkWhenComponentsAreReady(String systemContainerId) {
        this.systemContainerId = systemContainerId;
    }

    @Override
    public void handleCmd(Byte command, byte[] bytes, String replyTo){

        if (command == Commands.BENCHMARK_READY_SIGNAL)
            benchmarkReady = true;

        if (command == Commands.DATA_GENERATOR_READY_SIGNAL)
            dataGenReady = true;

        if (command == Commands.TASK_GENERATOR_READY_SIGNAL)
            taskGenReady = true;

        if (command == Commands.EVAL_STORAGE_READY_SIGNAL)
            evalStorageReady = true;

        if (command == Commands.SYSTEM_READY_SIGNAL)
            systemReady = true;

        synchronized (this){
            if (benchmarkReady && dataGenReady && taskGenReady && evalStorageReady && systemReady && !commandSent) {
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