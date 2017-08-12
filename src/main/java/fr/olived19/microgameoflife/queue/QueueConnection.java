package fr.olived19.microgameoflife.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueConnection {

    private String host = ConnectionFactory.DEFAULT_HOST;
    private int port = ConnectionFactory.DEFAULT_AMQP_PORT;
    private String username = ConnectionFactory.DEFAULT_USER;
    private String password = ConnectionFactory.DEFAULT_PASS;
    private int connectionTimeout = ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT;
    private Connection connection;
    private String queueName = "request_queue";

    public QueueConnection connect() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setPort(this.port);
        factory.setUsername(this.username);
        factory.setPassword(this.password);
        factory.setConnectionTimeout(this.connectionTimeout);
        connectWithRetries(factory);
        return this;
    }

    private void connectWithRetries(ConnectionFactory factory) {
        final int MAX_RETRIES = 40;
        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                this.connection = factory.newConnection();
                break;
            } catch (IOException | TimeoutException e) {
                if (i == MAX_RETRIES) {
                    throw new RuntimeException(e);
                } else {
                    try {
                        Thread.sleep(500);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    protected Channel createChannel() {
        try {
            return this.connection.createChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public QueueConnection setHost(String host) {
        this.host = host;
        return this;
    }

    public QueueConnection setPort(int port) {
        this.port = port;
        return this;
    }

    public QueueConnection setUsername(String username) {
        this.username = username;
        return this;
    }

    public QueueConnection setPassword(String password) {
        this.password = password;
        return this;
    }

    public QueueConnection setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public String getQueueName() {
        return queueName;
    }
}
