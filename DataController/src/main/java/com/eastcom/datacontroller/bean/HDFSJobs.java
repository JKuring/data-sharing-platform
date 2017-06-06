package com.eastcom.datacontroller.bean;

import java.util.List;

/**
 * Created by linghang.kong on 2017/5/11.
 */
public class HDFSJobs {

    private int path_num = 0;

    private List<String> paths;

    public HDFSJobs() {
    }

    public int getPath_num() {
        return path_num;
    }

    public void setPath_num(int path_num) {
        this.path_num = path_num;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
