package com.eastcom.common.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by linghang.kong on 2017/5/25.
 */
public class SendMessageUtility {

    private static final Logger logger = LoggerFactory.getLogger(SendMessageUtility.class);


    public static boolean send(RabbitTemplate rabbitTemplate, String message, MessageProperties messageProperties, int result) {
        boolean status = false;
        try {
            rabbitTemplate.send(new Message(message.getBytes(), MessageHead.getMessageProperties(messageProperties, result)));
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }


}
