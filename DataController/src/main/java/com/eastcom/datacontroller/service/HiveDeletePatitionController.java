package com.eastcom.datacontroller.service;

import com.eastcom.common.interfaces.service.Executor;
import org.springframework.amqp.core.Message;

/**
 * Created by linghang.kong on 2017/6/26.
 */
public class HiveDeletePatitionController implements Executor<Message> {
    @Override
    public void doJob(Message message) {

    }
}
