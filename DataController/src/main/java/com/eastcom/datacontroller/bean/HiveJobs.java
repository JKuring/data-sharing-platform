package com.eastcom.datacontroller.bean;

/**
 * Created by linghang.kong on 2017/6/27.
 */
public class HiveJobs {

    private String tableName;

    private String sql;

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
}
