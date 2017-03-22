package com.eastcom.dataloader.service;

import com.eastcom.Loader;
import com.eastcom.common.message.CommonMeaageProducer;
import com.eastcom.dataloader.bean.HBaseJobs;
import com.eastcom.dataloader.bean.SparkJobs;
import com.eastcom.dataloader.bean.SparkProperties;
import com.eastcom.dataloader.interfaces.dto.JobEntity;
import com.eastcom.dataloader.interfaces.service.HBaseService;
import com.eastcom.dataloader.interfaces.service.JobService;
import com.eastcom.dataloader.interfaces.service.MessageService;
import com.eastcom.dataloader.utils.parser.JsonParser;
import org.apache.spark.deploy.SparkSubmit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

    ApplicationContext applicationContext = Loader.applicationContext;

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private HBaseService<JobEntity> hbaseService;

    @Autowired
    private SparkProperties sparkProperties;

    private Map<String, RabbitTemplate> mqProducer = CommonMeaageProducer.producerCollection;

    private RabbitTemplate q_load = mqProducer.get("q_load");

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
            switch (jobType) {
                case MessageService.TaskType.LOAD_TABLE_HBASE:
                    doHBaseLoadDataJob(message);
                    break;
                case MessageService.TaskType.LOAD_TABLE_SPARK:
                    doSparkLoadDataJob(message);
                    break;
                default:
                    throw new Exception("invalid task type!");
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            messageProperties.setHeader(status, 1);
            q_load.send(new Message(("execute the task: "+jobType+", exception: "+e.getMessage()).getBytes(),message.getMessageProperties()));
        }
    }

    public void doHBaseCreateTableJob(Message message) {
    }

    public void doHBaseDeleteTableJob(Message message) {

    }

    @Override
    public void doHBaseLoadDataJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                HBaseJobs hBaseJobs = jsonParser.parseJsonToObject(context.getBytes(), HBaseJobs.class);
                String jobName = hBaseJobs.getName();
                // ioc
                final JobEntity jobEntity = (JobEntity) applicationContext.getBean(hBaseJobs.getName());
                jobEntity.setJobStartTime(System.currentTimeMillis());
                jobEntity.setCreateTime(Long.parseLong(hBaseJobs.getTime()));
                logger.info("the loading job name: {}.", jobName);
                try {
                    threadPoolTaskExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("start the thread: {}.", Thread.currentThread().getName());
                            int result = 2;
                            messageProperties.setHeader(startTime, System.currentTimeMillis());
                            try {
                                hbaseService.partition(jobEntity);
                                //关闭任务
                                jobEntity.setJobEndTime(System.currentTimeMillis());
                            } catch (Exception e) {
                                logger.error("Failed to load table, Exception: {}.", e.getMessage());
                                result = 1;
                            } finally {
                                q_load.send(new Message(("Finish loading task: " + jobEntity.getId()).getBytes(), getMessageProperties(messageProperties, result)));
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.debug("Thread pool: {}.", e.getMessage());
                }
            } else {
                throw new Exception("Unable task!");
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
    }

    public void doSparkLoadDataJob(Message message){
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                final SparkJobs sparkJobs = jsonParser.parseJsonToObject(context.getBytes(), SparkJobs.class);
                logger.info("the loading job name: {}.", sparkJobs.getTplPath());
                try {
                    threadPoolTaskExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("start the thread: {}.", Thread.currentThread().getName());
                            int result = 2;
                            String appId = null;
                            messageProperties.setHeader(startTime, System.currentTimeMillis());
                            try {
                                // submit code to cluster
                                SparkSubmit.main(sparkProperties.toStingArray(sparkJobs.getParameters()));
                            } catch (Exception e) {
                                logger.error("Failed to load table, Exception: {}.", e.getMessage());
                                result = 1;
                            } finally {
                                q_load.send(new Message(("Finish loading task: " + taskId + ", application id: " + appId).getBytes(), getMessageProperties(messageProperties, result)));
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.debug("Thread pool: {}.", e.getMessage());
                }
            } else {
                throw new Exception("Unable task!");
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
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
