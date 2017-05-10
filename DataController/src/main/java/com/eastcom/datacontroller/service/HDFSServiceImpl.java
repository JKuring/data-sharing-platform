package com.eastcom.datacontroller.service;

import com.eastcom.datacontroller.interfaces.service.HDFSService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by linghang.kong on 2017/5/9.
 */
public class HDFSServiceImpl implements HDFSService {

    private static final Logger logger = LoggerFactory.getLogger(HDFSServiceImpl.class);

    private FileSystem fileSystem;

    public HDFSServiceImpl(Configuration configuration) throws IOException {
        this.fileSystem = FileSystem.get(configuration);
    }

    @Override
    public void createDir(Path path) throws IOException {
        createDir(path,false);
    }


    public void createDir(Path path, boolean overwrite) throws IOException {
        this.fileSystem.create(path,overwrite);
        logger.info("Finish creating the dir of {}.",path.getName());
    }

    /**
     * Don't recur the dir with children.
     * @param path Need to delete relative path
     * @throws IOException
     */
    @Override
    public void deleteDir(Path path) throws IOException {
        deleteDir(path, false);
    }

    public void deleteDir(Path path, boolean recursive) throws IOException{
        if (!this.fileSystem.delete(path,recursive)) throw new IOException("Could not delete " + path);
        logger.info("Finish deleting the dir of {}.",path.getName());
    }
}
