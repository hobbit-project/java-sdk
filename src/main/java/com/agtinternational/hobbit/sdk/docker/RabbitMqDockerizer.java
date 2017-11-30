package com.agtinternational.hobbit.sdk.docker;

import com.agtinternational.hobbit.sdk.CommonConstants;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.PortBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Note! Requires Docker to be installed
 *
 * @author Roman Katerinenko
 */
public class RabbitMqDockerizer extends PullBasedDockerizer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqDockerizer.class);

    private final String imageName;
    private final Boolean useCachedContainer;

    private RabbitMqDockerizer(Builder builder) {
        super(builder.toGenericBuilder());

        imageName = builder.imageName;
        useCachedContainer = builder.useCachedContainer;

    }

    @Override
    public void run() {
        try {

            super.run();
            waitUntilRunning();

        } catch (Exception e) {

            logger.error("Exception", e);
        }
    }


    @Override
    public void stop(){
        logger.debug("Stopping containers and deleting if needed (imageName={})", imageName);
        if (useCachedContainer)
            super.stopCachedContainer();
        else
            removeAllSameNamedContainers();
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
            Thread.sleep(300);
        }
    }


    public static Builder builder() {
        return new Builder("RabbitMqDockerizer");
    }

    public static class Builder extends PullBasedDockerizer.Builder {

        private String imageName = "rabbitmq:latest";
        private String[] networks = new String[]{ CommonConstants.HOBBIT_NETWORK_NAME, CommonConstants.HOBBIT_CORE_NETWORK_NAME };
        private Boolean useCachedContainer = false;
        private Boolean skipLogsReading = true;
        AbstractDockerizerBuilder ret;

        public Builder(String name) {
            super(name);
            ret = new PullBasedDockerizer.Builder(RabbitMqDockerizer.class.getName());

        }


        public Builder imageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public Builder networks(String... networks) {
            this.networks = networks;
            return this;
        }

        public Builder useCachedContainer() {
            this.useCachedContainer = true;
            return this;
        }

        public Builder useCachedContainer(Boolean value) {
            this.useCachedContainer = value;
            return this;
        }

        public Builder hostName(String value) {
            ret.hostName(value);
            return this;
        }

        public Builder skipLogsReading(Boolean value) {
            this.skipLogsReading = value;
            return this;
        }

        public AbstractDockerizerBuilder toGenericBuilder(){
                    ret
                    .imageName(imageName)
                    .containerName("rabbit")
                    .addNetworks(networks)
                    .addPortBindings("5672/tcp", PortBinding.of("0.0.0.0", 5672))
                    .skipLogsReading(skipLogsReading)
                    .useCachedContainer(useCachedContainer);

            return ret;
        }

        public RabbitMqDockerizer build() {
            return new RabbitMqDockerizer(this);
        }
    }
}