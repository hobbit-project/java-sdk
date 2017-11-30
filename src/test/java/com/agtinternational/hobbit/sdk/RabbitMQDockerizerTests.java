package com.agtinternational.hobbit.sdk;

import com.agtinternational.hobbit.sdk.docker.PullBasedDockerizer;
import com.agtinternational.hobbit.sdk.docker.RabbitMqDockerizer;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class RabbitMQDockerizerTests {

    @Test
    public void checkHealth(){
        RabbitMqDockerizer dockerizer = RabbitMqDockerizer.builder().build();
        dockerizer.run();
        dockerizer.stop();
        assertNull(dockerizer.anyExceptions());
    }

//    @Test
//    @Ignore
//    public void checkHealthCached(){
//        PullBasedDockerizer dockerizer = PullBasedDockerizer.builder().build();
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
