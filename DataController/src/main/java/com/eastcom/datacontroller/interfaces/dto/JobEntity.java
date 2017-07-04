package com.eastcom.datacontroller.interfaces.dto;

import org.apache.hadoop.conf.Configuration;

import java.util.Map;

/**
 * Created by linghang.kong on 2016/12/27.
 */
public interface JobEntity<T> extends BaseEntity<String> {

    String getId();

    void setId(String id);

    String getJobName();

    void setJobName(String jobName);

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

    int getPreDays();

    void setPreDays(int preDays);

    String getDataPath();

    void setDataPath(String dataPath);

    String getGranularity();

    void setGranularity(String granularity);

    int getDelay();

    void setDelay(int delay);

    Map<String, String> getPropertiesMap();

    void setPropertiesMap(Map<String, String> propertiesMap);

    /**
     * add system parameter to the {@link Configuration}
     *
     * @param configuration
     */
    void addSystemProperties(Configuration configuration);
}
