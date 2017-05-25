package com.eastcom.datacontroller.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.datacontroller.bean.HDFSJobs;
import com.eastcom.datacontroller.interfaces.service.HDFSService;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 * Created by linghang.kong on 2017/5/11.
 */
public class HDFSDeleteDirController implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(HDFSDeleteDirController.class);

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private RabbitTemplate q_maint;

    @Autowired
    private HDFSService hdfsService;

    @Override
    public void doJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            if (taskId != null) {
                logger.info("start the task: {}.", taskId);
                HDFSJobs hBaseJobs = JsonParser.parseJsonToObject(context.getBytes(), HDFSJobs.class);
                for (String path : hBaseJobs.getPaths()
                        ) {
                    logger.debug("start the thread: {}.", Thread.currentThread().getName());
                    try {
                        final Path p = new Path(path);
                        threadPoolTaskExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                int result = Executor.SUCESSED;
                                try {
                                    hdfsService.deleteDir(p);
                                } catch (Exception e) {
                                    logger.error("Failed to delete path, Exception: {}.", e.getMessage());
                                    result = Executor.FAILED;
                                } finally {
                                    SendMessageUtility.send(q_maint,"Finish deleting task: " + p.getName(),messageProperties,result);
                                }
                            }
                        });
                    } catch (Exception e) {
                        logger.debug("Thread pool: {}.", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete path.", e.fillInStackTrace());
        }
    }
}
