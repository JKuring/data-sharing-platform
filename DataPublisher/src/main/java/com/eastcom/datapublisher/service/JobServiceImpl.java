package com.eastcom.datapublisher.service;

import com.eastcom.common.bean.TaskType;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.datapublisher.bean.MBD_PUBLISH_CONF;
import com.eastcom.datapublisher.interfaces.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
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
    // service
    private static final String PUBLISH_HIVE_FTP = "PUBLISH_HIVE_FTP";
    private static final String PUBLISH_HIVE_HBASE = "PUBLISH_HIVE_HBASE";

    private static final int PUBLISH_HIVE_FTP_TYPE = 1;
    private static final int PUBLISH_HIVE_HBASE_TYPE = 2;

    @Autowired
    private TaskType taskType;
    @Resource(name = "PUBLISH_HIVE_FTP")
    private Executor publishFtpExecutor;
    @Resource(name = "PUBLISH_HIVE_HBASE")
    private Executor publishHbaseExecutor;

    public void excute(Message message) {
        int jobType = 0;
//        MessageProperties messageProperties = message.getMessageProperties();
//        Map<String, Object> header = messageProperties.getHeaders();
        String context = new String(message.getBody());
        final MBD_PUBLISH_CONF mbdPublishConf = JsonParser.parseJsonToObject(context.getBytes(), MBD_PUBLISH_CONF.class);
        try {
//            jobType = (Integer) header.get(MessageService.Header.jobType);
//            Map<Integer, String> taskTypesMap = taskType.getTaskTypesMap();
//            if (taskTypesMap.containsKey(jobType)) {
//                String taskType = taskTypesMap.get(jobType);
                switch (mbdPublishConf.getPubType()) {
                    case PUBLISH_HIVE_FTP_TYPE:
                        publishFtpExecutor.doJob(message);
                        break;
                    case PUBLISH_HIVE_HBASE_TYPE:
                        publishHbaseExecutor.doJob(message);
                        break;
                    default:
                        throw new Exception("invalid task type!");
                }
//            }
        } catch (Exception e) {
            logger.error("execute the task: {}, exception: {}.", jobType, e.getMessage());
        }
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
