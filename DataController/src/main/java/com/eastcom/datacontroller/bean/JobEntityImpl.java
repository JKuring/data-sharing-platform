package com.eastcom.datacontroller.bean;


import com.eastcom.datacontroller.interfaces.dto.HBaseEntity;
import com.eastcom.datacontroller.interfaces.dto.JobEntity;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linghang.kong on 2016/12/27.
 */
@Component
@Scope("prototype")
public class JobEntityImpl implements JobEntity<HBaseEntity> {

    private static final Logger logger = LoggerFactory.getLogger(JobEntityImpl.class);

    private String id;
    private String jobName;
    private long createTime;
    private long startTime;
    private long stopTime;
    private long jobStartTime;
    private long jobEndTime;
    private int preDays;
    private boolean status = false;

    private String dataPath;
    private String granularity;
    private int delay;
    private Map<String, String> propertiesMap = new HashMap<String, String>();

    private HBaseEntity tableEntity;

    public JobEntityImpl(String jobName, HBaseEntity tableEntity) {
        this.jobName = jobName;
        this.tableEntity = tableEntity;
    }


    public HBaseEntity getTableEntity() {
        return tableEntity;
    }

    public synchronized void setTableEntity(HBaseEntity tableEntity) {
        this.tableEntity = tableEntity;
    }

    public String getId() {
        return this.id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public synchronized void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public synchronized void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public long getJobStartTime() {
        return jobStartTime;
    }

    public synchronized void setJobStartTime(long jobStartTime) {
        this.jobStartTime = jobStartTime;
    }

    public long getJobEndTime() {
        return jobEndTime;
    }

    public synchronized void setJobEndTime(long jobEndTime) {
        this.jobEndTime = jobEndTime;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getPreDays() {
        return preDays;
    }

    public void setPreDays(int preDays) {
        this.preDays = preDays;
    }

    @Override
    public String getDataPath() {
        return dataPath;
    }

    @Override
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    @Override
    public String getGranularity() {
        return granularity;
    }

    @Override
    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    @Override
    public void setPropertiesMap(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

    public String getName() {
        return this.jobName;
    }

    public synchronized void setName(String name) {
        this.jobName = name;
    }

    public boolean isStatus() {
        return status;
    }

    public synchronized void setStatus(boolean status) {
        this.status = status;
    }


    public void addSystemProperties(Configuration configuration) {
        for (String key : propertiesMap.keySet()
                ) {
            configuration.set(key, propertiesMap.get(key));
        }
    }
}
