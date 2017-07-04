package com.eastcom.datacontroller.interfaces.service;

import java.io.IOException;

/**
 * Created by linghang.kong on 2017/7/4.
 */
public interface HiveService<T> extends BaseService<String>{

    void delete(T entity) throws IOException;
}
