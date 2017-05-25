package com.eastcom.common.message;

import org.springframework.amqp.core.MessageProperties;

/**
 * Created by linghang.kong on 2017/5/25.
 */
public class MessageHead {

    // back head
    public static final String startTime = "startTime";
    public static final String endTime = "endTime";
    public static final String status = "status";


    public static MessageProperties getMessageProperties(MessageProperties messageProperties, int result) {
        messageProperties.setHeader(endTime, System.currentTimeMillis());
        messageProperties.setHeader(status, result);
        return messageProperties;
    }
}
