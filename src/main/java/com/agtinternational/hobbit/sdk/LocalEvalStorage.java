package com.agtinternational.hobbit.sdk;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.io.IOUtils;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.data.RabbitQueue;
import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Semaphore;

import static com.agtinternational.hobbit.sdk.CommonConstants.SYSTEM_FINISHED_SIGNAL;
import static org.hobbit.core.Commands.EVAL_STORAGE_TERMINATE;

/**
 * @author Pavel Smirnov
 */

public class LocalEvalStorage extends AbstractEvaluationStorage {
    private static final Logger logger = LoggerFactory.getLogger(LocalEvalStorage.class);
    protected Exception exception;

    private static final int MAX_OBJECT_SIZE = 100 * 1024; // 100mb

    private Semaphore currentlyProcessedMessages;
    private Semaphore currentlyProcessedTasks;


    private final int maxParallelProcessedMsgs=1;
    private final int maxParallelProcessedTasks=1;

    private final List<Result> actualResponses = new ArrayList<>();
    private final List<Result> expectedResponses = new ArrayList<>();

    protected RabbitQueue evalStorage2EvalModuleQueue;
    protected RabbitQueue taskGen2EvalStoreQueue;
    protected RabbitQueue system2EvalStoreQueue;

    private Semaphore startMutex = new Semaphore(0);

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");

        currentlyProcessedMessages = new Semaphore(maxParallelProcessedMsgs);
        currentlyProcessedTasks = new Semaphore(maxParallelProcessedTasks);

        LocalEvalStorage receiver = this;
        system2EvalStoreQueue = createDefaultRabbitQueue(
                generateSessionQueueName(Constants.SYSTEM_2_EVAL_STORAGE_QUEUE_NAME));
        system2EvalStoreQueue.channel.basicConsume(system2EvalStoreQueue.name, false,
                new DefaultConsumer(system2EvalStoreQueue.channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                               byte[] body) throws IOException {
                        try {
                            currentlyProcessedMessages.acquire();
                        } catch (InterruptedException e) {
                            throw new IOException("Interrupted while waiting for mutex.", e);
                        }
                        try {
                            //receiver.receiveResponseData(body);
                            system2EvalStoreQueue.channel.basicAck(envelope.getDeliveryTag(), false);
                        } finally {
                            currentlyProcessedMessages.release();
                        }
                    }
                });

        system2EvalStoreQueue.channel.basicQos(maxParallelProcessedMsgs);

        taskGen2EvalStoreQueue = createDefaultRabbitQueue(
                generateSessionQueueName(Constants.TASK_GEN_2_EVAL_STORAGE_QUEUE_NAME));
        taskGen2EvalStoreQueue.channel.basicConsume(taskGen2EvalStoreQueue.name, false,
                new DefaultConsumer(taskGen2EvalStoreQueue.channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                               byte[] body) throws IOException {
                        ByteBuffer buffer = ByteBuffer.wrap(body);
                        String taskId = RabbitMQUtils.readString(buffer);
                        byte[] data = RabbitMQUtils.readByteArray(buffer);
                        try {
                            currentlyProcessedTasks.acquire();
                        } catch (InterruptedException e) {
                            throw new IOException("Interrupted while waiting for mutex.", e);
                        }
                        try {
                            //receiver.receiveGeneratedTask(taskId, data);
                            taskGen2EvalStoreQueue.channel.basicAck(envelope.getDeliveryTag(), false);
                        } finally {
                            currentlyProcessedTasks.release();
                        }
                    }
                });
        taskGen2EvalStoreQueue.channel.basicQos(maxParallelProcessedMsgs);
        evalStorage2EvalModuleQueue = createDefaultRabbitQueue(generateSessionQueueName(Constants.EVAL_STORAGE_2_EVAL_MODULE_QUEUE_NAME));
    }

    @Override
    public void run() throws Exception {
        logger.debug("run()");
        startMutex.acquire();
        // wait until all messages have been read from the queue and all sent
        // messages have been consumed
        while ((system2EvalStoreQueue.messageCount() + taskGen2EvalStoreQueue.messageCount()
                + system2EvalStoreQueue.messageCount()) > 0) {
            Thread.sleep(1000);
        }

        sendResultsToEvalModule();

        while (evalStorage2EvalModuleQueue.messageCount() > 0) {
            Thread.sleep(1000);
        }

        // Collect all open mutex counts to make sure that there is no message
        // that is still processed
        Thread.sleep(1000);
        currentlyProcessedMessages.acquire(maxParallelProcessedMsgs);
        currentlyProcessedTasks.acquire(maxParallelProcessedMsgs);

        sendToCmdQueue(Commands.EVAL_STORAGE_TERMINATE);
    }


    @Override
    public void receiveExpectedResponseData(String s, long l, byte[] bytes) {
        logger.debug("receiveExpectedResponseData()->{}",new String(bytes));
        int actualSize = bytes.length / 1024;
        expectedResponses.add(new SerializableResult(l,bytes));
//        if (actualSize < MAX_OBJECT_SIZE) {
//            smallResultStoreFacade.put(ResultType.EXPECTED, s, new SerializableResult(l, bytes));
//        } else {
//            bigResultStoreFacade.put(ResultType.EXPECTED, s, new SerializableResult(l, bytes));
//        }
    }

    @Override
    public void receiveResponseData(String s, long l, byte[] bytes) {
        int actualSize = bytes.length / 1024;
        logger.debug("receiveResponseData()->{}",new String(bytes));
        actualResponses.add(new SerializableResult(l,bytes));
//        if (actualSize < MAX_OBJECT_SIZE) {
//            smallResultStoreFacade.put(ResultType.ACTUAL, s, new SerializableResult(l, bytes));
//        } else {
//            bigResultStoreFacade.put(ResultType.ACTUAL, s, new SerializableResult(l, bytes));
//        }
    }

    @Override
    protected Iterator<ResultPair> createIterator() {
        logger.debug("createIterator()");
        String test="123";
//        Iterator<ResultPair> si = smallResultStoreFacade.createIterator();
//        Iterator<ResultPair> bi = bigResultStoreFacade.createIterator();
//
//        return IteratorUtils.chainedIterator(si, bi);
        List<ResultPair> ret = new ArrayList<>();
        for(int i = 0; i< expectedResponses.size(); i++)
            ret.add(new ResultPairImpl(expectedResponses.get(i), actualResponses.get(i)));

        return ret.iterator();
    }

    //ToDo: Implement the resultsModel
    protected void sendResultsToEvalModule() throws IOException {
//        byte[] taskIdBytes = "".getBytes(Charsets.UTF_8);
//        // + 4 for taskIdBytes.length
//        // + 4 for data.length
//        int capacity = 4 + 4 + taskIdBytes.length + data.length;
//        ByteBuffer buffer = ByteBuffer.allocate(capacity);
//        buffer.putInt(taskIdBytes.length);
//        buffer.put(taskIdBytes);
//        buffer.putInt(data.length);
//        buffer.put(data);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(1);
        // system2EvalStore
        // .basicPublish("", system2EvalStoreQueueName,
        // MessageProperties.PERSISTENT_BASIC, buffer.array());
        evalStorage2EvalModuleQueue.channel.basicPublish("", evalStorage2EvalModuleQueue.name, MessageProperties.PERSISTENT_BASIC, buffer.array());
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        // If this is the signal that a container stopped (and we have a class that we need to notify)
        //if (command == Commands.TASK_GENERATION_FINISHED) {
        if (command == SYSTEM_FINISHED_SIGNAL) {
            // release the mutex
            startMutex.release();
        }

        if ((command == Commands.DOCKER_CONTAINER_TERMINATED) ) {
            super.receiveCommand(EVAL_STORAGE_TERMINATE, null);
        } else {
            super.receiveCommand(command, data);
        }
    }

    @Override
    public void close() throws IOException {
        logger.debug("close()");
        IOUtils.closeQuietly(taskGen2EvalStoreQueue);
        IOUtils.closeQuietly(system2EvalStoreQueue);
        super.close();
    }

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

    public class ResultPairImpl implements ResultPair{

        private Result expected;
        private Result actual;
        public ResultPairImpl(Result expected, Result actual){
            this.expected = expected;
            this.actual = actual;
        }

        @Override
        public Result getExpected() {
            return expected;
        }

        @Override
        public Result getActual() {
            return actual;
        }
    }

}
