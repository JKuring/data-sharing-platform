package com.eastcom.datacontroller.interfaces.service;

import org.apache.hadoop.fs.Path;

/**
 * Created by linghang.kong on 2017/5/9.
 */
public interface HDFSService {

    /**
     * create dir by relative path
     *
     * @param path Need to create relative path
     */
    public void createDir(Path path);

    /**
     * delete dir by relative path
     *
     * @param path Need to delete relative path
     */
    public void deleteDir(Path path);

}
