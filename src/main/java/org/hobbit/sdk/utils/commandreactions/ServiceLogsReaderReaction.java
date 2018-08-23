package org.hobbit.sdk.utils.commandreactions;

import com.google.gson.Gson;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.hobbit.core.Commands;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.PullBasedDockerizer;
import org.hobbit.sdk.docker.ServiceLogsReader;
import org.hobbit.sdk.utils.ComponentsExecutor;

public class ServiceLogsReaderReaction implements CommandReaction {

    private Gson gson = new Gson();
    private ComponentsExecutor componentsExecutor;

    public ServiceLogsReaderReaction(ComponentsExecutor componentsExecutor){
        this.componentsExecutor = componentsExecutor;
    }

    @Override
    public void handleCmd(Byte command, byte[] bytes, String replyTo) throws Exception {
        if (command == Commands.DOCKER_CONTAINER_START){

            String dataString = RabbitMQUtils.readString(bytes);
            StartCommandData startCommandData = gson.fromJson(dataString, StartCommandData.class);

            ServiceLogsReader reader = new ServiceLogsReader(startCommandData.image);
            componentsExecutor.submit(reader);
        }
    }
}