package org.hobbit.sdk.utils.commandreactions;

import org.hobbit.core.Commands;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.utils.CommandSender;

import java.awt.*;
import java.lang.reflect.Executable;

public class StartEvalModuleWhenSystemFinished implements CommandReaction {
    private final String systemContainerId;

    private boolean systemFinished = false;
    private boolean moduleStarted = false;
    private ComponentsExecutor componentsExecutor;
    private Runnable component;

    public StartEvalModuleWhenSystemFinished(Runnable component, String systemContainerId){
        this.systemContainerId = systemContainerId;
        this.componentsExecutor = componentsExecutor;
        this.component = component;
    }

    @Override
    public void handleCmd(Byte command, byte[] bytes, String replyTo){
        if (command == Commands.DOCKER_CONTAINER_START){
            systemFinished = true;
        }
        if (command == Commands.DOCKER_CONTAINER_TERMINATED) {
            systemFinished = true;
        }

        synchronized (this) {
            if (systemFinished && !moduleStarted) {
                moduleStarted = true;
                component.run();
            }
        }
    }
}
