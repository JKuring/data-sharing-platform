package com.eastcom.datapublisher.service;

import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.message.SendMessageUtility;
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
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class HivePublishHBase implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(HivePublishHBase.class);

    private final String sepa = System.lineSeparator();

    private Pattern cmdPattern = Pattern.compile("\\[" + "cmd" + "]");

    private final String timeId = "timeId";

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private RabbitTemplate q_publish;

    @Resource(name = "pubHbaseCmd")
    private String cmd;

    @Override
    public void doJob(final Message message) {
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
                            String parameters = getParameters(mbdPublishConf, headMap);
                            if (parameters == null)
                                throw new Exception("Encounter  incorrect  publish  config  messages ");

                            command = matcher.replaceFirst(parameters);

                            String[] cmdArray = new String[]{"/bin/sh", "-c", command};

                            Process process = Runtime.getRuntime().exec(cmdArray);
                            logger.info("executing command : " + command);
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
                                logger.info("publish MQ!");
                                SendMessageUtility.send(q_publish, "exit code: " + String.valueOf(process.exitValue()), messageProperties, rs);
                            }
                            logger.info("Finish! Command: {}.", command);
                        } catch (Exception e) {
                            rs = Executor.FAILED;
                            logger.error("Failed to execute cmd: {}.", command, e.fillInStackTrace());
                            SendMessageUtility.send(q_publish, "Finish publishing task: " + taskId + ", exception: " + e.getMessage(), messageProperties, rs);
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
    }

    private String getParameters(MBD_PUBLISH_CONF mbdPublishConf, Map<String, Object> headMap) {

        String fileFullPath = null;
        Date publishTime = null;
        SimpleDateFormat originalFmt = new SimpleDateFormat("yyyyMMddHHmm");

        SimpleDateFormat convertFmt = new SimpleDateFormat(mbdPublishConf.getFtpPathExpr());

        try {
            publishTime = originalFmt.parse((String) headMap.get(this.timeId));
        } catch (ParseException e) {
            logger.error("Error timeId  format  in message Properties ......");
            return null;
        }
        fileFullPath = convertFmt.format(publishTime);
        // parameter  list : TEMPLATE_FILE_NAME , HDFS_EXPORT_PATH ,  timeid , target_table
        StringBuilder builder = new StringBuilder();
        builder.append(mbdPublishConf.getExportTableName()).append(" ");
        builder.append(mbdPublishConf.getHdfsExportPath()).append(" ");
        builder.append(headMap.get(this.timeId)).append(" ");
        builder.append(mbdPublishConf.getHbaseTableName());
        return builder.toString();
    }
}
