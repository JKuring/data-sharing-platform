package com.eastcom.datacontroller.interfaces.dto;

import java.io.File;

/**
 * Created by linghang.kong on 2016/12/22.
 * hbase table construct
 */
public interface HBaseEntity extends BaseEntity<String> {


    String[] getColumns();

    void setColumns(String[] columns);

    int getVersion();

    void setVersion(int version);

    String getCompressionType();

    void setCompressionType(String compressionType);

    int getTtl();

    void setTtl(int ttl);

    String getSplitPolicy();

    void setSplitPolicy(String splitPolicy);

    File getSpiltKeysFile();

    void setSpiltKeysFile(File spiltKeysFile);

    String getCoprocessor();

    void setCoprocessor(String coprocessor);

    boolean isCurrentIsCreated();

    void setCurrentIsCreated(boolean currentIsCreated);

}
