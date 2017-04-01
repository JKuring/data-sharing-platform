package com.eastcom.dataloader.service;

import com.eastcom.common.utils.HBaseUtils;
import com.eastcom.dataloader.dao.HBaseDaoImpl;
import com.eastcom.dataloader.interfaces.dto.JobEntity;
import com.eastcom.dataloader.interfaces.service.HBaseService;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by linghang.kong on 2016/12/23.
 */
@Service
public class HBaseServiceImpl implements HBaseService<JobEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HBaseServiceImpl.class);

    private static final String HADOOP_USER_ROOT_PATH = "hadoop.user.root.path";
    private final long addition = 24 * 60 * 60 * 1000;
    private String HADOOP_USER_ROOT;
    @Autowired
    private HBaseDaoImpl hBaseDao;

    public HBaseServiceImpl() {
        String tmp = System.getProperty(HADOOP_USER_ROOT_PATH);
        this.HADOOP_USER_ROOT = tmp == null ? "" : tmp;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName() {

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

    public boolean partition(JobEntity jobEntity) {
        boolean result = false;
        String tableName = (String) jobEntity.getTableEntity();
        Map tableParametters = jobEntity.getPropertiesMap();
        String currentTimeTableName = HBaseUtils.getCurrentTimeTableName(tableName, jobEntity.getCreateTime(), jobEntity.getDelay(), jobEntity.getGranularity());
        String[] tmpTableName = tableName.split(":");
        String tmpPath = HBaseUtils.getCurrentTimePath(jobEntity.getCreateTime(), jobEntity.getDelay());
        String dataPath = HADOOP_USER_ROOT + jobEntity.getDataPath() + tmpPath;
        String outputPath = tableParametters.get("importtsv.bulk.output1") + "/" + tmpTableName[0] + "_" + tmpTableName[1];
        tableParametters.put("importtsv.bulk.output", outputPath + tmpPath);

        //加载为系统参数
        jobEntity.addSystemProperties(hBaseDao.getConfiguration());
        try {
            if (!HBaseUtils.createHFile(hBaseDao.getConfiguration(), currentTimeTableName, dataPath)) {
                logger.error("Upload data failing! Please clean dirty data, and try again later.");
            } else {
                Path hdfsPath = new Path(outputPath + tmpPath);
                if (HBaseUtils.upLoadHFile(hBaseDao.getConfiguration(),
                        (HTable) hBaseDao.getConnection().getTable(TableName.valueOf(currentTimeTableName)), hdfsPath)) {
                    result = true;
                }
            }
        } catch (Exception e) {
            logger.error("Upload data failing! Upload data to {}, the load path is {}.", currentTimeTableName, dataPath);
        }
        return result;
    }

    @Override
    public void createTable(JobEntity hBaseEntity) {

    }

    @Override
    public void delete(JobEntity hBaseEntity) {

    }

    public boolean createSchedulerJob(JobEntity jobEntity) {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}
