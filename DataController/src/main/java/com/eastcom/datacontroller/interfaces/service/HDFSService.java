package com.eastcom.datacontroller.interfaces.service;

import org.apache.hadoop.fs.Path;

import java.util.Date;

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

    /**
     * delete dir by relative time path
     * @param rootPath Need to delete relative path
     * @param date Need to delete date
     * @param DF if the DF is true , the deletion include the content about previous time.
     */
    public void deleteByTime(Path rootPath, Date date, boolean DF, String timeFormat);

}
