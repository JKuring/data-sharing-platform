package com.eastcom.dataloader.interfaces.service;

/**
 * Created by linghang.kong on 2016/12/27.
 */
public interface JobService<T> extends BaseService<String> {


    void excute(T message);

}
