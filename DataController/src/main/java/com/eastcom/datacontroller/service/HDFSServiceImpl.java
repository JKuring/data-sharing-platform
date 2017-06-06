package com.eastcom.datacontroller.service;

import com.eastcom.datacontroller.interfaces.dao.HDFSDao;
import com.eastcom.datacontroller.interfaces.service.HDFSService;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by linghang.kong on 2017/5/9.
 */
@Service
@Lazy
public class HDFSServiceImpl implements HDFSService {

    private static final Logger logger = LoggerFactory.getLogger(HDFSServiceImpl.class);

    @Autowired
    private HDFSDao<Path> hdfsDao;


    public HDFSServiceImpl() {
    }


    @Override
    public void createDir(Path path) {
        try {
            hdfsDao.create(path);
        } catch (IOException e) {
            logger.error("Failed to create dir {}.", path.getName(), e.fillInStackTrace());
        }
    }

    @Override
    public void deleteDir(Path path) {
        try {
            hdfsDao.delete(path);
        } catch (IOException e) {
            logger.error("Failed to delete dir {}.", path.getName(), e.fillInStackTrace());
        }

    }
}
