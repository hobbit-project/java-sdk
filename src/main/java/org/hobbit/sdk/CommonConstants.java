package org.hobbit.sdk;

import org.hobbit.core.Commands;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
public class CommonConstants {
    public static final String HOBBIT_NETWORK_NAME = "hobbit";
    public static final String HOBBIT_CORE_NETWORK_NAME = "hobbit-core";

    public static final String[] HOBBIT_NETWORKS = new String[]{
            CommonConstants.HOBBIT_NETWORK_NAME,
            //CommonConstants.HOBBIT_CORE_NETWORK_NAME

    };

    public static final String EXPERIMENT_URI = "http://example.com/exp1";
    public static final byte SYSTEM_CONTAINERS_FINISHED = 19;
    public static final String SYSTEM_CONTAINERS_COUNT_KEY = "SYSTEM_CONTAINERS_COUNT";
    public static final String SYSTEM_CONTAINER_ID_KEY = "SYSTEM_CONTAINER_ID_KEY";

}