package com.eastcom.datacontroller.bean;

import com.eastcom.datacontroller.interfaces.dto.HiveEntity;

/**
 * Created by linghang.kong on 2017/7/4.
 */
public class HiveEntityImpl implements HiveEntity{

    private String tableName;
    private String sql;
    private String partition;

    public HiveEntityImpl() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public void setName(String name) {
        this.tableName = name;
    }
}