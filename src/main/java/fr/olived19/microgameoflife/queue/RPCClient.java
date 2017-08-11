package fr.olived19.microgameoflife.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RPCClient {

    private QueueConnection queueConnection;
    private final String queueName;

    public RPCClient(QueueConnection queueConnection) {
        this.queueConnection = queueConnection;
        this.queueName = queueConnection.getQueueName();
    }

    public String publishMessage(String message, final String correlationId) {
        String result;
        try {
            Channel channel = queueConnection.createChannel();
            String replyQueueName = channel.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueueName)
                    .build();
            channel.basicPublish("", queueName, props, message.getBytes("UTF-8"));
            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
            channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(correlationId)) {
                        response.offer(new String(body, "UTF-8"));
                    }
                }
            });
            result = response.take();
        } catch(IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
