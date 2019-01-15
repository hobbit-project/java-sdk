package org.hobbit.sdk;

import org.hobbit.sdk.docker.PullBasedDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.hobbit.sdk.docker.builders.PullBasedDockersBuilder;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class RabbitMQDockerizerTests {

    @Test
    public void checkHealth() throws InterruptedException, DockerException, DockerCertificateException {
        RabbitMqDockerizer dockerizer = RabbitMqDockerizer.builder().build();
        dockerizer.run();
        dockerizer.stop();
        assertNull(dockerizer.anyExceptions());
    }

    @Test
    @Ignore
    public void checkHealthCached() throws InterruptedException, DockerException, DockerCertificateException {
        PullBasedDockerizer dockerizer = new PullBasedDockersBuilder("rabbitmq").build();
        dockerizer.run();

        dockerizer.stop();
        System.out.println("Starting cached test again");

        dockerizer.run();
        dockerizer.stop();
        assertNull(dockerizer.anyExceptions());

    }

}
