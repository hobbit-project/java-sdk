package org.hobbit.sdk;

import org.hobbit.sdk.docker.RabbitMqDockerizer;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
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

//    @Test
//    @Ignore
//    public void checkHealthCached(){
//        PullBasedDockerizer dockerizer = PullBasedDockerizer.builder().buildPlatformCommandsReaction();
//
//        dockerizer.run();
//
//        dockerizer.stop();
//        System.out.println("Starting cached test again");
//        dockerizer.run();
//        dockerizer.stop();
//        assertNull(dockerizer.anyExceptions());
//
//    }

}
