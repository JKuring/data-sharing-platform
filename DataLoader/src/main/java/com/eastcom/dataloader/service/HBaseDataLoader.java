package com.eastcom.dataloader.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.service.HttpRequestUtils;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.common.utils.time.TimeTransform;
import com.eastcom.dataloader.bean.HBaseJobs;
import com.eastcom.dataloader.bean.JobEntityImpl;
import com.eastcom.dataloader.interfaces.dto.JobEntity;
import com.eastcom.dataloader.interfaces.service.HBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 * Created by linghang.kong on 2017/4/7.
 */
public class HBaseDataLoader implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(HBaseDataLoader.class);

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private HBaseService<JobEntity> hbaseService;

    @Autowired
    private RabbitTemplate q_load;

    private final static String EASTCOM_SEPARATOR = ",";

    // back head
    private String startTime = "startTime";
    private String endTime = "endTime";
    private String status = "status";


    @Override
    public void doJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                HBaseJobs hBaseJobs = JsonParser.parseJsonToObject(context.getBytes(), HBaseJobs.class);
                assert hBaseJobs != null : "Can't find the job!";
                String jobName = hBaseJobs.getName();
                for (String name : jobName.split(EASTCOM_SEPARATOR)
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

    private MessageProperties getMessageProperties(MessageProperties messageProperties, int result) {
        messageProperties.setHeader(endTime, System.currentTimeMillis());
        messageProperties.setHeader(status, result);
        return messageProperties;
    }
}
