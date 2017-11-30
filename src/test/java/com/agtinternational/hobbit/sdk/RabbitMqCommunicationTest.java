package com.agtinternational.hobbit.sdk;
import com.agtinternational.hobbit.sdk.docker.RabbitMqDockerizer;
import com.agtinternational.hobbit.sdk.io.Communication;
import com.agtinternational.hobbit.sdk.io.RabbitMqCommunication;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertNull;

/**
 * @author Roman Katerinenko
 */
public class RabbitMqCommunicationTest {
    private static final String RABBIT_HOST_NAME = "127.0.0.1";
    private static final String RABBIT_MQ_CONTAINER_NAME = "rabbit";
    private RabbitMqDockerizer dockerizer;

    private String actualMessage;
    @Before
    public void before() throws Exception {
        dockerizer = RabbitMqDockerizer.builder()
                .useCachedContainer()
                .build();
        dockerizer.run();
        assertNull(dockerizer.anyExceptions());
    }

    @After
    public void after() throws Exception {
        dockerizer.stop();
    }

    @Test
    public void checkDataTransmission() throws Exception {

        String queueName = "dummyQueue";
        Charset charset = Charset.forName("UTF-8");
        CountDownLatch messageDeliveredLatch = new CountDownLatch(1);
        Communication communication = new RabbitMqCommunication.Builder()
                .host(RABBIT_HOST_NAME)
                .name(queueName)
                .charset(charset)
                .consumer(bytes -> {
                    actualMessage = new String(bytes, charset);
                    messageDeliveredLatch.countDown();
                })
                .build();
        String expectedMessage = "testMessage";
        communication.send(expectedMessage);
        messageDeliveredLatch.await();
        communication.close();
        Assert.assertEquals(expectedMessage, actualMessage);
    }
}