package com.agtinternational.hobbit.sdk.io;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
public interface Communication {

    void close() throws Exception;

    String getName();

    Charset getCharset();

    Consumer getConsumer();

    void send(byte[] bytes) throws Exception;

    void send(String string) throws Exception;

    interface Consumer {
        void handleDelivery(byte[] bytes);
    }
}