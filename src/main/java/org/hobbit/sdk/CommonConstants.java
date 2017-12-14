package org.hobbit.sdk;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
public class CommonConstants {
    public static final String HOBBIT_NETWORK_NAME = "hobbit";
    public static final String HOBBIT_CORE_NETWORK_NAME = "hobbit-common";
    public static final String HOBBIT_SESSION_ID = "sessionId1";
    public static final String[] HOBBIT_NETWORKS = new String[]{ CommonConstants.HOBBIT_NETWORK_NAME, CommonConstants.HOBBIT_CORE_NETWORK_NAME };

    public static final String SYSTEM_URI = "http://example.com/systems#sys10";
    public static final String EXPERIMENT_URI = "http://example.com/exp1";

    public static final Charset CHARSET = Charset.forName("UTF-8");

    public static final String LOCAL_SYSTEM_CONTAINER_KEY = "LOCAL_SYSTEM_CONTAINER";
    public static final String LOCAL_DATAGEN_CONTAINER_KEY = "LOCAL_DATAGEN_CONTAINER";
    public static final String LOCAL_TASKGEN_CONTAINER_KEY = "LOCAL_TASKGEN_CONTAINER";
    public static final String LOCAL_EVALMODULE_CONTAINER_KEY = "LOCAL_EVALMODULE_CONTAINER";
    public static final String LOCAL_EVALSTORAGE_CONTAINER_KEY = "LOCAL_EVALSTORAGE_CONTAINER";

    public static final String DOCKERIZE_KEY = "DOCKERIZE";
    public static final String CACHED_IMAGES_KEY = "USE_CACHED_IMAGES";

}