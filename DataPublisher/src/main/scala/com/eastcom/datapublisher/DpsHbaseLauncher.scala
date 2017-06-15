package com.eastcom.datapublisher

import akka.actor.{ActorSystem, Props}
import com.eastcom.datapublisher.context.AppContext
import com.eastcom.datapublisher.driver.{DpsHbaseDriver, DpsHbaseNode}
import com.eastcom.datapublisher.exception.DpsException
import com.eastcom.datapublisher.message.DpsStartMessage
import com.eastcom.datapublisher.utils.{CredentialsFromLocalPath, HBaseContextCluster}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.mapred.JobConf
import org.apache.log4j.Logger
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}


object DpsHbaseLauncher {
  private final val logging = Logger.getLogger(getClass)

  def main(args: Array[String]): Unit = {

    val localPath = "/tmp/token"


    if (args == null || args.isEmpty)
      throw new DpsException("parameter list shoud be ( exportHdfsPath, hbaseTableName, configServiceUrl, zookeeper_hosts, zookeeper_port, timeid)")


    val Array(configServiceUrl, tplCiCode, hdfsExportPath, hbaseTableName, timeid, path) = args

    // 配置spark configuration
    val sparkConf = new SparkConf()

    // 创建SparkContext
    val sc = new SparkContext(sparkConf.setAppName(s"spark publish hbase table [ ${hbaseTableName} ] job at time=${timeid}"))

    // 添加SparkContext
    AppContext.+(AppContext.sparkContext, sc)
    // 创建HiveContext
    val sqlContext = new HiveContext(sc)
    // 添加HiveContext
    AppContext.+(AppContext.hiveContext, sqlContext)

    sqlContext.setConf("hive.exec.dynamic.partition", "true")
    sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")

    // 创建 HBaseContext
    val conf = HBaseConfiguration.create()
    val fileSystem = FileSystem.get(conf)
    try {
      fileSystem.copyToLocalFile(false, new Path(path), new Path(localPath))
    } catch {
      case e: Exception => {
        logging.error(e)
        Thread.sleep(1000l)
        //
        fileSystem.copyToLocalFile(false, new Path(path), new Path(localPath))
      }
    }
    val credentialsFromLocalPath = new CredentialsFromLocalPath(conf, path, localPath)
    val jobConf = new JobConf(conf)
    jobConf.setCredentials(credentialsFromLocalPath.getCredential())
    val hbaseContext = new HBaseContextCluster(sc, conf, credentialsFromLocalPath.getStrToken())
    AppContext.+(AppContext.hbaseContext, hbaseContext)

    AppContext.timeid = timeid

    val system = ActorSystem(s"spark-publish-job-${timeid}")

    //创建任务
    val pubNode = new DpsHbaseNode(hdfsExportPath, configServiceUrl, tplCiCode, timeid, hbaseTableName)

    // 创建ActorRef
    val masterRouter = system.actorOf(Props(new DpsHbaseDriver(pubNode)), "dpsMasterRouter")
    // 发送message并马上返回
    masterRouter ! DpsStartMessage

    while (!AppContext.isFinish()) {
      Thread.sleep(2000l)
    }
  }
}

