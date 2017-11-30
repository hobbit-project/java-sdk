package com.agtinternational.hobbit.sdk.docker;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeoutException;

/**
 * @author Pavel Smirnov
 */

public class PullBasedDockerizer extends AbstractDockerizer {


    public PullBasedDockerizer(AbstractDockerizerBuilder builder) {
        super(builder);
    }

    @Override
    public void prepareImage(String imageName) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        pullImage(imageName);
    }

    @Override
    public void stop(Boolean onStart) throws InterruptedException, DockerException, DockerCertificateException {
        logger.debug("Stopping containers and removing if needed (imageName={})", imageName);
        try {
            if (useCachedContainer!=null && useCachedContainer) {
                stopCachedContainer();
            } else {
                removeAllSameNamedContainers();
            }
        }
        catch (Exception e){
            logger.error("Exception", e);
            exception = e;
        }
    }

    public void pullImage(String imageName) throws InterruptedException, DockerException, IOException, DockerCertificateException {
        logger.debug("Pulling image (imageName={})", imageName);
        dockerClient.pull(imageName);
    }

    public static class Builder extends AbstractDockerizer.AbstractDockerizerBuilder {

        public Builder(String name){
            super(name);
        }

        @Override
        public PullBasedDockerizer build() {
            return new PullBasedDockerizer(this);

        }
    }


}