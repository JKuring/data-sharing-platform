package com.eastcom.dataloader.service;

import com.eastcom.common.bean.TaskType;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.dataloader.interfaces.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private static final String LOAD_TABLE_HBASE = "LOAD_TABLE_HBASE";
    private static final String LOAD_TABLE_SPARK = "LOAD_TABLE_SPARK";


    @Autowired
    private TaskType taskType;

    @Autowired
    private RabbitTemplate q_load;

    @Resource(name = "LOAD_TABLE_HBASE")
    private Executor load_table_hbase;

    @Resource(name = "LOAD_TABLE_SPARK")
    private Executor load_table_spark;

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
                    case LOAD_TABLE_HBASE:
                        load_table_hbase.doJob(message);
                        break;
                    case LOAD_TABLE_SPARK:
                        load_table_spark.doJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            SendMessageUtility.send(q_load, "execute the task: " + jobType + ", exception: " + e.getMessage(), messageProperties, Executor.FAILED);
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
