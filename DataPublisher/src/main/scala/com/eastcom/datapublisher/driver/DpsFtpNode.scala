package com.eastcom.datapublisher.driver


/**
  *
  * @param configServiceUrl 配置服务 URL
  * @param tplCiCode        取数模板 ciCode
  * @param hdfsExportPath   中间输出HDFS 文件目录
  * @param timeid           取数时间
  */
class DpsFtpNode(configServiceUrl: String, tplCiCode: String, hdfsExportPath: String, timeid: String)
  extends DpsNode(configServiceUrl, tplCiCode, hdfsExportPath, timeid) {

}

