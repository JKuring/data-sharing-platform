package com.eastcom.datacontroller.interfaces.dao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by linghang.kong on 2017/5/11.
 */
public interface HDFSDao<T> extends BaseDao<T>, Closeable {

    /**
     *
     * @return
     */
    public Configuration getConfiguration();

    /**
     *
     * @return
     */
    public FileSystem getFileSystem();

    /**
     * create dir by relative path
     * @param path Need to create relative path
     */
    public void create(T path) throws IOException;

    /**
     * delete dir by relative path
     * @param path Need to delete relative path
     */
    public void delete(T path) throws IOException;
}
