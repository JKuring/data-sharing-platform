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

    private Pattern cmdPattern = Pattern.compile("\\[" + "cmd" + "]");
    private Pattern ftpPattern = Pattern.compile("ftp_url");

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
    private RabbitTemplate q_publish;

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
            final Matcher matcher = cmdPattern.matcher(cmd);
            if (matcher.find()) {
                threadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int rs = Executor.SUCESSED;
                        String command = null;
                        try {
                            logger.info("execute cmd.");
                            command = matcher.replaceFirst(getParameters(mbdPublishConf, headMap));
                            Process process = Runtime.getRuntime().exec(command);
                            logger.info("executing......");
                            if (process.waitFor() != 0) {
                                InputStreamReader inputStreamReader = new InputStreamReader(process.getErrorStream());
                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                String result;
                                StringBuffer tmp = new StringBuffer();
                                while ((result = bufferedReader.readLine()) != null) {
                                    tmp.append(result);
                                }
                                throw new Exception(tmp.toString());
                            } else {
                                InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                String result;
                                StringBuffer tmp = new StringBuffer();
                                while ((result = bufferedReader.readLine()) != null) {
                                    Matcher matcher1 = ftpPattern.matcher(result);
                                    if (matcher1.find()) {
                                        tmp.append(result);
                                    }
                                }
                                logger.info("publish MQ!");
                                q_publish.send(new Message(tmp.toString().getBytes(), getMessageProperties(messageProperties, rs)));
                            }
                            logger.info("Finish! Command: {}.", command);
                        } catch (Exception e) {
                            rs = Executor.FAILED;
                            logger.error("Failed to execute cmd: {}.", command, e.fillInStackTrace());
                            q_publish.send(new Message(("Finish publishing task: " + taskId + ", exception: " + e.getMessage()).getBytes(), getMessageProperties(messageProperties, rs)));
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
