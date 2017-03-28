package com.eastcom.aggregator.service;


import com.eastcom.aggregator.interfaces.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by linghang.kong on 2017/3/9.
 */
public class MessageServiceImpl implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private JobService<Message> jobService;

//    @Autowired
//    private RabbitTemplate q_aggr_spark;
//
//    @Autowired
//    private void iniMQProducer(){
//        String mq_name = q_aggr_spark.getClass().getName();
//        CommonMeaageProducer.producerCollection.put(mq_name,q_aggr_spark);
//        logger.info("put the {} mq handle to map",mq_name);
//    }

    @Override
    public void onMessage(Message message) {
        jobService.excute(message);
    }
}
