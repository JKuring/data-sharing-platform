package com.eastcom.dataloader.bean;


import com.eastcom.dataloader.interfaces.dto.HBaseEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by linghang.kong on 2016/12/21.
 */
@Component
@Scope("prototype")
public class HBaseEntityImpl implements HBaseEntity {


//    private String importtsv_columns;
//    private String importtsv_bulk_output;
//    private String importtsv_mapper_class;
//    private String mapreduce_map_memory_mb;
//    private String hbase_client_retries_number;
//    private String importtsv_rowkey_indexs; // row key 的位置
//    private String importtsv_rowkey_strategies; // 操作
//    private String importtsv_rowkey_encrypts; // 加密

    private String tableName;


    private String[] columns;
    private int version;
    private String compressionType;
    private int ttl;
    private String splitPolicy;
    private File spiltKeysFile;
    private String coprocessor;

    private boolean currentIsCreated = false;

    public HBaseEntityImpl(String tableName) {
        this.tableName = tableName;
    }

    public String getName() {
        return this.tableName;
    }

    public void setName(String name) {
        this.tableName = name;


    }


    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public String getSplitPolicy() {
        return splitPolicy;
    }

    public void setSplitPolicy(String splitPolicy) {
        this.splitPolicy = splitPolicy;
    }

    public File getSpiltKeysFile() {
        return spiltKeysFile;
    }

    public void setSpiltKeysFile(File spiltKeysFile) {
        this.spiltKeysFile = spiltKeysFile;
    }

    public String getCoprocessor() {
        return coprocessor;
    }

    public void setCoprocessor(String coprocessor) {
        this.coprocessor = coprocessor;
    }


    public boolean isCurrentIsCreated() {
        return currentIsCreated;
    }

    public void setCurrentIsCreated(boolean currentIsCreated) {
        this.currentIsCreated = currentIsCreated;
    }
}