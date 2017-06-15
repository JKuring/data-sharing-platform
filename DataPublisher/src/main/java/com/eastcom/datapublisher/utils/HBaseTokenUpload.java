package com.eastcom.datapublisher.utils;

import com.eastcom.datapublisher.dao.HBaseDaoImpl;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.token.AuthenticationTokenIdentifier;
import org.apache.hadoop.hbase.security.token.TokenUtil;
import org.apache.hadoop.security.token.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linghang.kong on 2017/5/27.
 */
@Component
public class HBaseTokenUpload {

    private static final Logger logger = LoggerFactory.getLogger(HBaseTokenUpload.class);


    @Autowired
    private HBaseDaoImpl hBaseDao;

    public void upload(Path path) {
        try {
            Configuration configuration = hBaseDao.getConfiguration();
            try (FileSystem fileSystem = FileSystem.get(configuration); Connection connection = ConnectionFactory.createConnection(configuration)) {
                if (!connection.isClosed()) {
                    logger.info("successful connection.");
                    logger.info("hbase.security.authentication: " + configuration.get("hbase.security.authentication"));
                    logger.info("hbase.regionserver.keytab.file: " + configuration.get("hbase.regionserver.keytab.file"));
                    logger.info("hbase.regionserver.port: " + configuration.get("hbase.regionserver.port"));
                    Token<AuthenticationTokenIdentifier> token =
                            TokenUtil.obtainToken(connection);
                    String urlString = token.encodeToUrlString();
                    FSDataOutputStream outputStream = fileSystem.create(path, true);
                    outputStream.writeBytes(urlString);
                    logger.info("output file: {}.", path.getName());
                }
            } catch (Exception e) {
                logger.error("", e.fillInStackTrace());
            } finally {
                logger.info("closed Connection and FileSystem.");
            }
        } catch (Exception e) {
            logger.error("", e.fillInStackTrace());
        }
    }
}
