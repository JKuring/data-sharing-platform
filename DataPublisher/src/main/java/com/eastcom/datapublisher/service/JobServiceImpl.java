package com.eastcom.datapublisher.service;

import com.eastcom.common.bean.TaskType;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.datapublisher.interfaces.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;


/**
 * Created by linghang.kong on 2017/3/10.
 */
@Service
public class JobServiceImpl implements JobService<Message> {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
    // service
    private static final String PUBLISH_HIVE_FTP = "PUBLISH_HIVE_FTP";
    private static final String PUBLISH_HIVE_HBASE = "PUBLISH_HIVE_HBASE";
    @Autowired
    private TaskType taskType;
    @Resource(name = "PUBLISH_HIVE_FTP")
    private Executor publishFtpExecutor;
    @Resource(name = "PUBLISH_HIVE_HBASE")
    private Executor publishHbaseExecutor;

    public void excute(Message message) {
        int jobType = 0;
        MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> header = messageProperties.getHeaders();
        try {
            jobType = (Integer) header.get(MessageService.Header.jobType);
            Map<Integer, String> taskTypesMap = taskType.getTaskTypesMap();
            if (taskTypesMap.containsKey(jobType)) {
                String taskType = taskTypesMap.get(jobType);
                switch (taskType) {
                    case PUBLISH_HIVE_FTP:
                        publishFtpExecutor.doJob(message);
                        break;
                    case PUBLISH_HIVE_HBASE:
                        publishHbaseExecutor.doJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName() {

    }

    @Override
    public void close() throws IOException {

    }
}
