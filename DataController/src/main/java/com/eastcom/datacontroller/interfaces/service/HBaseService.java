package com.eastcom.datacontroller.interfaces.service;

import java.io.IOException;

/**
 * Created by linghang.kong on 2016/12/23.
 */
public interface HBaseService<T> extends BaseService<String> {

    void createTable(T hBaseEntity) throws IOException;

    void delete(T hBaseEntity);

    boolean partition(T hBaseEntity);

}
