package org.hobbit.sdk;

import org.hobbit.sdk.docker.AbstractDockerizer;

import org.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Roman Katerinenko
 */
@RunWith(Parameterized.class)
public class RequiredArgumentsTest {
    private final static String NAME = "Test";
    private final static String IMAGE_NAME = "imageName1";
    private final static String CONTAINER_NAME = "containerName1";

    private final String name;
    private final String imageName;
    private final String containerName;

    @Test
    public void checkRequiredArguments() throws Exception {
        AbstractDockerizer dockerizer = new BuildBasedDockersBuilder(name)
                .imageName(imageName)
                .containerName(containerName)
                .build();
        Assert.assertNull(dockerizer);
    }

    @Parameterized.Parameters
    public static Collection parameters() throws Exception {
        return Arrays.asList(new Object[][]{
                {null, IMAGE_NAME, CONTAINER_NAME},
                {NAME, null, CONTAINER_NAME},
                {NAME, IMAGE_NAME, null}
        });
    }

    public RequiredArgumentsTest(String name,
                                 String imageName,
                                 String containerName) {
        this.name = name;
        this.imageName = imageName;
        this.containerName = containerName;
    }
}