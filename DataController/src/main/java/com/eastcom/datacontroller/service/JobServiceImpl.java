package com.eastcom.datacontroller.service;


import com.eastcom.common.bean.TaskType;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.datacontroller.interfaces.service.JobService;
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

    private static final String DELETE_DIR_HDFS = "DELETE_DIR_HDFS";
    private static final String CREATE_TABLE_HBASE = "CREATE_TABLE_HBASE";
    private static final String DELETE_TABLE_HBASE = "DELETE_TABLE_HBASE";
    private static final String DELETE_TABLE_HIVE = "DELETE_TABLE_HIVE";


    @Autowired
    private TaskType taskType;

    @Autowired
    private RabbitTemplate q_maint;

    @Resource(name = "DELETE_DIR_HDFS")
    private Executor delete_dir_hdfs;

    @Resource(name = "CREATE_TABLE_HBASE")
    private Executor create_table_hbase;

    @Resource(name = "DELETE_TABLE_HBASE")
    private Executor delete_table_hbase;

    @Resource(name = "DELETE_TABLE_HIVE")
    private Executor delete_table_hive;


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
                    case DELETE_DIR_HDFS:
                        delete_dir_hdfs.doJob(message);
                        break;
                    case CREATE_TABLE_HBASE:
                        create_table_hbase.doJob(message);
                        break;
                    case DELETE_TABLE_HBASE:
                        delete_table_hbase.doJob(message);
                        break;
                    case DELETE_TABLE_HIVE:
                        delete_table_hive.doJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            SendMessageUtility.send(q_maint, "execute the task: " + jobType + ", exception: " + e.getMessage(), messageProperties, Executor.FAILED);
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
