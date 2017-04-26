package com.eastcom.datapublisher.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;

/**
 * Created by linghang.kong on 2017/4/26.
 */
public class HivePublishFTP implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(HivePublishFTP.class);

    @Override
    public void doJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {

        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }

    }
}
