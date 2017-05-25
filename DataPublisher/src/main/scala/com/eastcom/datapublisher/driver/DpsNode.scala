package com.eastcom.datapublisher.driver

/**
  * Created by yuchi on 2017/5/24.
  */
class DpsNode(configServiceUrl: String, tplCiCode: String, hdfsExportPath: String, timeid: String) {

  def getConfigServiceUrl = configServiceUrl

  def getTplCiCode = tplCiCode

  def getHdfsExportPath = hdfsExportPath

  def getTimeid = timeid
}

