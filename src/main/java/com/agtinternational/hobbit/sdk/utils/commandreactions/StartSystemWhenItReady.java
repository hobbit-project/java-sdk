package com.agtinternational.hobbit.sdk.utils.commandreactions;

import com.agtinternational.hobbit.sdk.utils.CommandSender;
import org.hobbit.core.Commands;

/**
 * @author Roman Katerinenko
 */
public class StartSystemWhenItReady implements CommandReaction {
    @Override
    public void accept(Byte command, byte[] data) {
        if (command == Commands.SYSTEM_READY_SIGNAL) {
            try {
                new CommandSender(Commands.TASK_GENERATION_FINISHED).send();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                //Assert.fail(e.getMessage());
            }
        }
    }
}