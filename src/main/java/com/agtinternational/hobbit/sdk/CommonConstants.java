package com.agtinternational.hobbit.sdk;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
public class CommonConstants {
    public static final String HOBBIT_NETWORK_NAME = "hobbit";
    public static final String HOBBIT_CORE_NETWORK_NAME = "hobbit-common";
    public static final String HOBBIT_SESSION_ID = "sessionId1";
    public static final String SYSTEM_URI = "http://example.com/systems#sys10";

    public static final String RABBIT_MQ_CONTAINER_NAME = "rabbit";
    public static final String RABBIT_MQ_HOST_NAME = "rabbit";
    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final String RUN_LOCAL = "RUN_COMPONENTS_LOCALLY";
    public static final byte SYSTEM_FINISHED_SIGNAL = 19;

    private CommonConstants() {
    }
}