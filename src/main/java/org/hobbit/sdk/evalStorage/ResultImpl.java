package org.hobbit.sdk.evalStorage;

import org.hobbit.core.data.Result;

/**
 * A simple structure implementing the {@link Result} interface
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 * https://github.com/hobbit-project/core/blob/master/src/main/java/org/hobbit/core/components/test/InMemoryEvaluationStore.java
 */
public class ResultImpl implements Result {

    private long sentTimestamp;
    private byte[] data;

    public ResultImpl(long sentTimestamp, byte[] data) {
        this.sentTimestamp = sentTimestamp;
        this.data = data;
    }

    public void setSentTimestamp(long sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public long getSentTimestamp() {
        return sentTimestamp;
    }

    @Override
    public byte[] getData() {
        return data;
    }

}