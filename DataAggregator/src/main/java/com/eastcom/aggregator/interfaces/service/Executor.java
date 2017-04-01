package com.eastcom.aggregator.interfaces.service;


/**
 * Created by linghang.kong on 2017/4/1.
 */
public interface Executor<T> {

    public void doJob(T message);
}
