package com.eastcom.datacontroller.service;

import com.eastcom.datacontroller.bean.HiveJobs;
import com.eastcom.datacontroller.interfaces.dao.HiveDao;
import com.eastcom.datacontroller.interfaces.dto.HiveEntity;
import com.eastcom.datacontroller.interfaces.service.HiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
            hiveDao.delete(hiveEntity);
        }
    }

    @Override
    public void close() throws IOException {

    }


}
