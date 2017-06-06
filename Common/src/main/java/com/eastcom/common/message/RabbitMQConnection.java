package com.eastcom.common.message;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;


/**
 * Created by linghang.kong on 2017/3/23.
 */
public class RabbitMQConnection {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConnection.class);

    private ConnectionFactory factory;

    private String userName = "admin";
    private String password = "admin";
    private String host;
    private int port;


    public RabbitMQConnection(String userName, String password, String host, int port) {
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
        this.factory = new ConnectionFactory();
        factory.setUsername(this.userName);
        factory.setPassword(this.password);
//        factory.setVirtualHost(virtualHost);
        factory.setHost(this.host);
        factory.setPort(this.port);
    }

    public static void main(String[] args) throws IOException, TimeoutException {
//
//        RabbitMQConnection rabbitMQConnection = new RabbitMQConnection("admin", "admin", "10.221.247.50", 20100);
//        Connection connection = rabbitMQConnection.createConnection();
//        Channel channel = connection.createChannel();
//        String exchangeName = "KONG_TEST";
//        String routingKey = "test";
//        channel.exchangeDeclare(exchangeName, "direct", false);
//        String queueName = channel.queueDeclare().getQueue();
//        System.out.println(queueName);
//        channel.queueBind(queueName, exchangeName, routingKey);
//
//        // 添加 heads
//        Map<String, Object> headers = new HashMap<String, Object>();
//        headers.put("latitude", 51.5252949);
//        headers.put("longitude", -0.0905493);
//        byte[] messageBodyBytes = "Hello, world!".getBytes();
//
//        // publish data
//        channel.basicPublish(exchangeName, routingKey, new AMQP.BasicProperties.Builder().headers(headers).build(), messageBodyBytes);
//
//        // subscribe data
//        GetResponse response = channel.basicGet(queueName, false);
//
//        if (response == null) {
//            // No message retrieved.
//        } else {
//            // parse heads data
//            AMQP.BasicProperties props = response.getProps();
//            // parse body data
//            byte[] body = response.getBody();
//            long deliveryTag = response.getEnvelope().getDeliveryTag();
//
//            System.out.println(Arrays.toString(response.getProps().getHeaders().keySet().toArray()));
//            System.out.println(Arrays.toString(body));
//            System.out.println(deliveryTag);
//
//            channel.close();
//            rabbitMQConnection.deleteConnection(connection);
//
//        }

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("latitude", 51.5252949);
        headers.put("longitude", -0.0905493);
        RabbitMQConnection mqConnection = new RabbitMQConnection("admin", "admin", "10.221.247.50", 5672);
        Connection connection = mqConnection.createConnection();
        Channel channel = connection.createChannel();
        channel.basicPublish("E_SUYAN_SCHEDULE", "R_REPLY_DRIVE", new AMQP.BasicProperties.Builder().headers(headers).build(), "123".getBytes());
        connection.close();
    }

    public Connection createConnection() {
        try {
            return factory.newConnection();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (TimeoutException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public void deleteConnection(Connection connection) {
        try {
            connection.close();
        } catch (IOException e) {
            logger.error("can not close the connection, exception: {}.", e.getMessage());
        }
    }

    public Channel getChannel(Connection connection, String exchangeName, String routingKey) throws IOException {
        Channel channel = connection.createChannel();
        return channel;
    }
}
