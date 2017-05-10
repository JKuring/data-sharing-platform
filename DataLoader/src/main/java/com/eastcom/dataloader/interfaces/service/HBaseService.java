package com.eastcom.dataloader.interfaces.service;

/**
 * Created by linghang.kong on 2016/12/23.
 */
public interface HBaseService<T> extends BaseService<String> {

    void createTable(T hBaseEntity);

    void delete(T hBaseEntity);

    boolean createSchedulerJob(T jobEntity);

    boolean partition(T hBaseEntity);

}
