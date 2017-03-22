package com.eastcom.aggregator.service;

import com.eastcom.aggregator.SssLauncher;
import com.eastcom.aggregator.bean.SparkJobs;
import com.eastcom.aggregator.bean.SparkProperties;
import com.eastcom.aggregator.interfaces.service.JobService;
import com.eastcom.aggregator.interfaces.service.MessageService;
import com.eastcom.aggregator.utils.parser.JsonParser;
import com.eastcom.common.message.CommonMeaageProducer;
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
    private JsonParser jsonParser;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private SparkProperties sparkProperties;


    private Map<String, RabbitTemplate> mqProducer = CommonMeaageProducer.producerCollection;

    private RabbitTemplate q_aggr_spark = mqProducer.get("q_aggr_spark");


    // back head
    private String startTime = "startTime";
    private String endTime = "endTime";
    private String status = "status";


    public void excute(Message message) {
        int jobType = 0;
        // save message for scala
        MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> header = messageProperties.getHeaders();
        try {
            jobType = (Integer) header.get(MessageService.Header.jobType);
            switch (jobType) {
                case MessageService.TaskType.CREATE_TABLE_HBASE:
                    doSparkAggregationJob(message);
                    break;
                default:
                    throw new Exception("invalid task type!");
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            messageProperties.setHeader(status, 1);
            q_aggr_spark.send(new Message(("execute the task: "+jobType+", exception: "+e.getMessage()).getBytes(),message.getMessageProperties()));

        }
    }

    @Override
    public void doSparkAggregationJob(Message message) {
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
                                SssLauncher.launch(sparkJobs.getParameters(),sparkProperties);
                            } catch (Exception e) {
                                logger.error("Failed to load table, Exception: {}.", e.getMessage());
                                result = 1;
                            } finally {
                                q_aggr_spark.send(new Message(("Finish loading task: " + taskId + ", application id: " + appId).getBytes(), getMessageProperties(messageProperties, result)));
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
