package org.hobbit.sdk;

/**
 * This file is part of evaluation-storage.
 *
 * evaluation-storage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * evaluation-storage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with evaluation-storage.  If not, see <http://www.gnu.org/licenses/>.
 */


import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Data holder for a result.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class SerializableResult implements Result {

    private static final int LONG_SIZE = Long.SIZE / Byte.SIZE;

    private final long sentTimestamp;
    private final byte[] data;

    public SerializableResult(long sentTimestamp, byte[] data) {
        this.sentTimestamp = sentTimestamp;
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SerializableResult) {
            SerializableResult other = (SerializableResult) obj;
            return sentTimestamp == other.sentTimestamp && Arrays.equals(data, other.data);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Result@%s [%s]", sentTimestamp, Arrays.toString(data));
    }

    @Override
    public long getSentTimestamp() {
        return sentTimestamp;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public byte[] serialize() {
        return ByteBuffer
                .allocate(LONG_SIZE + data.length)
                .putLong(sentTimestamp)
                .put(data)
                .array();
    }

//        public static SerializableResult deserialize(byte[] serializedData) {
//            long sentTimestamp = ByteBuffer.wrap(serializedData, 0, LONG_SIZE).getLong();
//            byte[] data = new byte[serializedData.length - LONG_SIZE];
//            ByteBuffer.wrap(serializedData, LONG_SIZE, data.length).get(data);
//            return new SerializableResult(sentTimestamp, data);
//        }
}