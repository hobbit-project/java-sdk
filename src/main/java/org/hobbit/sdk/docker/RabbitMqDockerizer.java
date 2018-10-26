package org.hobbit.sdk.docker;

import org.hobbit.sdk.docker.builders.PullBasedDockersBuilder;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.PortBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.hobbit.sdk.Constants.HOBBIT_NETWORKS;

/**
 * Note! Requires Docker to be installed
 *
 * @author Roman Katerinenko
 */
public class RabbitMqDockerizer extends PullBasedDockerizer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqDockerizer.class);
    private final String hostName;
    private final String imageName;

    private RabbitMqDockerizer(Builder builder) {
        super(builder);
        hostName = builder.getHostName();
        imageName = builder.getImageName();
    }

    @Override
    public void run() {
        try {

            super.run();
            //waitUntilRunning();

        } catch (Exception e) {

            logger.error("Exception", e);
        }
    }



    public void waitUntilRunning() throws DockerCertificateException, InterruptedException, TimeoutException {
        logger.debug("Trying to connect to container at {} (imageName={})", hostName, imageName);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        boolean connected = false;
        while (!connected) {
            try {
                Connection connection = factory.newConnection();
                connected = true;
                connection.close();
            } catch (IOException e) {
                // ignore
            }
            if(!connected)
                Thread.sleep(300);
        }
    }


    public static Builder builder() {
        return new Builder();

    }

    public static class Builder extends PullBasedDockersBuilder {

        public Builder() {
            super("RabbitMqDockerizer");
            hostName("rabbit");
            containerName("rabbit");
            imageName("rabbitmq:latest");
            addPortBindings("5672/tcp", PortBinding.of("0.0.0.0", 5672));
            useCachedContainer(true);
            skipLogsReading(true);
            addNetworks(HOBBIT_NETWORKS);
        }


        public RabbitMqDockerizer build() {
            return new RabbitMqDockerizer(this);
        }
    }
}