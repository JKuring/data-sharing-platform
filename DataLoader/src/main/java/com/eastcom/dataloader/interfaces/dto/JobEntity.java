package com.eastcom.dataloader.interfaces.dto;

import org.apache.hadoop.conf.Configuration;

import java.util.Map;

/**
 * Created by linghang.kong on 2016/12/27.
 */
public interface JobEntity<T> extends BaseEntity {

    public String getId();

    public void setId(String id);

    public long getCreateTime();

    public void setCreateTime(long createTime);

    public long getStartTime();

    public void setStartTime(long startTime);

    public boolean isStatus();

    public void setStatus(boolean status);

    public T getTableEntity();

    public void setTableEntity(T tableEntity);

    public long getStopTime();

    public void setStopTime(long stopTime);

    public long getJobStartTime();

    public void setJobStartTime(long jobStartTime);

    public long getJobEndTime();

    public void setJobEndTime(long jobEndTime);

    public String getDataPath();

    public void setDataPath(String dataPath);

    public String getGranularity();

    public void setGranularity(String granularity);

    public int getDelay();

    public void setDelay(int delay);

    public Map<String, String> getPropertiesMap();

    public void setPropertiesMap(Map<String, String> propertiesMap);

    public void addSystemProperties(Configuration configuration);
}
