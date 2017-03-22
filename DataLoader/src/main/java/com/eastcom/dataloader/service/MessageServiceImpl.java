package com.eastcom.dataloader.service;


import com.eastcom.common.message.CommonMeaageProducer;
import com.eastcom.dataloader.interfaces.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by linghang.kong on 2017/3/9.
 */
public class MessageServiceImpl implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private JobService<Message> jobService;

    @Autowired
    private RabbitTemplate q_load;

    @Autowired
    private void iniMQProducer(){
        CommonMeaageProducer.producerCollection.put(q_load.getClass().getName(),q_load);
    }

    @Override
    public void onMessage(Message message) {
        jobService.excute(message);
    }
}
