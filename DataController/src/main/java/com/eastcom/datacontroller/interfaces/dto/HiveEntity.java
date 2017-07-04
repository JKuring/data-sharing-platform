package com.eastcom.datacontroller.interfaces.dto;

/**
 * Created by linghang.kong on 2017/7/4.
 */
public interface HiveEntity extends BaseEntity<String>{

    public String getTableName();

    public void setTableName(String tableName);

    public String getSql();

    public void setSql(String sql);

    public String getPartition();

    public void setPartition(String partition);
}
