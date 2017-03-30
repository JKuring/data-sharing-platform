package com.eastcom.dataloader.confparser

import com.eastcom.dataloader.context.Context
import org.apache.spark.SparkContext

/**
  * Created by slp on 2016/2/18.
  */
object SlsConfParser {
  def parser(confFile: String, initCmdPath: String, tplPath: String): SlsJob = {
    val job = new SlsJob(initCmdPath, tplPath)
    // 获取 spark context
    val sc = Context.getContext(Context.sparkContext).asInstanceOf[SparkContext]
    //load_spark_data.conf
    //    hive | spark_odc_data | d_enl_radius | p_hour| /jc_xngl/rawdata/cdr/radius/#{time yyyyMMdd}/#{time HH} | /jc_xngl/spark_odc_data/ext_d_enl_radius | | 1 | | 0
    sc.textFile(confFile).collect().foreach(config => {
      if (config != null && config.trim != "" && !config.startsWith("#")) {
        job + new SlsNode(config)
      }
    })
    // 返回job（tasks）
    job
  }
}
