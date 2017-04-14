package com.eastcom.dataloader

import akka.actor.{ActorSystem, Props}
import com.cloudera.spark.hbase.HBaseContext
import com.eastcom.dataloader.confparser.SlsConfParser
import com.eastcom.dataloader.context.Context
import com.eastcom.dataloader.driver.SlsDriver
import com.eastcom.dataloader.exception.SlsException
import com.eastcom.dataloader.message.SlsStartMessage
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.log4j.Logger
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by slp on 2016/2/17.
  */
object SlsLauncher {
  private final val logging = Logger.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    // Read args from command line
    //    	$CONF_PATH \ hive | spark_odc_data | d_enl_radius | p_hour, p_sec | /jc_xngl/rawdata/cdr/radius/#{time yyyyMMdd}/#{time HH}/#{time mm} | /jc_xngl/spark_odc_data/ext_d_enl_radius | | 1 | | 0
    //      $UDF_PATH \ 无用
    //      $TPL_PATH \ 无用
    //      $ZOOKEEPER_HOSTS \
    //      $ZOOKEEPER_PORT \
    //      $APP_ID_DIR \ app id 存放hdfs上的位置，应用于yarn logs 用
    //      $TIME 一个推后15分钟的格式化时间 20170317100027
    val Array(confFile, initCmdPath, tplPath, zookeeper_hosts, zookeeper_port, timeid) =
    if (args == null || args.isEmpty) {
      //        Array("D:\\ideaProjects\\soh\\src\\main\\config\\load.conf","D:\\ideaProjects\\soh\\src\\main\\config\\init.conf", "D:\\ideaProjects\\soh\\src\\main\\config\\tpl", "hadoop01,hadoop03,hadoop04", "2181", "10", "201602221500")
      throw new SlsException("parameter list ( confFile , udfPath , tplPath , zookeeper_hosts , zookeeper_port)")
    } else {
      args
    }

    // 配置spark configuration
    val sparkConf = new SparkConf()
    //    sparkConf.setMaster(confProperties.getMaster)
    //    val properties = confProperties.getParopertiesMap
    //    for (key <- properties){
    //      sparkConf.set(key,properties.get(key))
    //    }
    // 创建SparkContext
    val sc = new SparkContext(sparkConf.setAppName(s"spark load job at time=${timeid}"))

    // 添加SparkContext
    Context.+(Context.sparkContext, sc)
    // 创建HiveContext
    val sqlContext = new HiveContext(sc)
    // 添加HiveContext
    Context.+(Context.hiveContext, sqlContext)

    sqlContext.setConf("hive.exec.dynamic.partition", "true")
    sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")

    if (zookeeper_hosts != "null") {
      val conf = HBaseConfiguration.create()
      conf.set("hbase.zookeeper.quorum", zookeeper_hosts)
      conf.set("hbase.zookeeper.property.clientPort", if (zookeeper_port == "null") "2181" else zookeeper_port)
      val hbaseContext = new HBaseContext(sc, conf)
      logging.warn(s"hbase.zookeeper.quorum=${zookeeper_hosts};hbase.zookeeper.property.clientPort=${zookeeper_port}")
      Context.+(Context.hbaseContext, hbaseContext)
    }

    // 记录application id 使用yarn 日志来查找
    //    {
    //    val appIdRdd = sc.parallelize(List(sc.applicationId), 1)
    //      appIdRdd.saveAsTextFile(appIdDir)
    //    }

    Context.timeid = timeid

    //根据配置文件来创建多个任务的job
    val sohJob = SlsConfParser.parser(confFile, initCmdPath, tplPath)

    val system = ActorSystem(s"spark-load-job-${timeid}")
    // 创建ActorRef
    val masterRouter = system.actorOf(Props(new SlsDriver(sohJob)), "slsMasterRouter")
    // 发送message并马上返回
    masterRouter ! SlsStartMessage

    while (!Context.isFinish) {
      Thread.sleep(1000l)
    }
  }
}

