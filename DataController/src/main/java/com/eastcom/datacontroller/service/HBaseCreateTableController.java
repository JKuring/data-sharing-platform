package com.eastcom.datacontroller.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.MessageHead;
import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.common.utils.time.TimeTransform;
import com.eastcom.datacontroller.bean.HBaseJobs;
import com.eastcom.datacontroller.bean.JobEntityImpl;
import com.eastcom.datacontroller.interfaces.dto.JobEntity;
import com.eastcom.datacontroller.interfaces.service.HBaseService;
import com.eastcom.datacontroller.utilities.BuildHBaseEntity;
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
public class HBaseCreateTableController implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(HBaseCreateTableController.class);

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private HBaseService<JobEntity> hbaseService;

    @Autowired
    private RabbitTemplate q_maint;


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
                for (String tableName : hBaseJobs.getName()
                        ) {
                    final JobEntity jobEntity = new JobEntityImpl((String) headMap.get(MessageService.Header.jobName), BuildHBaseEntity.getHBaseEntity(tableName, hBaseJobs));
                    jobEntity.setJobStartTime(System.currentTimeMillis());
                    jobEntity.setCreateTime(TimeTransform.getTimestamp(hBaseJobs.getTime()));
                    jobEntity.setPreDays(hBaseJobs.getPreDays());
                    jobEntity.setGranularity(hBaseJobs.getGranularity());
                    logger.info("create the table: {}.", tableName);
                    try {
                        threadPoolTaskExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                logger.debug("start the thread: {}.", Thread.currentThread().getName());
                                int result = Executor.SUCESSED;
                                messageProperties.setHeader(MessageService.Header.startTime, System.currentTimeMillis());
                                try {
                                    hbaseService.createTable(jobEntity);
                                    //关闭任务
                                    jobEntity.setJobEndTime(System.currentTimeMillis());
                                } catch (Exception e) {
                                    logger.error("Failed to create table, Exception: {}.", e.getMessage());
                                    result = Executor.FAILED;
                                } finally {
                                    SendMessageUtility.send(q_maint, "Finish creating task: " + jobEntity.getJobName(), messageProperties, result);
                                }
                            }
                        });
                    } catch (Exception e) {
                        logger.debug("Thread pool: {}.", e.getMessage());
                        throw e;
                    }
                }
            } else {
                throw new Exception("Unable task! task ID is null.");
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
            SendMessageUtility.send(q_maint, "Finish creating task: " + taskId + ", exception: " + e.getMessage(), messageProperties, Executor.FAILED);
        }
    }
}
