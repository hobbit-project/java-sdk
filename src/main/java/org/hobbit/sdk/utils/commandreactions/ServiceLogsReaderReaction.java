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

import java.util.*;
import java.util.stream.Collectors;

public class ServiceLogsReaderReaction implements CommandReaction {

    private Gson gson = new Gson();
    private ComponentsExecutor componentsExecutor;
    List<String> imagesToListen;
    Map<String, ServiceLogsReader> submittedReaders;

    public ServiceLogsReaderReaction(CommandReactionsBuilder builder){
        this.componentsExecutor = builder.componentsExecutor;
        this.imagesToListen = Arrays.asList(new String[]{
                builder.benchmarkControllerImageName,
                builder.dataGeneratorImageName,
                builder.taskGeneratorImageName,
                builder.systemAdapterImageName,
                builder.evalStorageImageName,
                builder.evalModuleImageName,
        });
        submittedReaders = new HashMap<>();
    }

    public ServiceLogsReaderReaction(CommandReactionsBuilder builder, String [] imagesToListen){
        this(builder);
        this.imagesToListen = Arrays.asList(imagesToListen);
    }

    @Override
    public void handleCmd(Byte command, byte[] bytes, String replyTo) throws Exception {
        if (command == Commands.DOCKER_CONTAINER_START){

            String dataString = RabbitMQUtils.readString(bytes);
            StartCommandData startCommandData = gson.fromJson(dataString, StartCommandData.class);
            if(!submittedReaders.containsKey(startCommandData.image))
                if(imagesToListen==null || imagesToListen.contains(startCommandData.image)) {
                    ServiceLogsReader reader = new ServiceLogsReader(startCommandData.image);
                    componentsExecutor.submit(reader);
                    submittedReaders.put(startCommandData.image, reader);
                }
        }
    }
}