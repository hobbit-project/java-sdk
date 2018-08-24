package org.hobbit.sdk;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.PullBasedDockerizer;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;
import org.hobbit.sdk.docker.builders.PullBasedDockersBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.io.IOException;

public class AbstractDockerizerTest {

    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void checkHeath() throws Exception {
        environmentVariables.set("DOCKER_HOST", "tcp://localhost:2376");

        AbstractDockerizer abstractDockerizer = new PullBasedDockersBuilder("test").build();
//        abstractDockerizer.startMonitoringAndLogsReading("75f74654958cb13a95ef9801db20ea6e0861ff43369e81173f7017d575796f2f",0);
//        String test="123";


        DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
        dockerClient.listServices();

        dockerClient.listServices().get(0).spec().taskTemplate().containerSpec().image();

    }

}
