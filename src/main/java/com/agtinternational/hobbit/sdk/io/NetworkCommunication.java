package com.agtinternational.hobbit.sdk.io;

/**
 * @author Roman Katerinenko
 */
public abstract class NetworkCommunication extends MinimalCommunication {
    private final String host;
    private final int prefetchCount;

    protected NetworkCommunication(Builder builder) {
        super(builder);
        host = builder.getHost();
        prefetchCount = builder.getPrefetchCount();
    }

    public final String getHost() {
        return host;
    }

    public final int getPrefetchCount() {
        return prefetchCount;
    }

    public abstract static class Builder extends MinimalCommunication.Builder {
        private String host;
        private int prefetchCount;

        public NetworkCommunication.Builder host(String host) {
            this.host = host;
            return this;
        }

        public NetworkCommunication.Builder prefetchCount(int prefetchCount) {
            this.prefetchCount = prefetchCount;
            return this;
        }

        protected String getHost() {
            return host;
        }

        protected int getPrefetchCount() {
            return prefetchCount;
        }
    }
}