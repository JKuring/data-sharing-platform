package com.eastcom.datapublisher.driver

/**
  *
  * @param hdfsExportPath    HDFS 中间输出路径
  * @param hbaseTableName    HBASE 表名
  * @param configServiceUrl  配置服务
  * @param tplCiCode          取数模板 ciCode
  * @param timeid              取数时间
  */
class DpsHbaseNode( configServiceUrl : String, tplCiCode : String ,hdfsExportPath :String, timeid :String , hbaseTableName : String)
  extends DpsNode (configServiceUrl, tplCiCode, hdfsExportPath, timeid)
{
    def getHbaseTableName = hbaseTableName
}
