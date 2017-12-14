package org.hobbit.sdk.utils;

import java.io.IOException;

@FunctionalInterface
public interface TriConsumer<T,U,S> {
    void handleCmd(T t, U u, S s);

}
