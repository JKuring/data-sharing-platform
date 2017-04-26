package com.eastcom.aggregator.service;

import com.eastcom.aggregator.interfaces.service.JobService;
import com.eastcom.common.bean.TaskType;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
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

    private final TaskType taskType;

    @Autowired
    private RabbitTemplate q_aggr_spark;


    // back head
    private String startTime = "startTime";
    private String endTime = "endTime";
    private String status = "status";

    // executor
    @Resource(name = "AGGREGATE_SPARK")
    Executor aggregator_spark;

    // task types
    private static final String AGGREGATE_SPARK = "AGGREGATE_SPARK";

    @Autowired
    public JobServiceImpl(TaskType taskType) {
        this.taskType = taskType;
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
                        aggregator_spark.doJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
            q_aggr_spark.send(new Message(("execute the task: " + jobType + ", exception: " + e.getMessage()).getBytes(), getMessageProperties(messageProperties, Executor.FAILED)));
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
