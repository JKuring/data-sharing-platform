package com.eastcom.dataloader.service;

import com.eastcom.common.bean.SparkProperties;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.common.service.HttpRequestUtils;
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

    private SparkProperties sparkProperties;

    @Autowired
    private RabbitTemplate q_load;

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
                logger.info("the name of loaded job: {}.", sparkJobs.getName());

                sparkProperties = HttpRequestUtils.httpGet(sparkJobs.getConfigServiceUrl() + sparkJobs.getSparkConf(), SparkProperties.class);
                try {
                    threadPoolTaskExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("start the thread: {}.", Thread.currentThread().getName());
                            int result = Executor.SUCESSED;
                            messageProperties.setHeader(MessageService.Header.startTime, System.currentTimeMillis());
                            String[] params = null;
                            try {
                                // submit code to cluster
                                try {
                                    params = MergeArrays.merge(sparkProperties.toParametersArray(), sparkJobs.getParameters());
                                } catch (Exception e) {
                                    throw new Exception("Parameters false!!!!!!!");
                                }
                                SparkSubmit$.MODULE$.main(params);
                            } catch (Exception e) {
                                logger.error("Failed to load table, params: {}, Exception: {}.", Arrays.toString(params), e.getMessage());
                                result = Executor.FAILED;
                            } finally {
                                SendMessageUtility.send(q_load, "Finish loading task: " + taskId, messageProperties, result);
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.error("Thread pool: {}.", e.getMessage());
                    throw e;
                }
            } else {
                throw new Exception("Unable task! task ID is null.");
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
            SendMessageUtility.send(q_load, "Finish loading task: " + taskId + ", exception: " + e.getMessage(), messageProperties, Executor.FAILED);

        }
    }
}
