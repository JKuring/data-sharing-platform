package com.eastcom.datapublisher.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linghang.kong on 2017/4/27.
 */
public class HivePublishFTP2 implements Executor<Message> {


    private static final Logger logger = LoggerFactory.getLogger(HivePublishFTP2.class);

    private final String tableName = "tableName";

    private final String partition = "partition";

    private final String timeId = "timeId";

    private final String hour = "_h";

    private final String day = "_d";

    private final String month = "_m";


    private String sepa = File.separator;

    private Pattern pattern = Pattern.compile("_[hdm]");

    @Resource(name = "hiveWarehousePath")
    private String hiveWarehousePath;

    @Override
    public void doJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());

        String tableName = (String) headMap.get(this.tableName);
        String partition = (String) headMap.get(this.partition);
        String path;
        try {
            if (partition.length() > 0) {
                Matcher matcher = this.pattern.matcher(tableName);
                if (matcher.find()) {
                    switch (matcher.group()) {
                        case hour:
                            ;
                            break;
                        case day:
                            ;
                            break;
                        case month:
                            ;
                            break;
                    }

                }
            } else {

            }

            path = this.hiveWarehousePath + tableName + this.sepa + partition;
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }

    }
}
