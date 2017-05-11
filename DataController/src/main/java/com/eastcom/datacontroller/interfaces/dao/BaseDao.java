package com.eastcom.datacontroller.interfaces.dao;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by linghang.kong on 2016/12/20.
 * add the option of field level
 */
public interface BaseDao<T> {

    // 根据ID加载实体
    T get(Class<T> entityClazz, Serializable id);

    // 保存实体
    Serializable save(T entity);

    // 更新实体
    void update(T entity);

    // 删除实体
    void delete(T entity) throws IOException;

    // 根据ID删除实体
    void delete(Class<T> entityClazz, Serializable id);

    // 获取所有实体
    List<T> findAll(Class<T> entityClazz);

    // 获取实体总数
    long findCount(Class<T> entityClazz);

}
