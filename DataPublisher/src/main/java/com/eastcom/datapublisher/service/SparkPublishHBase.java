package com.eastcom.datapublisher.service;

import com.eastcom.common.bean.SparkProperties;
import com.eastcom.common.interfaces.service.Executor;
import com.eastcom.common.interfaces.service.MessageService;
import com.eastcom.common.service.HttpRequestUtils;
import com.eastcom.common.utils.MergeArrays;
import com.eastcom.common.utils.parser.JsonParser;
import com.eastcom.datapublisher.bean.MBD_PUBLISH_CONF;
import org.apache.spark.deploy.SparkSubmit$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Map;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class SparkPublishHBase implements Executor<Message> {

    private static final Logger logger = LoggerFactory.getLogger(SparkPublishHBase.class);

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

    private SparkProperties sparkProperties;

    //    @Value("${global.configServiceUrl}")
    private String configServiceUrl;

    //    @Value("${global.sparkSubmitParamCiCode}")
    private String sparkSubmitCiCode;

    @Override
    public void doJob(Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Object> headMap = messageProperties.getHeaders();
        final String taskId = (String) headMap.get(MessageService.Header.taskId);
        String context = new String(message.getBody());

        try {
            logger.info("start the task: {}.", taskId);
            final MBD_PUBLISH_CONF mbdPublishConf = JsonParser.parseJsonToObject(context.getBytes(), MBD_PUBLISH_CONF.class);
            sparkProperties = HttpRequestUtils.httpGet(configServiceUrl + sparkSubmitCiCode, SparkProperties.class);
            try {
                threadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.debug("start the thread: {}.", Thread.currentThread().getName());
                        String[] params = null;
                        messageProperties.setHeader(startTime, System.currentTimeMillis());
                        try {
                            try {
                                params = MergeArrays.merge(sparkProperties.toParametersArray(), getParameters(mbdPublishConf, headMap));
                            } catch (Exception e) {
                                throw new Exception("Parameters false!!!!!!!");
                            }
                            SparkSubmit$.MODULE$.main(params);
                        } catch (Exception e) {
                            logger.error("Failed to aggregate table,  params: {}, Exception: {}.", Arrays.toString(params), e.getMessage());
//                            q_aggr_spark.send(new Message(("Finish aggregating task: " + taskId + ", jobs parameter: " + Arrays.toString(params)).getBytes(), getMessageProperties(messageProperties, Executor.FAILED)));
                        }
                    }
                });
            } catch (Exception e) {
                logger.debug("Thread pool: {}.", e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to execute the task id: {}, message: {}, exception: {}.", taskId, context, e.getMessage());
        }
    }

    private String[] getParameters(MBD_PUBLISH_CONF mbdPublishConf, Map<String, Object> headMap) {

//        Date publishTime = null;
//        SimpleDateFormat originalFmt = new SimpleDateFormat("yyyyMMddHHmm");
//        try {
//             publishTime = originalFmt.parse((String) headMap.get(this.timeId));
//        } catch (ParseException e) {
//            logger.error("Error timeId  format  in message Properties ......");
//            return  null;
//        }
        // configServiceUrl, tplCiCode , hdfsExportPath, hbaseTableName,  timeid , zookeeper_hosts, zookeeper_port,
        // Hbase  发布参数 :configServiceUrl , 取数模板 ciCode ，  HDFS 中间输出路径 , HBASE 表名 ,  取数时间 , zk 地址， zk端口
        return new String[]{configServiceUrl, mbdPublishConf.getExportTableName(),
                mbdPublishConf.getHdfsExportPath(), mbdPublishConf.getHbaseTableName(),
                (String) headMap.get(this.timeId)};

    }

    private MessageProperties getMessageProperties(MessageProperties messageProperties, int result) {
        messageProperties.setHeader(endTime, System.currentTimeMillis());
        messageProperties.setHeader(status, result);
        return messageProperties;
    }
}