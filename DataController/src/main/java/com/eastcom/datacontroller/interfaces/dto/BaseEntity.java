package com.eastcom.datacontroller.interfaces.dto;

/**
 * Created by linghang.kong on 2016/12/21.
 */
public interface BaseEntity<T> {
    /**
     * set entity name;
     *
     * @return null
     */
    T getName();

    /**
     * get entity name;
     *
     * @param name entity name
     */
    void setName(T name);
}
