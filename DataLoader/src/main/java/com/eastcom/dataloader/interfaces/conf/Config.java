package com.eastcom.dataloader.interfaces.conf;

/**
 * Created by linghang.kong on 2016/12/19.
 */
public interface Config<T> {
    /**
     * Get a configuration by kerberos
     *
     * @return
     */
    public T getConfiguration(T t);
}
