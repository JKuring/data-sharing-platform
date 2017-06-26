package com.eastcom.datacontroller.bean;

import java.util.List;

/**
 * Created by linghang.kong on 2017/5/11.
 */
public class HDFSJobs {

    private String timeId;

    private int path_num = 0;

    private List<String> paths;

    private String timePathFormat;

    private boolean df = false;

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

    public String getTimeId() {
        return timeId;
    }

    public void setTimeId(String timeId) {
        this.timeId = timeId;
    }

    public String getTimePathFormat() {
        return timePathFormat;
    }

    public void setTimePathFormat(String timePathFormat) {
        this.timePathFormat = timePathFormat;
    }

    public boolean isDf() {
        return df;
    }

    public void setDf(boolean df) {
        this.df = df;
    }
}
