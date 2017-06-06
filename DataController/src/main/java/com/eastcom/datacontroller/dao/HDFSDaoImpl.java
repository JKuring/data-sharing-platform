package com.eastcom.datacontroller.dao;

import com.eastcom.datacontroller.interfaces.dao.HDFSDao;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by linghang.kong on 2017/1/20.
 */
public class HDFSDaoImpl implements HDFSDao<Path> {

    private static final Logger logger = LoggerFactory.getLogger(HDFSDaoImpl.class);

    private Configuration configuration;

    private FileSystem fileSystem;

    public HDFSDaoImpl(Configuration configuration) throws IOException {
        this.configuration = configuration;
        this.fileSystem = FileSystem.get(configuration);
    }


    @Override
    public Path get(Class<Path> entityClazz, Serializable id) {
        return null;
    }

    @Override
    @Deprecated
    public Serializable save(Path entity) {
        return null;
    }

    @Override
    public void update(Path entity) {

    }

    @Override
    public void delete(Path path) throws IOException {
        delete(path, true);
    }

    public void delete(Path path, boolean recursive) throws IOException {
        if (!this.fileSystem.delete(path, recursive)) throw new IOException("Could not delete " + path);
        logger.info("Finish deleting the dir of {}.", path.getName());
    }

    @Override
    public void delete(Class<Path> entityClazz, Serializable id) {

    }

    @Override
    public List<Path> findAll(Class<Path> entityClazz) {
        return null;
    }

    @Override
    public long findCount(Class<Path> entityClazz) {
        return 0;
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public FileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public void create(Path path) throws IOException {
        create(path, false);
    }

    public void create(Path path, boolean overwrite) throws IOException {
        this.fileSystem.create(path, overwrite);
        logger.info("Finish creating the dir of {}.", path.getName());
    }

    @Override
    public void close() throws IOException {
        this.fileSystem.close();
    }
}
