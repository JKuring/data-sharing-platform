package com.eastcom.datacontroller.bean;

/**
 * Created by linghang.kong on 2017/6/27.
 */
public class HiveJobs {

    private String[] tableName;
    private String sql;
    private String partition;

    private boolean df = false;

    public HiveJobs() {
    }

    public String[] getTableName() {
        return tableName;
    }

    public void setTableName(String[] tableName) {
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

    public boolean isDf() {
        return df;
    }

    public void setDf(boolean df) {
        this.df = df;
    }
}
