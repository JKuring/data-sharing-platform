package com.eastcom.common.interfaces.service;


/**
 * Created by linghang.kong on 2017/4/1.
 */
public interface Executor<T> {

    /**
     * execute job
     *
     * @param message mq message
     */
    public void doJob(T message);
}
