package com.eastcom.datacontroller.bean;


import com.eastcom.datacontroller.interfaces.dto.HBaseEntity;

import java.io.File;
import java.util.Arrays;

/**
 * Created by linghang.kong on 2016/12/21.
 * the class implement {@link HBaseEntity},
 * it is some information of hbase table that describe the table construct.
 */
public class HBaseEntityImpl implements HBaseEntity {

    private String name;
    private String[] columns;
    private int version;
    private String compressionType;
    private int ttl;
    private String splitPolicy;
    private File spiltKeysFile;
    private String coprocessor;

    private boolean currentIsCreated = false;


    public HBaseEntityImpl() {
    }

    public HBaseEntityImpl(String tableName) {
        this.name = tableName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;


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

    @Override
    public String toString() {
        return "HBaseEntityImpl{" +
                "name='" + name + '\'' +
                ", columns=" + Arrays.toString(columns) +
                ", version=" + version +
                ", compressionType='" + compressionType + '\'' +
                ", ttl=" + ttl +
                ", splitPolicy='" + splitPolicy + '\'' +
                ", spiltKeysFile=" + spiltKeysFile +
                ", coprocessor='" + coprocessor + '\'' +
                ", currentIsCreated=" + currentIsCreated +
                '}';
    }
}