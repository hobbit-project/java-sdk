package org.hobbit.sdk;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
public class CommonConstants {
    public static final String HOBBIT_NETWORK_NAME = "hobbit";
    public static final String HOBBIT_CORE_NETWORK_NAME = "hobbit-common";
    public static final String[] HOBBIT_NETWORKS = new String[]{ CommonConstants.HOBBIT_NETWORK_NAME, CommonConstants.HOBBIT_CORE_NETWORK_NAME };

    public static final String SYSTEM_URI = "http://example.com/systems#sys10";
    public static final String EXPERIMENT_URI = "http://example.com/exp1";

    public static final Charset CHARSET = Charset.forName("UTF-8");

}