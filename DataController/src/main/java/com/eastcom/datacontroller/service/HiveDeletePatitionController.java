package com.eastcom.datacontroller.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.datacontroller.bean.HiveJobs;
import com.eastcom.datacontroller.interfaces.dao.HiveDao;
import com.eastcom.datacontroller.interfaces.dto.HiveEntity;
import com.eastcom.datacontroller.interfaces.service.HiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 * Created by linghang.kong on 2017/6/26.
 */
public class HiveDeletePatitionController implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(HiveDeletePatitionController.class);

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private RabbitTemplate q_maint;

    @Autowired
    private HiveService<HiveJobs> hiveService;

    @Override
    public void doJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            logger.info("start the task: {}.", taskId);
            final HiveJobs hiveJobs = JsonParser.parseJsonToObject(context.getBytes(), HiveJobs.class);
            try {
                threadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.debug("start the thread: {}.", Thread.currentThread().getName());
                        int result = Executor.SUCESSED;
                        messageProperties.setHeader(MessageService.Header.startTime, System.currentTimeMillis());
                        try {
                            hiveService.delete(hiveJobs);
                            logger.info("Finish deleting partition!");
                        } catch (Exception e) {
                            logger.error("Failed to drop the {} table,  partition: {}, Exception: {}.", hiveJobs.getTableName(), hiveJobs.getPartition(), e.getMessage());
                            result = Executor.FAILED;
                        } finally {
                            SendMessageUtility.send(q_maint, "Finish dropping task: " + taskId, messageProperties, result);
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("Thread pool: {}.", e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
            SendMessageUtility.send(q_maint, "Finish dropping task: " + taskId + ", exception: " + e.getMessage(), messageProperties, Executor.FAILED);
        }

    }
}
