package com.eastcom;

import com.eastcom.common.utils.kerberos.HBaseKerberos;
import com.eastcom.dataloader.dao.HBaseDaoImpl;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.token.AuthenticationTokenIdentifier;
import org.apache.hadoop.hbase.security.token.TokenUtil;
import org.apache.hadoop.security.token.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;

/**
 * Created by linghang.kong on 2017/5/31.
 */
public class Test2CreateTGT {

    private static final Logger logger = LoggerFactory.getLogger(Test2CreateTGT.class);
    public static ApplicationContext applicationContext;


//
//    public static void main(String[] args) throws IOException {
//        try {
////            System.setProperty("auth.config.path", "DataSharingPlatform/DataLoader/target/conf/hbase/auth/");
//            applicationContext = new ClassPathXmlApplicationContext("classpath:beans.xml");
//
//
//            HBaseDaoImpl hBaseDao = applicationContext.getBean("hbaseDaoImpl",HBaseDaoImpl.class);
//
//
//            Configuration configuration = hBaseDao.getConfiguration();
////            if (configuration == null) {
////                logger.info("configuration is null!");
////                System.exit(1);
////            } else {
////                logger.info("successful Fetch configuration!");
////                // add kerberos
////                configuration = HBaseKerberos.getConfiguration(configuration);
////            }
//            Connection connection = ConnectionFactory.createConnection(configuration);
//            try {
//                if (!connection.isClosed()) {
//                    logger.info("successful connection.");
//                    logger.info("hbase.security.authentication: "+configuration.get("hbase.security.authentication"));
//                    logger.info("hbase.regionserver.keytab.file: "+configuration.get("hbase.regionserver.keytab.file"));
//                    logger.info("hbase.regionserver.port: "+configuration.get("hbase.regionserver.port"));
//                    hBaseDao.deleteTable(TableName.valueOf("test"));
//
//                }
//                Token<AuthenticationTokenIdentifier> token =
//                        TokenUtil.obtainToken(connection);
//                String urlString = token.encodeToUrlString();
//                File temp = new File(FileUtils.getTempDirectory(), "token");
//                FileUtils.writeStringToFile(temp, urlString);
//                logger.info(temp.getAbsolutePath());
//            } catch (Exception e) {
//                logger.error("",e.fillInStackTrace());
//            } finally {
//                connection.close();
//            }
//        }catch (Exception e){
//            logger.error("",e.fillInStackTrace());
//        }
//    }
}
