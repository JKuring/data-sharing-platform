package com.eastcom.dataloader.interfaces.dto;

import org.apache.hadoop.conf.Configuration;

import java.util.Map;

/**
 * Created by linghang.kong on 2016/12/27.
 */
public interface JobEntity<T> extends BaseEntity {

    String getId();

    void setId(String id);

    long getCreateTime();

    void setCreateTime(long createTime);

    long getStartTime();

    void setStartTime(long startTime);

    boolean isStatus();

    void setStatus(boolean status);

    T getTableEntity();

    void setTableEntity(T tableEntity);

    long getStopTime();

    void setStopTime(long stopTime);

    long getJobStartTime();

    void setJobStartTime(long jobStartTime);

    long getJobEndTime();

    void setJobEndTime(long jobEndTime);

    String getDataPath();

    void setDataPath(String dataPath);

    String getGranularity();

    void setGranularity(String granularity);

    int getDelay();

    void setDelay(int delay);

    Map<String, String> getPropertiesMap();

    void setPropertiesMap(Map<String, String> propertiesMap);

    void addSystemProperties(Configuration configuration);
}
