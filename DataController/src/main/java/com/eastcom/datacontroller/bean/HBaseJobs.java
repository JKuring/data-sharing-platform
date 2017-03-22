package com.eastcom.datacontroller.bean;

import java.io.File;

/**
 * Created by linghang.kong on 2017/3/9.
 */
public class HBaseJobs {

    private String name;
    private String time;
    private String[] columns;
    private int version;
    private String compressionType;
    private int ttl;
    private String splitPolicy;
    private File spiltKeysFile;
    private String coprocessor;
    private int preDays = 3;
    private String granularity;

    private boolean currentIsCreated = false;


    public HBaseJobs() {
    }

    public String[] getName() {
        return this.name.split(",");
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

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public String getSplitPolicy() {
        return splitPolicy;
    }

    public void setSplitPolicy(String splitPolicy) {
        this.splitPolicy = splitPolicy;
    }

    public File getSpiltKeysFile() {
        return spiltKeysFile;
    }

    public void setSpiltKeysFile(File spiltKeysFile) {
        this.spiltKeysFile = spiltKeysFile;
    }

    public String getCoprocessor() {
        return coprocessor;
    }

    public void setCoprocessor(String coprocessor) {
        this.coprocessor = coprocessor;
    }

    public boolean isCurrentIsCreated() {
        return currentIsCreated;
    }

    public void setCurrentIsCreated(boolean currentIsCreated) {
        this.currentIsCreated = currentIsCreated;
    }

    public int getPreDays() {
        return preDays;
    }

    public void setPreDays(int preDays) {
        this.preDays = preDays;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

}
