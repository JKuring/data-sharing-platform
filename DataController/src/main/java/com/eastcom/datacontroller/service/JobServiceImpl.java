package com.eastcom.datacontroller.service;


import com.eastcom.common.bean.TaskType;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
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
                    case DELETE_DIR_HDFS:
                        delete_dir_hdfs.doJob(message);
                        break;
                    case CREATE_TABLE_HBASE:
                        create_table_hbase.doJob(message);
                        break;
                    case DELETE_TABLE_HBASE:
                        delete_table_hbase.doJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            messageProperties.setHeader(status, 1);
            q_maint.send(new Message(("execute the task: " + jobType + ", exception: " + e.getMessage()).getBytes(), getMessageProperties(messageProperties, Executor.FAILED)));
        }
    }

    //    public void doHBaseCreateTableJob(Message message) {
//        final MessageProperties messageProperties = message.getMessageProperties();
//        Map<String, Object> headMap = messageProperties.getHeaders();
//        String taskId = (String) headMap.get(MessageService.Header.taskId);
//        String context = new String(message.getBody());
//        try {
//            if (taskId != null) {
//                logger.info("start the task: {}.", taskId);
//                HBaseJobs hBaseJobs = JsonParser.parseJsonToObject(context.getBytes(), HBaseJobs.class);
//                for (String tableName : hBaseJobs.getName()
//                        ) {
//                    final JobEntity jobEntity = new JobEntityImpl((String) headMap.get(MessageService.Header.jobName), getHBaseEntity(tableName, hBaseJobs));
//                    jobEntity.setJobStartTime(System.currentTimeMillis());
//                    jobEntity.setCreateTime(TimeTransform.getTimestamp(hBaseJobs.getTime()));
//                    jobEntity.setPreDays(hBaseJobs.getPreDays());
//                    jobEntity.setGranularity(hBaseJobs.getGranularity());
//                    logger.info("create the table: {}.", tableName);
//                    try {
//                        threadPoolTaskExecutor.execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                logger.debug("start the thread: {}.", Thread.currentThread().getName());
//                                int result = 2;
//                                messageProperties.setHeader(startTime, System.currentTimeMillis());
//                                try {
//                                    hbaseService.createTable(jobEntity);
//                                    //关闭任务
//                                    jobEntity.setJobEndTime(System.currentTimeMillis());
//                                } catch (Exception e) {
//                                    logger.error("Failed to create table, Exception: {}.", e.getMessage());
//                                    result = 1;
//                                } finally {
//                                    q_maint.send(new Message(("Finish creating task: " + jobEntity.getJobName()).getBytes(), getMessageProperties(messageProperties, result)));
//                                }
//                            }
//                        });
//                    } catch (Exception e) {
//                        logger.debug("Thread pool: {}.", e.getMessage());
//                    }
//                }
//            } else {
//                throw new Exception("Unable task!");
//            }
//        } catch (Exception e) {
//            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
//        }
//    }
//
//    public void doHBaseDeleteTableJob(Message message) {
//        final MessageProperties messageProperties = message.getMessageProperties();
//        Map<String, Object> headMap = messageProperties.getHeaders();
//        String taskId = (String) headMap.get(MessageService.Header.taskId);
//        String context = new String(message.getBody());
//        try {
//            if (taskId != null) {
//                logger.info("start the task: {}.", taskId);
//                HBaseJobs hBaseJobs = JsonParser.parseJsonToObject(context.getBytes(), HBaseJobs.class);
//                for (String tableName : hBaseJobs.getName()
//                        ) {
//                    final JobEntityImpl jobEntity = new JobEntityImpl((String) headMap.get(MessageService.Header.jobName), getHBaseEntity(tableName, hBaseJobs));
//                    jobEntity.setGranularity(hBaseJobs.getGranularity());
//                    logger.info("deleteTable the table: {}.", tableName);
//                    threadPoolTaskExecutor.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            logger.debug("start the thread: {}.", Thread.currentThread().getName());
//                            int result = 2;
//                            messageProperties.setHeader(startTime, System.currentTimeMillis());
//                            try {
//                                hbaseService.deleteTable(jobEntity);
//                                //关闭任务
//                                jobEntity.setJobEndTime(System.currentTimeMillis());
//                            } catch (Exception e) {
//                                logger.error("Failed to deleteTable table, Exception: {}.", e.getMessage());
//                                result = 1;
//                            } finally {
//                                q_maint.send(new Message(("Finish to deleteTable task: " + jobEntity.getJobName()).getBytes(), getMessageProperties(messageProperties, result)));
//                            }
//                        }
//                    });
//                }
//            } else {
//                throw new Exception("Unable task!");
//            }
//        } catch (Exception e) {
//            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
//        }
//    }
//
//    private HBaseEntity getHBaseEntity(String tableName, HBaseJobs hbaseJobs) {
//        HBaseEntity hbaseEntity = new HBaseEntityImpl();
//        hbaseEntity.setName(tableName);
//        hbaseEntity.setColumns(hbaseJobs.getColumns());
//        hbaseEntity.setVersion(hbaseJobs.getVersion());
//        hbaseEntity.setCompressionType(hbaseJobs.getCompressionType());
//        hbaseEntity.setTtl(hbaseJobs.getTtl());
//        hbaseEntity.setSplitPolicy(hbaseJobs.getSplitPolicy());
//        hbaseEntity.setSpiltKeysFile(hbaseJobs.getSpiltKeysFile());
//        hbaseEntity.setCoprocessor(hbaseJobs.getCoprocessor());
//        logger.debug(hbaseEntity.toString());
//        return hbaseEntity;
//    }
//
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
