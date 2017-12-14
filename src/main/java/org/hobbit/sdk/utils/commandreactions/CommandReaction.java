package org.hobbit.sdk.utils.commandreactions;

import org.hobbit.sdk.utils.TriConsumer;

import java.util.function.BiConsumer;

/**
 * @author Roman Katerinenko
 */
//public interface CommandReaction extends BiConsumer<Byte, byte[]> {
public interface CommandReaction extends TriConsumer<Byte, byte[], String> {

}