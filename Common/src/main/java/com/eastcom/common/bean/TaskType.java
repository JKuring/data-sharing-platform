package com.eastcom.common.bean;

import java.util.Map;

/**
 * Created by linghang.kong on 2017/3/23.
 */
public class TaskType {
    private int TaskNum;
    private Map<Integer,String> TaskTypesMap;

    public int getTaskNum() {
        return TaskNum;
    }

    public void setTaskNum(int taskNum) {
        TaskNum = taskNum;
    }

    public Map<Integer, String> getTaskTypesMap() {
        return TaskTypesMap;
    }

    public void setTaskTypesMap(Map<Integer, String> taskTypesMap) {
        TaskTypesMap = taskTypesMap;
    }
}
