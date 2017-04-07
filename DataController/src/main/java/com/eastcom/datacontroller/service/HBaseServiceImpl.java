package com.eastcom.datacontroller.service;

import com.eastcom.common.utils.HBaseUtils;
import com.eastcom.datacontroller.dao.HBaseDaoImpl;
import com.eastcom.datacontroller.interfaces.dto.HBaseEntity;
import com.eastcom.datacontroller.interfaces.dto.JobEntity;
import com.eastcom.datacontroller.interfaces.service.HBaseService;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by linghang.kong on 2016/12/23.
 */
@Service
@Lazy
public class HBaseServiceImpl implements HBaseService<JobEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HBaseServiceImpl.class);

    private final long addition = 24 * 60 * 60 * 1000;
    @Autowired
    private HBaseDaoImpl hBaseDao;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName() {

    }

    public void createTable(JobEntity jobEntity) throws IOException {
        HBaseEntity hBaseEntity = (HBaseEntity) jobEntity.getTableEntity();
        try {
            for (int i = 0; i < jobEntity.getPreDays(); i++) {
                this.hBaseDao.createTable(TableName.valueOf(HBaseUtils.getCurrentTimeTableName(hBaseEntity.getName(),
                        jobEntity.getCreateTime() + addition * i, 0, jobEntity.getGranularity())), hBaseEntity.getColumns(),
                        hBaseEntity.getVersion(), hBaseEntity.getTtl(), hBaseEntity.getCompressionType(),
                        hBaseEntity.getCoprocessor(), hBaseEntity.getSplitPolicy(), hBaseEntity.getSpiltKeysFile());
            }
        } catch (IOException e) {
            logger.error("execute to create the table:{}.", hBaseEntity.getName());
            throw e;
        }
    }

    public void delete(JobEntity jobEntity) {
        HBaseEntity hBaseEntity = (HBaseEntity) jobEntity.getTableEntity();
        try {
            String tableName = HBaseUtils.getCurrentTimeTableName(hBaseEntity.getName(), System.currentTimeMillis() - hBaseEntity.getTtl() * 1000L, 0, jobEntity.getGranularity());
            this.hBaseDao.deleteTable(TableName.valueOf(tableName));
        } catch (Exception e) {
            logger.error("Failed to delete the {} table, exception: {}.", hBaseEntity.getName(), e.getMessage());
        }
    }

    @Override
    public boolean partition(JobEntity hBaseEntity) {
        return false;
    }


    public List<TableName> getTableNames() {
        try {
            TableName[] tableName = this.hBaseDao.getTableNames();
            return Arrays.asList(tableName);
        } catch (IOException e) {
            logger.error("");
        }
        return null;
    }

    private JobEntity getJob() {

        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
