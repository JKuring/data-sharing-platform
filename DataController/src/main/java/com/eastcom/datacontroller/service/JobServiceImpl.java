package com.eastcom.datacontroller.service;


import com.eastcom.common.bean.TaskType;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.common.utils.time.TimeTransform;
import com.eastcom.datacontroller.bean.HBaseEntityImpl;
import com.eastcom.datacontroller.bean.HBaseJobs;
import com.eastcom.datacontroller.bean.JobEntityImpl;
import com.eastcom.datacontroller.interfaces.dto.HBaseEntity;
import com.eastcom.datacontroller.interfaces.dto.JobEntity;
import com.eastcom.datacontroller.interfaces.service.HBaseService;
import com.eastcom.datacontroller.interfaces.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Created by linghang.kong on 2017/3/10.
 */
@Service
public class JobServiceImpl implements JobService<Message> {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    @Autowired
    private TaskType taskType;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private HBaseService<JobEntity> hbaseService;

    @Autowired
    private RabbitTemplate q_maint;

    private static final String CREATE_TABLE_HBASE = "CREATE_TABLE_HBASE";
    private static final String DELETE_TABLE_HBASE = "DELETE_TABLE_HBASE";


    // back head
    private String startTime = "startTime";
    private String endTime = "endTime";
    private String status = "status";


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
                    case CREATE_TABLE_HBASE:
                        doHBaseCreateTableJob(message);
                        break;
                    case DELETE_TABLE_HBASE:
                        doHBaseDeleteTableJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            messageProperties.setHeader(status, 1);
            q_maint.send(new Message(("execute the task: " + jobType + ", exception: " + e.getMessage()).getBytes(), message.getMessageProperties()));
        }
    }

    public void doHBaseCreateTableJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                HBaseJobs hBaseJobs = JsonParser.parseJsonToObject(context.getBytes(), HBaseJobs.class);
                for (String tableName : hBaseJobs.getName()
                        ) {
                    final JobEntity jobEntity = new JobEntityImpl((String) headMap.get(MessageService.Header.jobName), getHBaseEntity(tableName, hBaseJobs));
                    jobEntity.setJobStartTime(System.currentTimeMillis());
                    jobEntity.setCreateTime(TimeTransform.getTimestamp(hBaseJobs.getTime()));
                    jobEntity.setPreDays(hBaseJobs.getPreDays());
                    jobEntity.setGranularity(hBaseJobs.getGranularity());
                    logger.info("create the table: {}.", tableName);
                    try {
                        threadPoolTaskExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                logger.debug("start the thread: {}.", Thread.currentThread().getName());
                                int result = 2;
                                messageProperties.setHeader(startTime, System.currentTimeMillis());
                                try {
                                    hbaseService.createTable(jobEntity);
                                    //关闭任务
                                    jobEntity.setJobEndTime(System.currentTimeMillis());
                                } catch (Exception e) {
                                    logger.error("Failed to create table, Exception: {}.", e.getMessage());
                                    result = 1;
                                } finally {
                                    q_maint.send(new Message(("Finish creating task: " + jobEntity.getJobName()).getBytes(), getMessageProperties(messageProperties, result)));
                                }
                            }
                        });
                    } catch (Exception e) {
                        logger.debug("Thread pool: {}.", e.getMessage());
                    }
                }
            } else {
                throw new Exception("Unable task!");
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
    }

    public void doHBaseDeleteTableJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                HBaseJobs hBaseJobs = JsonParser.parseJsonToObject(context.getBytes(), HBaseJobs.class);
                for (String tableName : hBaseJobs.getName()
                        ) {
                    final JobEntityImpl jobEntity = new JobEntityImpl((String) headMap.get(MessageService.Header.jobName), getHBaseEntity(tableName, hBaseJobs));
                    jobEntity.setGranularity(hBaseJobs.getGranularity());
                    logger.info("delete the table: {}.", tableName);
                    threadPoolTaskExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("start the thread: {}.", Thread.currentThread().getName());
                            int result = 2;
                            messageProperties.setHeader(startTime, System.currentTimeMillis());
                            try {
                                hbaseService.delete(jobEntity);
                                //关闭任务
                                jobEntity.setJobEndTime(System.currentTimeMillis());
                            } catch (Exception e) {
                                logger.error("Failed to delete table, Exception: {}.", e.getMessage());
                                result = 1;
                            } finally {
                                q_maint.send(new Message(("Finish to delete task: " + jobEntity.getJobName()).getBytes(), getMessageProperties(messageProperties, result)));
                            }
                        }
                    });
                }
            } else {
                throw new Exception("Unable task!");
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
    }

    private HBaseEntity getHBaseEntity(String tableName, HBaseJobs hbaseJobs) {
        HBaseEntity hbaseEntity = new HBaseEntityImpl();
        hbaseEntity.setName(tableName);
        hbaseEntity.setColumns(hbaseJobs.getColumns());
        hbaseEntity.setVersion(hbaseJobs.getVersion());
        hbaseEntity.setCompressionType(hbaseJobs.getCompressionType());
        hbaseEntity.setTtl(hbaseJobs.getTtl());
        hbaseEntity.setSplitPolicy(hbaseJobs.getSplitPolicy());
        hbaseEntity.setSpiltKeysFile(hbaseJobs.getSpiltKeysFile());
        hbaseEntity.setCoprocessor(hbaseJobs.getCoprocessor());
        logger.debug("HBaseEntity: {}.", hbaseEntity.toString());
        return hbaseEntity;
    }

    private MessageProperties getMessageProperties(MessageProperties messageProperties, int result) {
        messageProperties.setHeader(endTime, System.currentTimeMillis());
        messageProperties.setHeader(status, result);
        return messageProperties;
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
