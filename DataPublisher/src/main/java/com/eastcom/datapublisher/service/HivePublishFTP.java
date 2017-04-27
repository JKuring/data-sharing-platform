package com.eastcom.datapublisher.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.datapublisher.bean.MBD_PUBLISH_CONF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linghang.kong on 2017/4/26.
 */
public class HivePublishFTP implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(HivePublishFTP.class);

    private String sepa = File.separator;

    private Pattern pattern = Pattern.compile("\\[" + "cmd" + "]");

    // back head
    private String startTime = "startTime";
    private String endTime = "endTime";
    private String status = "status";

    private final String tableName = "tableName";

    private final String partition = "partition";

    private final String timeId = "timeId";

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private RabbitTemplate q_log;

    @Resource(name = "hiveWarehousePath")
    private String hiveWarehousePath;

    @Resource(name = "cmd")
    private String cmd;

    @Resource(name = "FILE_PREFIX")
    private String FILE_PREFIX;

    @Resource(name = "TIME_FORMAT")
    private String TIME_FORMAT;

    @Override
    public void doJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());
        try {
            logger.info("start the task: {}.", taskId);
            final MBD_PUBLISH_CONF mbdPublishConf = JsonParser.parseJsonToObject(context.getBytes(), MBD_PUBLISH_CONF.class);
            final Matcher matcher = pattern.matcher(cmd);
            if (matcher.find()) {
                threadPoolTaskExecutor.execute(new Runnable() {
                    int rs = Executor.SUCESSED;

                    @Override
                    public void run() {
                        try {
                            logger.info("execute cmd.");
                            Process process = Runtime.getRuntime().exec(matcher.replaceFirst(getParameters(mbdPublishConf, headMap)));
                            if (process.waitFor() != 0) {
                                InputStreamReader inputStreamReader = new InputStreamReader(process.getErrorStream());
                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                String result;
                                StringBuffer tmp = new StringBuffer();
                                if ((result = bufferedReader.readLine()).length() > 0) {
                                    tmp.append(result);
                                }
                                rs = Executor.FAILED;
                                throw new Exception(tmp.toString());
                            }
                            logger.info("Finish!");
                        } catch (Exception e) {
                            logger.error("Failed to execute cmd: {}, exception: {}.", cmd, e.getMessage());
                        } finally {
                            q_log.send(new Message(("Finish loading task: " + taskId).getBytes(), getMessageProperties(messageProperties, rs)));

                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
    }


    private String getParameters(MBD_PUBLISH_CONF mbdPublishConf, Map<String, Object> headMap) {
        StringBuilder builder = new StringBuilder();
        builder.append(" ");
        builder.append(mbdPublishConf.getCatalogId()).append(" ");
        builder.append(mbdPublishConf.getRealTableName()).append(" ");
        builder.append(mbdPublishConf.getPubType()).append(" ");
        builder.append(headMap.get(this.timeId)).append(" ");
        builder.append(mbdPublishConf.getFtpPathExpr()).append(" ");
        builder.append(this.FILE_PREFIX).append(" ");
        builder.append(mbdPublishConf.getFtpServer()).append(" ");
        builder.append(this.TIME_FORMAT).append(" ");
        builder.append(mbdPublishConf.getEsbCode());
        return builder.toString();
    }

    private MessageProperties getMessageProperties(MessageProperties messageProperties, int result) {
        messageProperties.setHeader(endTime, System.currentTimeMillis());
        messageProperties.setHeader(status, result);
        return messageProperties;
    }
}
