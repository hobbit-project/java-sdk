package com.agtinternational.hobbit.sdk.utils.commandreactions;

import java.util.function.BiConsumer;

/**
 * @author Roman Katerinenko
 */
public interface CommandReaction extends BiConsumer<Byte, byte[]> {
}