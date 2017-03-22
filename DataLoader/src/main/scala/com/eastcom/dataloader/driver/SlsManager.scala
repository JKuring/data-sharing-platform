package com.eastcom.dataloader.driver

import java.util.Date

import akka.actor.Actor
import com.eastcom.dataloader.confparser.SlsNode
import com.eastcom.dataloader.context.Context
import com.eastcom.dataloader.message.{SlsJobMessage, SlsResultMessage}
import org.apache.hadoop.fs.{FileStatus, FileSystem}
import org.apache.log4j.Logger
import org.apache.spark.SparkContext

/**
 * Created by slp on 2016/2/18.
 */
class SlsManager(tplPath: String) extends Actor {
  val logging = Logger.getLogger(getClass)
  val hiveExecutor = new SlsHiveExecutor(tplPath)
  val hbaseExecutor = new SlsHbaseExecutor(tplPath)
  val fs = FileSystem.get(sparkContext.hadoopConfiguration)
  lazy val sparkContext = Context.getContext(Context.sparkContext).asInstanceOf[SparkContext]

  override def receive: Receive = {
    case SlsJobMessage(node: SlsNode, fileStatuses: Array[FileStatus]) => {
      try {
        // 移动xdr文件到一个加载目录下
        FileHelp.moveFiles(fs, node.getLoadingDir, fileStatuses)
        val startTime = new Date().getTime
        logging.info(s" [ SLS_JOB ] [ ${node.getType} ] Start exec load job with table [ ${node.getTplName} ] ...")
        try {
          exec(node)
        } catch {
          case e: Exception => {
            Thread.sleep(30000l)
            exec(node)
          }
        }
        val eastime = (new Date().getTime - startTime) / 1000
        logging.info(s" [ SLS_JOB ] [ ${node.getType} ] Finish exec load job with table [ ${node.getTplName} ] with [${fileStatuses.length}] files eastime [ $eastime ] s !")
      } catch {
        case e: Exception => logging.error(s" [ SLS_JOB ] [ ${node.getType} ] Exec load job with table [ ${node.getTplName} ] fail !!!", e)
      } finally {
        if (node.getLoadedDir != "") {
          FileHelp.moveFiles(fs, node.getLoadingDir, node.getLoadedDir)
        } else {
          FileHelp.rmFiles(fs, node.getLoadingDir)
        }
      }
      context.sender ! SlsResultMessage(node)
    }
  }

  private def exec(node: SlsNode): Unit = {
    if (Context.hiveType == node.getType) {
      hiveExecutor.executor(node)
    } else if (Context.hbaseType == node.getType) {
      hbaseExecutor.executor(node)
    }
  }

}
