package com.eastcom.datapublisher.dao;

import com.eastcom.common.utils.kerberos.HBaseKerberos;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by linghang.kong on 2017/6/14.
 */
public class HBaseDaoImpl {
    private static final Logger logger = LoggerFactory.getLogger(HBaseDaoImpl.class);

    private Configuration configuration;

    public HBaseDaoImpl(Configuration configuration) {
        this.configuration = configuration;
        if (this.configuration == null) {
            logger.error("configuration is null!");
            System.exit(1);
        } else {
            logger.info("successful Fetch configuration!");
            // add kerberos
            this.configuration = HBaseKerberos.getConfiguration(this.configuration);
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
