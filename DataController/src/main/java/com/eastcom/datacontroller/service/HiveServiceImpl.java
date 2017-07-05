package com.eastcom.datacontroller.service;

import com.eastcom.datacontroller.bean.HiveJobs;
import com.eastcom.datacontroller.interfaces.dao.HiveDao;
import com.eastcom.datacontroller.interfaces.dto.HiveEntity;
import com.eastcom.datacontroller.interfaces.service.HiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * Created by linghang.kong on 2017/7/4.
 */
@Service
public class HiveServiceImpl implements HiveService<HiveJobs> {

    private static final Logger logger = LoggerFactory.getLogger(HiveServiceImpl.class);

    @Autowired
    private HiveDao<HiveEntity> hiveDao;

    @Autowired
    private HiveEntity hiveEntity;

    @Resource(name = "partitionName")
    private String partitionName;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName() {

    }

    @Override
    public void delete(HiveJobs entity) throws IOException {

        for (String table : entity.getTableName()
                ) {
            hiveEntity.setTableName(table);
            hiveEntity.setPartition(entity.getPartition());
            hiveEntity.setSql(entity.getSql());
            if (!entity.isDf()) {
                hiveDao.delete(hiveEntity);
            }else {
                // 仅用于删除时间分区
                List<String> partitions = hiveDao.getPartitions(hiveEntity);
                if (partitions !=null){
                    int initPartition = subPartitionTime(hiveEntity.getPartition());
                    for (String partition: partitions
                         ) {
                        if (subPartitionTime(partition) <= initPartition){
                            hiveEntity.setPartition(partition);
                            hiveDao.delete(hiveEntity);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {

    }


    private int subPartitionTime(String partition){
        if (partition !=null) {
            return Integer.parseInt(partition.replaceAll(partitionName, ""));
        }
        return 0;
    }


}
