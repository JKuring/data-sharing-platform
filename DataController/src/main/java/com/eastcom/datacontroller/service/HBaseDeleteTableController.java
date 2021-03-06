package com.eastcom.datacontroller.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.datacontroller.bean.HBaseEntityImpl;
import com.eastcom.datacontroller.bean.HBaseJobs;
import com.eastcom.datacontroller.bean.JobEntityImpl;
import com.eastcom.datacontroller.interfaces.dto.HBaseEntity;
import com.eastcom.datacontroller.interfaces.dto.JobEntity;
import com.eastcom.datacontroller.interfaces.service.HBaseService;
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
public class HBaseDeleteTableController implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(HBaseDeleteTableController.class);


    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private HBaseService<JobEntity> hbaseService;

    @Autowired
    private RabbitTemplate q_maint;

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
                for (String tableName : hBaseJobs.getName()
                        ) {
                    final JobEntityImpl jobEntity = new JobEntityImpl((String) headMap.get(MessageService.Header.jobName), getHBaseEntity(tableName, hBaseJobs));
                    jobEntity.setGranularity(hBaseJobs.getGranularity());
                    logger.info("delete the table: {}.", tableName);
                    threadPoolTaskExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("start the thread: {}.", Thread.currentThread().getName());
                            int result = 2;
                            messageProperties.setHeader(startTime, System.currentTimeMillis());
                            try {
                                hbaseService.delete(jobEntity);
                                //关闭任务
                                jobEntity.setJobEndTime(System.currentTimeMillis());
                            } catch (Exception e) {
                                logger.error("Failed to delete table, Exception: {}.", e.getMessage());
                                result = 1;
                            } finally {
                                q_maint.send(new Message(("Finish to delete task: " + jobEntity.getJobName()).getBytes(), getMessageProperties(messageProperties, result)));
                            }
                        }
                    });
                }
            } else {
                throw new Exception("Unable task!");
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
    }

    private HBaseEntity getHBaseEntity(String tableName, HBaseJobs hbaseJobs) {
        HBaseEntity hbaseEntity = new HBaseEntityImpl();
        hbaseEntity.setName(tableName);
        hbaseEntity.setColumns(hbaseJobs.getColumns());
        hbaseEntity.setVersion(hbaseJobs.getVersion());
        hbaseEntity.setCompressionType(hbaseJobs.getCompressionType());
        hbaseEntity.setTtl(hbaseJobs.getTtl());
        hbaseEntity.setSplitPolicy(hbaseJobs.getSplitPolicy());
        hbaseEntity.setSpiltKeysFile(hbaseJobs.getSpiltKeysFile());
        hbaseEntity.setCoprocessor(hbaseJobs.getCoprocessor());
        logger.debug(hbaseEntity.toString());
        return hbaseEntity;
    }

    private MessageProperties getMessageProperties(MessageProperties messageProperties, int result) {
        messageProperties.setHeader(endTime, System.currentTimeMillis());
        messageProperties.setHeader(status, result);
        return messageProperties;
    }
}
