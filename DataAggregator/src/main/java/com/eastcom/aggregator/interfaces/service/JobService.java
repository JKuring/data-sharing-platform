package com.eastcom.aggregator.interfaces.service;

/**
 * Created by linghang.kong on 2016/12/27.
 */
public interface JobService<T> extends BaseService<String> {

    /**
     * executor
     *
     * @param message
     */
    void excute(T message);


}
