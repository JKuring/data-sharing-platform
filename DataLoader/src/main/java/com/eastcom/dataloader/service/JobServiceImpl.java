package com.eastcom.dataloader.service;

import com.eastcom.common.bean.SparkProperties;
import com.eastcom.common.bean.TaskType;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.CommonMeaageProducer;
import com.eastcom.common.service.HttpRequestUtils;
import com.eastcom.common.utils.MergeArrays;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.common.utils.time.TimeTransform;
import com.eastcom.dataloader.bean.HBaseJobs;
import com.eastcom.dataloader.bean.JobEntityImpl;
import com.eastcom.dataloader.bean.SparkJobs;
import com.eastcom.dataloader.interfaces.dto.JobEntity;
import com.eastcom.dataloader.interfaces.service.HBaseService;
import com.eastcom.dataloader.interfaces.service.JobService;
import org.apache.spark.deploy.SparkSubmit$;
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

    private final TaskType taskType;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final HBaseService<JobEntity> hbaseService;

    private final SparkProperties sparkProperties;

    private Map<String, RabbitTemplate> mqProducer = CommonMeaageProducer.producerCollection;

    @Autowired
    private RabbitTemplate q_load;
//
//    @Resource(name = "confService")
//    private
//    String confService;

    // back head
    private String startTime = "startTime";
    private String endTime = "endTime";
    private String status = "status";

    // service
    private static final String LOAD_TABLE_HBASE = "LOAD_TABLE_HBASE";
    private static final String LOAD_TABLE_SPARK = "LOAD_TABLE_SPARK";

    @Autowired
    public JobServiceImpl(TaskType taskType, ThreadPoolTaskExecutor threadPoolTaskExecutor, HBaseService<JobEntity> hbaseService, SparkProperties sparkProperties) {
        this.taskType = taskType;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.hbaseService = hbaseService;
        this.sparkProperties = sparkProperties;
    }


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
                        doHBaseLoadDataJob(message);
                        break;
                    case LOAD_TABLE_SPARK:
                        doSparkLoadDataJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            q_load.send(new Message(("execute the task: " + jobType + ", exception: " + e.getMessage()).getBytes(), getMessageProperties(messageProperties, 1)));
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
                HBaseJobs hBaseJobs = JsonParser.parseJsonToObject(context.getBytes(), HBaseJobs.class);
                assert hBaseJobs != null: "Can't find the job!";
                String jobName = hBaseJobs.getName();
                for (String name : jobName.split("\\|")
                        ) {
                    // http
                    final JobEntity jobEntity = HttpRequestUtils.httpGet(name, JobEntityImpl.class);
                    // ioc
//                    final JobEntity jobEntity = (JobEntity) Loader.applicationContext.getBean(jobName);
                    jobEntity.setJobStartTime(System.currentTimeMillis());
                    jobEntity.setCreateTime(TimeTransform.getTimestamp(hBaseJobs.getTime()));
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
                }
            } else {
                throw new Exception("Unable task!");
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
    }

    public void doSparkLoadDataJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                final SparkJobs sparkJobs = JsonParser.parseJsonToObject(context.getBytes(), SparkJobs.class);
                logger.info("the name of loaded job: {}.", sparkJobs.getTplPath());

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
                                SparkSubmit$.MODULE$.main(MergeArrays.merge(sparkProperties.toStingArray(), sparkJobs.getParameters()));
                            } catch (Exception e) {
                                logger.error("Failed to load table, Exception: {}.", e.getMessage());
                                result = 1;
                            } finally {
                                q_load.send(new Message(("Finish loading task: " + taskId + ", application id: " + appId).getBytes(), getMessageProperties(messageProperties, result)));
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.error("Thread pool: {}.", e.getMessage());
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
