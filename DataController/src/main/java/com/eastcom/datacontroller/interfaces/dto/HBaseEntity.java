package com.eastcom.datacontroller.interfaces.dto;

import java.io.File;

/**
 * Created by linghang.kong on 2016/12/22.
 */
public interface HBaseEntity extends BaseEntity {


    public String[] getColumns();

    public void setColumns(String[] columns);

    public int getVersion();

    public void setVersion(int version);

    public String getCompressionType();

    public void setCompressionType(String compressionType);

    public int getTtl();

    public void setTtl(int ttl);

    public String getSplitPolicy();

    public void setSplitPolicy(String splitPolicy);

    public File getSpiltKeysFile();

    public void setSpiltKeysFile(File spiltKeysFile);

    public String getCoprocessor();

    public void setCoprocessor(String coprocessor);

    public boolean isCurrentIsCreated();

    public void setCurrentIsCreated(boolean currentIsCreated);

}
