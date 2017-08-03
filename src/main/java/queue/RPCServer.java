package queue;

import com.rabbitmq.client.*;
import messages.Message;

import java.io.IOException;

public class RPCServer {

    private QueueConnection queueConnection;
    private final String queueName;

    public RPCServer(QueueConnection queueConnection) {
        this.queueConnection = queueConnection;
        this.queueName = queueConnection.getQueueName();
    }

    public void consumeMessage(final RPCAction rpcAction) {
        final Channel channel = queueConnection.createChannel();
        boolean isQueueDurable = true;
        boolean isQueueExclusive = false;
        boolean isQueueAutoDelete = false;
        int prefetchCount = 1;
        try {
            channel.queueDeclare(queueName, isQueueDurable, isQueueExclusive, isQueueAutoDelete, null);
            channel.confirmSelect(); // DK
            channel.basicQos(prefetchCount); // accept only 1 unack-ed message at a time
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();
                String jsonMessage = new String(body,"UTF-8");
                Message responseMessage = null;
                try {
                    responseMessage = rpcAction.execute(jsonMessage);
                }
                catch (RuntimeException e){
                    e.printStackTrace();
                }
                finally {
                    String response = responseMessage != null ? responseMessage.asString() : "";
                    channel.basicPublish( "", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(envelope.getDeliveryTag(), false); // Manual ACK
                }
            }
        };

        boolean autoAck = false; // Manual ACK
        try {
            channel.basicConsume(queueName, autoAck, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
