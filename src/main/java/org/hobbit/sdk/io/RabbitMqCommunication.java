package org.hobbit.sdk.io;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import org.hobbit.core.data.RabbitQueue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Katerinenko
 */
public class RabbitMqCommunication extends NetworkCommunication {
    private final RabbitQueue queue;

    private RabbitMqCommunication(Builder builder) {
        super(builder);
        queue = builder.queue;
    }

    @Override
    public void close() throws Exception {
        Channel channel = queue.getChannel();
        Connection connection = channel.getConnection();
        channel.close();
        connection.close();
    }

    @Override
    public void send(byte[] bytes) throws IOException {
        Channel channel = queue.getChannel();
        channel.basicPublish("", getName(), MessageProperties.PERSISTENT_BASIC, bytes);
    }

    @Override
    public void send(String string) throws IOException {
        send(string.getBytes(getCharset()));
    }

    public static class Builder extends NetworkCommunication.Builder {
        private RabbitQueue queue;

        public RabbitMqCommunication build() throws Exception {
            if (getName() == null) {
                throw new IllegalStateException("queue name mustn't be null");
            }
            if (getHost() == null) {
                throw new IllegalStateException("host name mustn't be null");
            }
            createQueue();
            registerConsumer();
            return new RabbitMqCommunication(this);
        }

        private void createQueue() throws IOException, TimeoutException {
            Channel channel = createConnection().createChannel();
            channel.basicQos(getPrefetchCount());
            channel.queueDeclare(getName(), false, false, true, null);
            queue = new RabbitQueue(channel, getName());
        }

        private Connection createConnection() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(getHost());
            return factory.newConnection();
        }

        private void registerConsumer() throws IOException {
            if (getConsumer() != null) {
                Channel channel = queue.getChannel();
                channel.basicConsume(getName(), false, new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body) throws IOException {
                        getChannel().basicAck(envelope.getDeliveryTag(), false);
                        getConsumer().handleDelivery(body);
                    }
                });
            }
        }
    }
}
