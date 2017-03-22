package com.eastcom.aggregator.interfaces.service;

/**
 * Created by linghang.kong on 2017/3/9.
 */
public interface MessageService {

    /**
     * MQ message head class.
     */
    class Header {
        public static final String timestamp = "timestamp";
        public static final String priority = "priority";
        public static final String delivery_mode = "delivery_mode";
        public static final String headers = "headers";
        public static final String taskId = "taskId";
        public static final String startTime = "startTime";
        public static final String endTime = "endTime";
        public static final String jobType = "jobType";
        public static final String jobName = "jobName";
        public static final String content_encoding = "content_encoding";
        public static final String content_type = "content_type";
    }

    class TaskType {
        public static final int CREATE_TABLE_HBASE = 101;
        public static final int DELETE_TABLE_HBASE = 111;
    }
}
