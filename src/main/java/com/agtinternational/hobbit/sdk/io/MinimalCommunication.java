package com.agtinternational.hobbit.sdk.io;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
public abstract class MinimalCommunication implements Communication {
    private final Communication.Consumer consumer;
    private final String name;
    private final Charset charset;

    protected MinimalCommunication(Builder builder) {
        consumer = builder.getConsumer();
        name = builder.getName();
        charset = builder.getCharset();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Charset getCharset() {
        return charset;
    }

    @Override
    public final Communication.Consumer getConsumer() {
        return consumer;
    }

    public abstract static class Builder {
        private Communication.Consumer consumer;
        private String name;
        private Charset charset;

        public Builder consumer(Communication.Consumer consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        protected Consumer getConsumer() {
            return consumer;
        }

        protected String getName() {
            return name;
        }

        protected Charset getCharset() {
            return charset;
        }

        public abstract MinimalCommunication build() throws Exception;
    }
}
