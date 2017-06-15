package com.eastcom.datapublisher.driver

import akka.actor.Actor
import com.eastcom.common.utils.hdfs.filefilter.NonTmpFileFilter
import com.eastcom.common.utils.hdfs.util.FileHelper
import com.eastcom.datapublisher.context.{AppContext, SqlFileParser}
import com.eastcom.datapublisher.message.DpsStartMessage
import com.eastcom.datapublisher.utils.HBaseContextCluster
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hbase.client.Put
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.{DataFrame, Row}


class DpsHbaseDriver(val node: DpsHbaseNode) extends Thread with Actor {

  val logging = Logger.getLogger(getClass)
  val sqlContext = AppContext.getContext(AppContext.hiveContext).asInstanceOf[HiveContext]
  val sparkContext = AppContext.getContext(AppContext.sparkContext).asInstanceOf[SparkContext]
  val hbaseContext = AppContext.getContext(AppContext.hbaseContext).asInstanceOf[HBaseContextCluster]


  val fs = FileSystem.get(sparkContext.hadoopConfiguration)


  override def run(): Unit = {

    try {
      //initUdf()
      try {
        // 切换DB
        //    sqlContext.sql(s"use ${node.getSchema}")
        // 解析sql语句，替换时间参数
        val sqlText = AppContext.getSql(node.getConfigServiceUrl, node.getTplCiCode)

        val sqls = SqlFileParser.parse(sqlText)

        var execCount = 0
        for (sql: String <- sqls) {
          logging.info(s"Execute Sql: $sql")
          val result = sqlContext.sql(sql)
          execCount += 1
          if (execCount == sqls.length) {
            putModeExecutor(result)
          }
        }
      }
      catch {
        case e: Exception => logging.error(s" [ DPS_JOB ] Exec job with table [ ${node.getHbaseTableName} ] fail !!!", e)
      }
    } catch {
      case e: Exception => {
        logging.error(s"Prepare Publish  ${node.getHbaseTableName} false, shutdown the current service", e)
        shutdown()
      }
      case e: Throwable => {
        logging.error(s"Prepare Publish  ${node.getHbaseTableName} false, shutdown the current service", e)
        shutdown()
      }
    }
    finally {
      shutdown()
    }
  }

  def putModeExecutor(result: DataFrame) = {

    val column = new Column("cf:")

    hbaseContext.bulkPut(result.rdd, node.getHbaseTableName,
      (row: Row) => {
        val put = new Put(row.getString(0).getBytes(AppContext.character))
        put.setWriteToWAL(false)
        put.add(column.family, column.qualifier, row.getString(1).getBytes(AppContext.character))
        put
      },
      autoFlush = false)
  }

  def shutdown() = {
    context.system.shutdown()
    AppContext.shutdown()
  }

  override def receive: Actor.Receive = {
    case DpsStartMessage => {
      this.start()
    }
  }

  def bulkloadExecutor(result: DataFrame) = {

    // 创建中间输出目录
    FileHelp.createDirIfNotExists(fs, node.getHdfsExportPath)
    result.rdd.saveAsTextFile(node.getHdfsExportPath)

    val fileStatuss = FileHelper.listStatus(fs, new Path(node.getHdfsExportPath), new NonTmpFileFilter())
    if (fileStatuss.length > 0) {
      // TODO : not implement yet
    }
    else
      logging.error(s" [ DPS_JOB ]  Exec Publish job with table [ ${node.getHbaseTableName} ] abort , no data to process ...!");
  }

  //  private def initUdf(): Unit = {
  //    if (sqlContext == null) {
  //      throw new DpsException("HiveContext is not initialization!!!")
  //    }
  //
  //    // http
  //    HttpRequestUtils.httpGet(job.configServiceUrl + job.initCmdPath, "".getClass).split("\\n").foreach(udf => {
  //      if (udf != null && udf.trim != "" && !udf.startsWith("#")) {
  //        logging.info("init cmd: " + udf)
  //        sqlContext.sql(udf)
  //      }
  //    })
  //  }

  class Column(column: String) extends Serializable {
    val family = family_.trim.getBytes(AppContext.character)
    val qualifier = if (!"".equalsIgnoreCase(qualifier_)) {
      qualifier_.trim.getBytes(AppContext.character)
    } else {
      null
    }
    private val Array(family_, qualifier_) = column.split(":", -1)
  }

}
