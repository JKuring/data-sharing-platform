package com.eastcom.aggregator.interfaces.service;

/**
 * Created by linghang.kong on 2016/12/27.
 */
public interface JobService<T> extends BaseService<String> {


    /**
     * executor
     * @param message
     */
    public void excute(T message);

    /**
     * aggregate hive data to hive by spark sql
     *
     * @param message
     */
    public void doSparkAggregationJob(T message);


}
