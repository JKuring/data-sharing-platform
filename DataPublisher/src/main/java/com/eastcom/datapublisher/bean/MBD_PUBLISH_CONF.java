package com.eastcom.datapublisher.bean;

/**
 * Created by linghang.kong on 2017/4/27.
 */
public class MBD_PUBLISH_CONF {
    private String catalogId;

    //发布数据表源表 ( parquet  表)
    private String realTableName;

    //发布类型 :     1  -  FTP     2 -  HBase
    private Integer pubType;

    private String ftpServer;
    private String ftpAccount;
    private String ftpPsw;
    //文件路径表达式 ，包括FTP 路径和文件名称 ,其中时间可变部分，需根据时间表达式替换
    private String ftpPathExpr;

    //是否发送 ESB 通知
    private Integer notityEsb;

    //ESB 规约编码
    private String esbCode;

    //仅发布类型为 HBase时 需要填写
    private String hbaseTableName;

    //HIVE 表 HDFS 路径表达式
    private String hdfsExportPath;

    //实体表所在SCHEMA
    private String schema;

    //导出表所在schema
    private String exportSchema;

    private String exportTableName;

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getRealTableName() {
        return realTableName;
    }

    public void setRealTableName(String realTableName) {
        this.realTableName = realTableName;
    }

    public Integer getPubType() {
        return pubType;
    }

    public void setPubType(Integer pubType) {
        this.pubType = pubType;
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public String getFtpAccount() {
        return ftpAccount;
    }

    public void setFtpAccount(String ftpAccount) {
        this.ftpAccount = ftpAccount;
    }

    public String getFtpPsw() {
        return ftpPsw;
    }

    public void setFtpPsw(String ftpPsw) {
        this.ftpPsw = ftpPsw;
    }

    public String getFtpPathExpr() {
        return ftpPathExpr;
    }

    public void setFtpPathExpr(String ftpPathExpr) {
        this.ftpPathExpr = ftpPathExpr;
    }

    public Integer getNotityEsb() {
        return notityEsb;
    }

    public void setNotityEsb(Integer notityEsb) {
        this.notityEsb = notityEsb;
    }

    public String getEsbCode() {
        return esbCode;
    }

    public void setEsbCode(String esbCode) {
        this.esbCode = esbCode;
    }

    public String getHbaseTableName() {
        return hbaseTableName;
    }

    public void setHbaseTableName(String hbaseTableName) {
        this.hbaseTableName = hbaseTableName;
    }

    public String getHdfsExportPath() {
        return hdfsExportPath;
    }

    public void setHdfsExportPath(String hdfsExportPath) {
        this.hdfsExportPath = hdfsExportPath;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getExportSchema() {
        return exportSchema;
    }

    public void setExportSchema(String exportSchema) {
        this.exportSchema = exportSchema;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublishConf [catalogId=").append(catalogId).append(", realTableName=").append(realTableName)
                .append(", pubType=").append(pubType)
                .append(", ftpServer=").append(ftpServer).append(", ftpAccount=").append(ftpAccount).append(", ftpPsw=")
                .append(ftpPsw).append(", ftpPathExpr=").append(ftpPathExpr).append(", notityEsb=").append(notityEsb)
                .append(", esbCode=").append(esbCode).append(", hbaseTableName=").append(hbaseTableName).append("]");
        return builder.toString();
    }

    public String getExportTableName() {
        return exportTableName;
    }

    public void setExportTableName(String exportTableName) {
        this.exportTableName = exportTableName;
    }


}
