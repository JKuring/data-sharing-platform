package com.eastcom.dataloader.confparser

import com.eastcom.dataloader.context.Context


/**
  * Created by slp on 2016/2/18.
  * 实例化任务模板
  */
class SlsNode(config: String) {
  //    hive | spark_odc_data | d_enl_radius | p_hour| /jc_xngl/rawdata/cdr/radius/#{time yyyyMMdd}/#{time HH} | /jc_xngl/spark_odc_data/ext_d_enl_radius | | 1 | | 0
  private val Array(type_, schema,  table , partitions, xdrDir, loadingDir, loadedDir, loadFileOnce, tplCiCode , timeold_) = config.split("\\s*\\|\\s*", -1)
  //hivespark_odc_datad_enl_radiusp_hour/jc_xngl/rawdata/cdr/radius/#{time yyyyMMdd}/#{time HH}/jc_xngl/spark_odc_data/ext_d_enl_radius10

  private val tplname = table.replaceAll(":", "_")

  private val timeold = try {
    timeold_.toInt
  } catch {
    case e: Exception => 0
  }

  def getType = type_.toLowerCase

  def getTable = table.toLowerCase

  def getTplName = tplname.toLowerCase

  // 获取 模板Ci Code
  def getTplCiCode = tplCiCode

  def getPartitions = partitions.toLowerCase

  def getLoadFileOnce = loadFileOnce.toInt

  def getXdrDir = Context.replaceTimePlaceHolder(xdrDir)

  def getLoadingDir = Context.replaceTimePlaceHolder(loadingDir)

  def getLoadedDir = Context.replaceTimePlaceHolder(loadedDir)

  def getSchema = schema

  def getTimeOld = timeold
}
