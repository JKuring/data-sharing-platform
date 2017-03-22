package com.eastcom.dataloader.driver

import java.util.Date

import akka.actor.{Actor, Props}
import akka.routing.RoundRobinRouter
import com.eastcom.dataloader.confparser.{SlsJob, SlsNode}
import com.eastcom.dataloader.context.Context
import com.eastcom.dataloader.exception.SlsException
import com.eastcom.dataloader.message.{SlsJobMessage, SlsResultMessage, SlsStartMessage}
import com.eastcom.dataloader.utils.hdfs.filefilter.{NonTmpFileFilter, NonTmpOldFileFilter}
import com.eastcom.dataloader.utils.hdfs.util.FileHelper
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.hive.HiveContext

import scala.collection.mutable.ListBuffer


/**
 * Created by slp on 2016/2/18.
  * job 的驱动器
 */
class SlsDriver(val job: SlsJob) extends Thread with Actor {
  val logging = Logger.getLogger(getClass)
  val sqlContext = Context.getContext(Context.hiveContext).asInstanceOf[HiveContext]
  val workerRouter = context.actorOf(Props(new SlsManager(job.tplPath)).withRouter(RoundRobinRouter(job.getSessions)), "slsWorkerRouter")
  val sparkContext = Context.getContext(Context.sparkContext).asInstanceOf[SparkContext]
  val nodeDataJob = ListBuffer[String]()
  val fs = FileSystem.get(sparkContext.hadoopConfiguration)

  override def run(): Unit = {
    initUdf()

    // 遍历任务
    job.nodesMap.foreach(x => {
      try {
        createDirIfNotExists(x._2.getXdrDir)
        createDirIfNotExists(x._2.getLoadingDir)
        createDirIfNotExists(x._2.getLoadedDir)

        val n = x._2;
        try {
//          timeOld = 0
          val fileStatuss = if (n.getTimeOld <= 0) {
            FileHelper.listStatus(fs, new Path(n.getXdrDir), new NonTmpFileFilter())
          } else {
            FileHelper.listStatus(fs, new Path(n.getXdrDir), new NonTmpOldFileFilter(n.getLoadFileOnce, new Date().getTime - n.getTimeOld * 1000, fs))
          }
          logging.info("Dir: " + x._2.getXdrDir + ", FileSize: " + fileStatuss.size);
          // 有文件状态，代表有文件
          if (fileStatuss.length > 0) {
            workerRouter ! SlsJobMessage(n, fileStatuss)
          } else {
            nodeDataJob += x._1;
          }
        } catch {
          case e: Exception => logging.error(s" [ SLS_JOB ] [ ${n.getType} ] Exec job with table [ ${n.getTplName} ] fail !!!", e)
        }
      } catch {
        case e: Exception => logging.error(s" [ SLS_JOB ] [ ${x._2.getType} ] Exec job with table [ ${x._2.getTplName} ] fail !!!", e)
      }
    })

    shutdown()
  }

  def shutdown() = {
    while (nodeDataJob.size < job.nodesMap.size) {
      Thread.sleep(1000l)
    }
    context.system.shutdown()
    Context.shutdown()
  }

  private def initUdf(): Unit = {
    if (sqlContext == null) {
      throw new SlsException("HiveContext is not initialization!!!")
    }
    val sc = Context.getContext(Context.sparkContext).asInstanceOf[SparkContext]
    sc.textFile(job.initCmdPath).collect().foreach(udf => {
      if (udf != null && udf.trim != "" && !udf.startsWith("#")) {
        sqlContext.sql(udf)
      }
    })
  }

  override def receive: Actor.Receive = {
    case SlsResultMessage(node: SlsNode) => {
      nodeDataJob += node.getTplName
    }

    case SlsStartMessage => {
      this.start()
    }
  }

  def createDirIfNotExists(str: String) = {
    if (str != null && str != "") {
      val path = new Path(str)
      if (!fs.exists(path)) {
        fs.mkdirs(path)
      }
    }
  }

}

