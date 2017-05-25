package com.eastcom.datacontroller.utilities;

import com.eastcom.datacontroller.bean.HBaseEntityImpl;
import com.eastcom.datacontroller.bean.HBaseJobs;
import com.eastcom.datacontroller.interfaces.dto.HBaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by linghang.kong on 2017/5/25.
 */
public class BuildHBaseEntity {

    private static final Logger logger = LoggerFactory.getLogger(BuildHBaseEntity.class);

    public static HBaseEntity getHBaseEntity(String tableName, HBaseJobs hbaseJobs) {
        HBaseEntity hbaseEntity = new HBaseEntityImpl();
        try {
            hbaseEntity.setName(tableName);
            hbaseEntity.setColumns(hbaseJobs.getColumns());
            hbaseEntity.setVersion(hbaseJobs.getVersion());
            hbaseEntity.setCompressionType(hbaseJobs.getCompressionType());
            hbaseEntity.setTtl(hbaseJobs.getTtl());
            hbaseEntity.setSplitPolicy(hbaseJobs.getSplitPolicy());
            hbaseEntity.setSpiltKeysFile(hbaseJobs.getSpiltKeysFile());
            hbaseEntity.setCoprocessor(hbaseJobs.getCoprocessor());
        }catch (Exception e){
            logger.error("Failed to build HBaseEntity.");
        }
        return hbaseEntity;
    }
}
