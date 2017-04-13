package com.eastcom.common.interfaces.service;


/**
 * Created by linghang.kong on 2017/4/1.
 */
public interface Executor<T> {


    public static final int SUCESSED = 1;
    public static final int FAILED = 2;

    /**
     * execute job
     *
     * @param message mq message
     */
    public void doJob(T message);
}
