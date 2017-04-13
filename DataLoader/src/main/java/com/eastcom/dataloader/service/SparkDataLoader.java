package com.eastcom.dataloader.service;

import com.eastcom.common.bean.SparkProperties;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.utils.MergeArrays;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.dataloader.bean.SparkJobs;
import org.apache.spark.deploy.SparkSubmit$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by linghang.kong on 2017/4/7.
 */
public class SparkDataLoader implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(SparkDataLoader.class);

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    SparkProperties sparkProperties;

    @Autowired
    private RabbitTemplate q_load;

    // back head
    private String startTime = "startTime";
    private String endTime = "endTime";
    private String status = "status";

    @Override
    public void doJob(Message message) {
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
                            int result = Executor.SUCESSED;
                            messageProperties.setHeader(startTime, System.currentTimeMillis());
                            String[] params = null;
                            try {
                                // submit code to cluster
                                params = MergeArrays.merge(sparkProperties.toParametersArray(), sparkJobs.getParameters());
                                SparkSubmit$.MODULE$.main(params);
                            } catch (Exception e) {
                                logger.error("Failed to load table, params: {}, Exception: {}.", Arrays.toString(params), e.getMessage());
                                result = Executor.FAILED;
                            } finally {
                                q_load.send(new Message(("Finish loading task: " + taskId + ", jobs parameter: " + sparkJobs.getParameters()).getBytes(), getMessageProperties(messageProperties, result)));
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
}
