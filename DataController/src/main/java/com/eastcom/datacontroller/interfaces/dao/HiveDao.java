package com.eastcom.datacontroller.interfaces.dao;

import java.io.Closeable;
import java.util.List;

/**
 * Created by linghang.kong on 2017/6/27.
 */
public interface HiveDao<T> extends BaseDao<T>, Closeable {


    public void drop(T t);

    public List<String> getPartitions(T t);

}
