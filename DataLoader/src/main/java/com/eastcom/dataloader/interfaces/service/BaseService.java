package com.eastcom.dataloader.interfaces.service;

import java.io.Closeable;

/**
 * Created by linghang.kong on 2016/12/23.
 */
public interface BaseService<T> extends Closeable {
    public T getName();

    public void setName();
}
