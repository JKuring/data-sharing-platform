package com.eastcom.aggregator

import akka.actor.{ActorSystem, Props}
import com.cloudera.spark.hbase.HBaseContext
import com.eastcom.aggregator.confparser.SssConfParser
import com.eastcom.aggregator.context.Context
import com.eastcom.aggregator.driver.SssDriver
import com.eastcom.aggregator.exception.SssException
import com.eastcom.aggregator.message.SssStartMessage
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.log4j.Logger
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by slp on 2016/2/17.
 */
object SssLauncher {
  val logging = Logger.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    // Read args from command line
    val Array(confFile, initCmdPath, tplPath, zookeeper_hosts, zookeeper_port, sessions, timeid, appIdDir, timeout) =
      if (args == null || args.isEmpty) {
        throw new SssException("parameter list ( confFile , udfPath , tplPath , zookeeper_hosts , zookeeper_port , sessions , timeid , timeout(min))")
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
    val sc = new SparkContext(sparkConf.setAppName(s"spark sql job at time=${timeid}"))

    Context.+(Context.sparkContext, sc)
    val sqlContext = new HiveContext(sc)
    Context.+(Context.hiveContext, sqlContext)

    sqlContext.setConf("hive.exec.dynamic.partition", "true")
    sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")

    if (zookeeper_hosts != "null") {
      val conf = HBaseConfiguration.create()
      conf.set("hbase.zookeeper.quorum", zookeeper_hosts)
      conf.set("hbase.zookeeper.property.clientPort", if (zookeeper_port == "null") "2181" else zookeeper_port)
      val hbaseContext = new HBaseContext(sc, conf)
      Context.+(Context.hbaseContext, hbaseContext)
    }

    {
      val appIdRdd = sc.parallelize(List(sc.applicationId), 1)
      appIdRdd.saveAsTextFile(appIdDir)
    }

    val sohJob = SssConfParser.parser(confFile, initCmdPath, tplPath, sessions.toInt, timeid)
    val system = ActorSystem(s"spark-sql-job-${timeid}")
    val masterRouter = system.actorOf(Props(new SssDriver(sohJob)), "sssMasterRouter")
    masterRouter ! SssStartMessage

    val timeoutMS = timeout.toLong * 60 * 1000
    val startTime = System.currentTimeMillis()
    while (!Context.isFinish && startTime - System.currentTimeMillis() <= timeoutMS) {
      Thread.sleep(3000l)
    }

    system.shutdown()
    sc.stop()
  }

}
