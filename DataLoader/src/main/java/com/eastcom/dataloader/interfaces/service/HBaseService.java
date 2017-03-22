package com.eastcom.dataloader.interfaces.service;

/**
 * Created by linghang.kong on 2016/12/23.
 */
public interface HBaseService<T> extends BaseService<String> {

    public void createTable(T hBaseEntity);

    public void delete(T hBaseEntity);

    public boolean createSchedulerJob(T jobEntity);

    public boolean partition(T hBaseEntity);

}
