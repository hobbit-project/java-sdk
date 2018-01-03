package org.hobbit.sdk;

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

//    protected RabbitQueue evalStorage2EvalModuleQueue;
//    protected RabbitQueue taskGen2EvalStoreQueue;
//    protected RabbitQueue system2EvalStoreQueue;

    private Semaphore startMutex = new Semaphore(0);

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        currentlyProcessedMessages = new Semaphore(maxParallelProcessedMsgs);
        currentlyProcessedTasks = new Semaphore(maxParallelProcessedTasks);

    }

    @Override
    public void receiveExpectedResponseData(String s, long l, byte[] bytes) {
        logger.debug("receiveExpectedResponseData()->{}",new String(bytes));
        int actualSize = bytes.length / 1024;
        expectedResponses.add(new SerializableResult(l,bytes));
    }

    @Override
    public void receiveResponseData(String s, long l, byte[] bytes) {
        int actualSize = bytes.length / 1024;
        logger.debug("receiveResponseData()->{}",new String(bytes));
        actualResponses.add(new SerializableResult(l,bytes));
    }

    @Override
    protected Iterator<ResultPair> createIterator(){
        logger.debug("createIterator()");
        String test="123";

        List<ResultPair> ret = new ArrayList<>();
        for(int i = 0; i< expectedResponses.size(); i++)
            ret.add(new ResultPairImpl(expectedResponses.get(i), actualResponses.get(i)));

        return ret.iterator();
    }



}
