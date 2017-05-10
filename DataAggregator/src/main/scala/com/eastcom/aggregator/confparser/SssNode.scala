package com.eastcom.aggregator.confparser

import scala.collection.mutable.ArrayBuffer

/**
  * Created by slp on 2016/2/18.
  */
class SssNode(config: String) {
  //hive|spark_odc_data|a_ls_sig_lte_all_h | a_lf_sig_lte_s1mme_h, a_lf_sig_user_http_h,a_lf_sig_user_dns_h,a_lf_sig_user_mms_h |p_hour,sec_partition|
  private val Array(type_, schema, table, oritables, partitions, tplCiCode) = config.replaceAll("\\s+", "").split("\\|", -1)
  private val oriTables = if (oritables != "") {
    val oris = ArrayBuffer[String]()
    oritables.split(",").foreach(x => {
      oris += x.toLowerCase
    })
    oris.toArray
  } else {
    Array[String]()
  }

  /**
    * comma-separated
    */

    //
    val tplname_ = ""
  private val tplname = (if (tplname_ != "") tplname_ else table).replaceAll(":", "_")

  def getType = type_.toLowerCase

  def getTable = table.toLowerCase

  // 获取 模板Ci Code
  def getTplCiCode = tplCiCode

  def getOriTables = oriTables

  def getTplName = tplname.toLowerCase

  def getPartitions = partitions.toLowerCase

  def getSchema = schema
}
