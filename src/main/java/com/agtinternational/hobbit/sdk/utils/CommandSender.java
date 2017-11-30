package com.agtinternational.hobbit.sdk.utils;

import com.rabbitmq.client.AMQP;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.rabbit.RabbitMQUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Roman Katerinenko
 */
public class CommandSender extends AbstractCommandReceivingComponent {
    private final byte command;

    private String address;
    private byte[] data;
    private AMQP.BasicProperties properties;

    public static void sendContainerTerminatedCommand(String finishedContainerName, byte exitCode) throws Exception {
        String address = Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS;
        byte command = Commands.DOCKER_CONTAINER_TERMINATED;
        byte[] data = RabbitMQUtils.writeByteArrays(null,
                new byte[][]{RabbitMQUtils.writeString(finishedContainerName)},
                new byte[]{exitCode});
        AMQP.BasicProperties properties = null;
        new CommandSender(address, command, data, properties).send();
    }

    public CommandSender(byte command) {
        this(null, command, null, null);
    }

    public CommandSender(byte command, String dataString) {
        this(null,
                command,
                RabbitMQUtils.writeByteArrays(RabbitMQUtils.writeString(dataString), new byte[][]{}, null),
                null);
    }

    /**
     * @param data must be encoded with {@link RabbitMQUtils#writeByteArrays}
     */
    public CommandSender(byte command, byte[] data) {
        this(null, command, data, null);
    }

    /**
     * @param data must be encoded with {@link RabbitMQUtils#writeByteArrays}
     */
    public CommandSender(String address, byte command, byte[] data, AMQP.BasicProperties properties) {
        this.address = address;
        this.command = command;
        this.data = data;
        this.properties = properties;
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
    }

    @Override
    public void run() throws Exception {
    }

    public void send() throws Exception {
        super.init();
        String correctedAddress = address == null ? getHobbitSessionId() : address;
        sendToCmdQueue(correctedAddress, command, data, properties);
        close();
    }

    /**
     * @param data must be encoded with {@link RabbitMQUtils#writeByteArrays}
     */
    private void sendToCmdQueue(String address, byte command, byte[] data, AMQP.BasicProperties props) throws IOException {
        byte addressBytes[] = RabbitMQUtils.writeString(address);
        int addressBytesLength = addressBytes.length;
        int resultLength = addressBytesLength + getByteAmountIn(addressBytesLength) + getByteAmountIn(command);
        boolean attachData = (data != null) && (data.length > 0);
        if (attachData) {
            resultLength += data.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(resultLength);
        buffer.putInt(addressBytesLength);
        buffer.put(addressBytes);
        buffer.put(command);
        if (attachData) {
            buffer.put(data);
        }
        cmdChannel.basicPublish(Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "", props, buffer.array());
    }

    private int getByteAmountIn(byte byteVariable) {
        return Byte.BYTES;
    }

    private int getByteAmountIn(int intVariable) {
        return Integer.BYTES;
    }

}