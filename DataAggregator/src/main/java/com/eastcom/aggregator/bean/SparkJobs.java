package com.eastcom.aggregator.bean;

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
    private String sessions;
    private String timeid;
    private String appIdDir;
    private String timeout;

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

    public String getSessions() {
        return sessions;
    }

    public void setSessions(String sessions) {
        this.sessions = sessions;
    }

    public String getTimeid() {
        return timeid;
    }

    public void setTimeid(String timeid) {
        this.timeid = timeid;
    }

    public String getAppIdDir() {
        return appIdDir;
    }

    public void setAppIdDir(String appIdDir) {
        this.appIdDir = appIdDir;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String[] getParameters() {
        return new String[]{confFile,initCmdPath,tplPath,zookeeper_hosts,zookeeper_port,sessions,timeid,appIdDir,timeout};
    }
}
