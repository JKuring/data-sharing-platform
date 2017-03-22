package com.eastcom.dataloader.bean;

/**
 * Created by linghang.kong on 2017/3/9.
 */
public class HBaseJobs {

    /**
     * job name,
     */
    private String name;
    private String time;


    public HBaseJobs() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
