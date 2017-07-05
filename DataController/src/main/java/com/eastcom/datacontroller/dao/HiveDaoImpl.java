package com.eastcom.datacontroller.dao;

import com.eastcom.datacontroller.interfaces.dao.HiveDao;
import com.eastcom.datacontroller.interfaces.dto.HiveEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hive.HiveClient;
import org.springframework.data.hadoop.hive.HiveClientCallback;
import org.springframework.data.hadoop.hive.HiveTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by linghang.kong on 2017/6/27.
 */
public class HiveDaoImpl implements HiveDao<HiveEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HiveDaoImpl.class);

    @Resource(name = "hiveTemplate")
    private HiveTemplate template;

    @Resource(name = "dropPartitionTemp")
    private String dropPartitionTemp;

    @Resource(name = "showPartitionTemp")
    private String showPartitionTemp;

    private String patternTableName;
    private String patternPartitionName;

    public HiveDaoImpl() {
        patternTableName = "\\[tableName]";
        patternPartitionName = "\\[partitionName]";
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

    @Override
    public void drop(HiveEntity hiveEntity) {

    }

    @Override
    public List<String> getPartitions(HiveEntity hiveEntity) {
        try {
            final String showSQL = showPartitionTemp.replaceFirst(patternTableName, hiveEntity.getTableName()).replaceFirst(patternPartitionName, hiveEntity.getPartition());
                return this.template.execute(new HiveClientCallback<List<String>>() {
                    @Override
                    public List<String> doInHive(HiveClient hiveClient) throws Exception {
                        return hiveClient.execute(showSQL);
                    }
                });
        } catch (Exception e) {
            logger.error("Failed to get the {} table, the partition: {}. Exception: {}.", hiveEntity.getTableName(), hiveEntity.getPartition(), e.getMessage());
        }
        return null;
    }

    @Override
    public HiveEntity get(Class<HiveEntity> entityClazz, Serializable id) {
        return null;
    }

    @Override
    public Serializable save(HiveEntity entity) {
        return null;
    }

    @Override
    public void update(HiveEntity entity) {

    }

    @Override
    public void delete(HiveEntity entity) throws IOException {
        try {
            final String dropSQL = dropPartitionTemp.replaceFirst(patternTableName, entity.getTableName()).replaceFirst(patternPartitionName, entity.getPartition());
            if (!isDrop(dropSQL)) {
                this.template.execute(new HiveClientCallback<List<String>>() {
                    @Override
                    public List<String> doInHive(HiveClient hiveClient) throws Exception {
                        return hiveClient.execute(dropSQL);
                    }
                });
                logger.info("Finish dropping the {} partition of the {} table.", entity.getPartition(), entity.getTableName());
            } else {
                throw new Exception(dropSQL+" sql is not a 'drop' or 'DROP' statement.");
            }
        } catch (Exception e) {
            logger.error("Failed to drop the {} table, the partition: {}. Exception: {}.", entity.getTableName(), entity.getPartition(), e.getMessage());
        }
    }

    @Override
    public void delete(Class<HiveEntity> entityClazz, Serializable id) {

    }

    @Override
    public List<HiveEntity> findAll(Class<HiveEntity> entityClazz) {
        return null;
    }

    @Override
    public long findCount(Class<HiveEntity> entityClazz) {
        return 0;
    }

    @Override
    public void close() throws IOException {

    }
}
