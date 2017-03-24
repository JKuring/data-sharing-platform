package com.eastcom.aggregator.service;

import com.eastcom.aggregator.bean.MQConf;
import com.eastcom.aggregator.bean.SparkJobs;
import com.eastcom.aggregator.interfaces.service.JobService;
import com.eastcom.aggregator.interfaces.service.MessageService;
import com.eastcom.aggregator.utils.parser.JsonParser;
import com.eastcom.aggregator.utils.parser.MqHeadParser;
import com.eastcom.common.bean.SparkProperties;
import com.eastcom.common.bean.TaskType;
import com.eastcom.common.message.CommonMeaageProducer;
import com.eastcom.common.utils.MergeArrays;
import org.apache.spark.deploy.SparkSubmit;
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

    private final JsonParser jsonParser;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final SparkProperties sparkProperties;

    private final MQConf mqConf;

    private Map<String, RabbitTemplate> mqProducer = CommonMeaageProducer.producerCollection;

    private RabbitTemplate q_aggr_spark = mqProducer.get("q_aggr_spark");


    // back head
    private String startTime = "startTime";
    private String endTime = "endTime";
    private String status = "status";

    // task types
    private static final String AGGREGATE_SPARK = "AGGREGATE_SPARK";

    @Autowired
    public JobServiceImpl(TaskType taskType, JsonParser jsonParser, ThreadPoolTaskExecutor threadPoolTaskExecutor, SparkProperties sparkProperties, MQConf mqConf) {
        this.taskType = taskType;
        this.jsonParser = jsonParser;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.sparkProperties = sparkProperties;
        this.mqConf = mqConf;
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
                    case AGGREGATE_SPARK:
                        doSparkAggregationJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            q_aggr_spark.send(new Message(("execute the task: "+jobType+", exception: "+e.getMessage()).getBytes(),getMessageProperties(messageProperties, 1)));
        }
    }

    @Override
    public void doSparkAggregationJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                final SparkJobs sparkJobs = jsonParser.parseJsonToObject(context.getBytes(), SparkJobs.class);
                logger.info("the name of aggregated job: {}.", sparkJobs.getTplPath());
                try {
                    threadPoolTaskExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("start the thread: {}.", Thread.currentThread().getName());
                            int result = 2;
                            String appId = null;
                            messageProperties.setHeader(startTime, System.currentTimeMillis());
                            try {
                                SparkSubmit.main(MergeArrays.merge(sparkProperties.toStingArray(),sparkJobs.getParameters(),mqConf.getParameters(), MqHeadParser.getHeadArrays(headMap)));
                            } catch (Exception e) {
                                logger.error("Failed to aggregate table, Exception: {}.", e.getMessage());
                                result = 1;
                            } finally {
                                q_aggr_spark.send(new Message(("Finish aggregating task: " + taskId + ", application id: " + appId).getBytes(), getMessageProperties(messageProperties, result)));
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
