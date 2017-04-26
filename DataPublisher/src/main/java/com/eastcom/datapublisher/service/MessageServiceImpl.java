package com.eastcom.datapublisher.service;

import com.eastcom.datapublisher.interfaces.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by linghang.kong on 2017/4/26.
 */
public class MessageServiceImpl implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private JobService<Message> jobService;


    @Override
    public void onMessage(Message message) {
        jobService.excute(message);
    }
}
