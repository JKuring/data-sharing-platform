package com.eastcom.dataloader

import org.apache.log4j.Logger
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by linghang.kong on 2017/4/19.
  */
object TestSpark {

  private final val logging = Logger.getLogger(getClass)


  def main(args: Array[String]): Unit = {
    // 配置spark configuration
    val sparkConf = new SparkConf()

    logging.info(s"Spark log dir: ${sparkConf.get("spark.eventLog.dir")}")

    // 创建SparkContext
    val sc = new SparkContext(sparkConf.setAppName(s"spark load job at time=${System.currentTimeMillis()}"))

    // 创建HiveContext
    val sqlContext = new HiveContext(sc)


    sqlContext.sql(args(0))
    sc.stop();
  }
}
