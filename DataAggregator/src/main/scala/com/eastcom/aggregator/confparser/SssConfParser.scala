package com.eastcom.aggregator.confparser


import com.eastcom.common.service.HttpRequestUtils

/**
  * Created by slp on 2016/2/18.
  */
object SssConfParser {
  // conf
  //hive|spark_odc_data|a_dm_re_ba_sbc_d||p_day|
  //hive|spark_odc_data|a_dm_re_ba_ippbx_d||p_day|
  def parser(confFile: String, initCmdPath: String, tplPath: String, sessions: Int, timeid: String): SssJob = {
    val job = new SssJob(initCmdPath, tplPath, sessions, timeid)
//    val sc = Context.getContext(Context.sparkContext).asInstanceOf[SparkContext]
//    sc.textFile(confFile).collect().foreach(config => {
//      if (config != null && config.trim != "" && !config.startsWith("#")) {
//        job + new SssNode(config)
//      }
//    })
    //    Source.fromFile(confFile).getLines().foreach(config => {
    //      if (config != null && config.trim != "" && !config.startsWith("#")) {
    //        job + new SssNode(config)
    //      }
    //    })

    // http
    HttpRequestUtils.httpGet(confFile, "".getClass).split("\\n").foreach(config => {
      if (config != null && config.trim != "" && !config.startsWith("#")) {
        job + new SssNode(config)
      }
    })

    job.check
  }
}
