package com.eastcom.datacontroller.interfaces.service;

import java.io.Closeable;

/**
 * Created by linghang.kong on 2016/12/23.
 */
public interface BaseService<T> extends Closeable {
    T getName();

    void setName();
}
