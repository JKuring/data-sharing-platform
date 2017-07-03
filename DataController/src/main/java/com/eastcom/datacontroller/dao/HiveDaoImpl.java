package com.eastcom.datacontroller.dao;

import com.eastcom.common.message.SendMessageUtility;
import com.eastcom.datacontroller.bean.HiveJobs;
import com.eastcom.datacontroller.interfaces.dao.HiveDao;
import com.sun.jersey.api.ParamException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hive.HiveClient;
import org.springframework.data.hadoop.hive.HiveClientCallback;
import org.springframework.data.hadoop.hive.HiveTemplate;

import javax.annotation.Resource;
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

    @Resource(name = "hiveTemplate")
    private HiveTemplate template;




    @Override
    public void drop(final HiveJobs hiveJobs) throws ParamException {

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
    public void delete(final HiveJobs entity) throws IOException {
        try {
            if (isDrop(entity.getSql())) {
                this.template.execute(new HiveClientCallback<List<String>>() {
                    @Override
                    public List<String> doInHive(HiveClient hiveClient) throws Exception {
                        return hiveClient.execute(entity.getSql());
                    }
                });
                logger.info("Finish to dropping the {} table.", entity.getTableName());
            }else {
                throw new Exception("this sql is not a 'drop' or 'DROP' statement.");
            }
        } catch (Exception e) {
            logger.error("Failed to drop the {} table, the sql: {}. Exception: {}.",entity.getTableName(),entity.getSql(),e.getMessage());
        }
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

    public HiveTemplate getTemplate() {
        return template;
    }

    public void setTemplate(HiveTemplate template) {
        this.template = template;
    }

    private boolean isDrop(String sql) {
        return StringUtils.contains("drop", sql) || StringUtils.contains("DROP", sql);
    }
}
