package com.eastcom.dataloader.bean;

/**
 * Created by linghang.kong on 2017/3/17.
 */
public class SparkJobs {

    private String name;
    private String confFile;
    private String initCmdPath;
    private String tplPath;
    private String zookeeper_hosts;
    private String zookeeper_port;
    private String timeid;

    public SparkJobs() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfFile() {
        return confFile;
    }

    public void setConfFile(String confFile) {
        this.confFile = confFile;
    }

    public String getInitCmdPath() {
        return initCmdPath;
    }

    public void setInitCmdPath(String initCmdPath) {
        this.initCmdPath = initCmdPath;
    }

    public String getTplPath() {
        return tplPath;
    }

    public void setTplPath(String tplPath) {
        this.tplPath = tplPath;
    }

    public String getZookeeper_hosts() {
        return zookeeper_hosts;
    }

    public void setZookeeper_hosts(String zookeeper_hosts) {
        this.zookeeper_hosts = zookeeper_hosts;
    }

    public String getZookeeper_port() {
        return zookeeper_port;
    }

    public void setZookeeper_port(String zookeeper_port) {
        this.zookeeper_port = zookeeper_port;
    }

    public String getTimeid() {
        return timeid;
    }

    public void setTimeid(String timeid) {
        this.timeid = timeid;
    }

    public String[] getParameters() {
        return new String[]{confFile, initCmdPath, tplPath, zookeeper_hosts, zookeeper_port, timeid};
    }
}
