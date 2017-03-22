package com.eastcom.aggregator.bean;

import java.util.Map;

/**
 * Created by linghang.kong on 2017/3/21.
 */
public class SparkProperties {
    private String master;

    private Map<String,String> paropertiesMap;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public Map<String, String> getParopertiesMap() {
        return paropertiesMap;
    }

    public void setParopertiesMap(Map<String, String> paropertiesMap) {
        this.paropertiesMap = paropertiesMap;
    }
}
