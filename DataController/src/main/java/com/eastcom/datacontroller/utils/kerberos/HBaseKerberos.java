package com.eastcom.datacontroller.utils.kerberos;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.zookeeper.ZKUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by linghang.kong on 2016/12/19.
 */
public class HBaseKerberos {

    private static final Logger logger = LoggerFactory.getLogger(HBaseKerberos.class);

    public static Configuration getConfiguration(Configuration configuration) {
        logger.info("Fetch HBase configuration.");
        File directory = new File("").getAbsoluteFile().getParentFile();
        if (User.isHBaseSecurityEnabled(configuration)) {
            String confHomePath = System.getProperty("auth.config.path");
            String separator = System.getProperty("file.separator");
            String confDirPath = directory.getAbsolutePath() + separator + confHomePath;
            logger.info("kerberos configuration path: {}.", confDirPath);
            logger.debug("zookeeper.sasl.clientconfig: {}.", System.getProperty("zookeeper.sasl.clientconfig"));
            logger.debug("zookeeper.server.principal: {}.", System.getProperty("zookeeper.server.principal"));

            // jaas.configuration file, it is included in the client pakcage file
            System.setProperty("java.security.auth.login.config", confDirPath + "jaas.conf");
            // set the kerberos server info,point to the kerberosclient
            // configuration file.
            System.setProperty("java.security.krb5.conf", confDirPath + "krb5.conf");
            // set "user.keytab" as the download keytab file name, 注[1]
            configuration.set("username.client.keytab.file", confDirPath + "user.keytab");
            // set "hbaseuser1" as the new create user name
            configuration.set("username.client.kerberos.principal", "wg_B@HADOOP_B.COM");

            try {
                String hostName = InetAddress.getLocalHost().getCanonicalHostName();
                logger.debug("hadoop.security.authentication: {}.", configuration.get("hadoop.security.authentication"));
                User.login(configuration, "username.client.keytab.file", "username.client.kerberos.principal", "xx");
                logger.debug("login ZK client!");
                ZKUtil.loginClient(configuration, "username.client.keytab.file", "username.client.kerberos.principal", "xx");
                logger.debug("host name: {}.", hostName);
            } catch (UnknownHostException e) {
                logger.error("UnknownHostException: {}.", e.getMessage());
            } catch (IOException e) {
                logger.error("IOException: {}.", e.getMessage());
            }
        }
        return configuration;
    }
}
