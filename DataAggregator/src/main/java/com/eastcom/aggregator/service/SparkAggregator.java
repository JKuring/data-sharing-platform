package com.eastcom.aggregator.service;

import com.eastcom.aggregator.bean.MQConf;
import com.eastcom.aggregator.bean.SparkJobs;
import com.eastcom.common.bean.SparkProperties;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.MessageHead;
import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.common.service.HttpRequestUtils;
import com.eastcom.common.utils.MergeArrays;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.common.utils.parser.MqHeadParser;
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
 * Created by linghang.kong on 2017/4/1.
 */
public class SparkAggregator implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(SparkAggregator.class);


    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private SparkProperties sparkProperties;

    @Autowired
    private MQConf mqConf;

    @Autowired
    private RabbitTemplate q_aggr_spark;


    @Override
    public void doJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                final SparkJobs sparkJobs = JsonParser.parseJsonToObject(context.getBytes(), SparkJobs.class);
                logger.info("the name of aggregated job: {}.", sparkJobs.getName());
                sparkProperties = HttpRequestUtils.httpGet(sparkJobs.getConfigServiceUrl() + sparkJobs.getSparkConf(), SparkProperties.class);
                try {
                    threadPoolTaskExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("start the thread: {}.", Thread.currentThread().getName());
                            String[] params = null;
                            messageProperties.setHeader(MessageHead.startTime, System.currentTimeMillis());
                            try {
                                try {
                                    params = MergeArrays.merge(sparkProperties.toParametersArray(), sparkJobs.getParameters(), mqConf.getParameters(), MqHeadParser.getHeadArrays(headMap));
                                } catch (Exception e) {
                                    throw new Exception("Parameters false!!!!!!!");
                                }
                                SparkSubmit$.MODULE$.main(params);
                            } catch (Exception e) {
                                logger.error("Failed to aggregate table,  params: {}, Exception: {}.", Arrays.toString(params), e.getMessage());
                                SendMessageUtility.send(q_aggr_spark,"Finish aggregating task: " + taskId + ", jobs exception: " + e.getMessage(),messageProperties,Executor.FAILED);
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
}
