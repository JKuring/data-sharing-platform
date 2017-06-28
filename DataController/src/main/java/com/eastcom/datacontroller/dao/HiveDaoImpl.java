package com.eastcom.datacontroller.dao;

import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.datacontroller.bean.HiveJobs;
import com.eastcom.datacontroller.interfaces.dao.HiveDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by linghang.kong on 2017/6/27.
 */
public class HiveDaoImpl implements HiveDao<HiveJobs> {

    private static final Logger logger = LoggerFactory.getLogger(HiveDaoImpl.class);

    @Autowired
    private DataSource dataSource;


    @Override
    public void drop(HiveJobs hiveJobs) {
        try {
            Connection connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("");
        }
    }

    @Override
    public HiveJobs get(Class<HiveJobs> entityClazz, Serializable id) {
        return null;
    }

    @Override
    public Serializable save(HiveJobs entity) {
        return null;
    }

    @Override
    public void update(HiveJobs entity) {

    }

    @Override
    public void delete(HiveJobs entity) throws IOException {

    }

    @Override
    public void delete(Class<HiveJobs> entityClazz, Serializable id) {

    }

    @Override
    public List<HiveJobs> findAll(Class<HiveJobs> entityClazz) {
        return null;
    }

    @Override
    public long findCount(Class<HiveJobs> entityClazz) {
        return 0;
    }

    @Override
    public void close() throws IOException {

    }
}
