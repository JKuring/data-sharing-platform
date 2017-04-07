package com.eastcom.datacontroller.interfaces.service;

/**
 * Created by linghang.kong on 2016/12/27.
 */
public interface JobService<T> extends BaseService<String> {


    void excute(T message);

//    /**
//     * create HBase table
//     *
//     * @param message
//     */
//    void doHBaseCreateTableJob(T message);
//
//    /**
//     * delete HBase table
//     *
//     * @param message
//     */
//    void doHBaseDeleteTableJob(T message);

}
