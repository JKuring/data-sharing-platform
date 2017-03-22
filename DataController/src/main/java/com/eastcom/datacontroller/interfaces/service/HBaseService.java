package com.eastcom.datacontroller.interfaces.service;

import java.io.IOException;

/**
 * Created by linghang.kong on 2016/12/23.
 */
public interface HBaseService<T> extends BaseService<String> {

    public void createTable(T hBaseEntity) throws IOException;

    public void delete(T hBaseEntity);

    public boolean partition(T hBaseEntity);

}
