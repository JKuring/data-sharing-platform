package com.eastcom.aggregator.driver

import akka.actor.{Actor, Props}
import akka.routing.RoundRobinRouter
import com.eastcom.aggregator.bean.MQConf
import com.eastcom.aggregator.confparser.{SssJob, SssNode}
import com.eastcom.aggregator.context.Context
import com.eastcom.aggregator.exception.SssException
import com.eastcom.aggregator.message.{SssJobMessage, SssResultMessage, SssStartMessage}
import com.eastcom.common.service.HttpRequestUtils
import org.apache.log4j.Logger
import org.apache.spark.sql.hive.HiveContext

import scala.collection.mutable.{ListBuffer, Map}

/**
  * Created by slp on 2016/2/18.
  */
class SssDriver(val job: SssJob, val mqConf: MQConf, val headProperties: Array[String]) extends Thread with Actor {

  val logging = Logger.getLogger(getClass)

  val sqlContext = Context.getContext(Context.hiveContext).asInstanceOf[HiveContext]
  val workerRouter = context.actorOf(Props(new SssManager(job.tplPath, job.timeid, mqConf, headProperties)).withRouter(RoundRobinRouter(job.sessions)), "sssWorkerRouter")

  val finishNodes = ListBuffer[String]()
  val tplNodes = Map[String, SssNode]()
  val execTplNodes = {
    val nodes = ListBuffer[String]()
    job.nodesMap.foreach(ns => {
      ns._2.nodesList.foreach(n => {
        tplNodes += (n.getTplName -> n)
        if (n.getOriTables == null || n.getOriTables.isEmpty) {
          nodes += n.getTplName
        }
      })
    })
    nodes
  }

  override def run(): Unit = {
    try {
      initUdf()
      while (finishNodes.size < job.nodesMap.size) {
        if (execTplNodes.nonEmpty) {
          execTplNodes.toList.foreach(x => {
            val n = tplNodes.getOrElse(x, null)
            if (n != null) {
              workerRouter ! SssJobMessage(n)
              logging.info(s"execute tpl: ${n.getTplName}, finishNodes and Nodes size: ${finishNodes.size}, ${job.nodesMap.size}.")
            }
            execTplNodes -= x
          })
        } else {
          Thread.sleep(3000l)
        }
      }
      logging.info(s"Finish! FinishNodes and Nodes size: ${finishNodes.size}, ${job.nodesMap.size}.")
      finish()
    } catch {
      case e: Exception => logging.error("Failed tpl!", e)
    }
  }

  private def finish() = {
    Context.finish()
  }

  private def initUdf(): Unit = {
    if (sqlContext == null) {
      throw new SssException("HiveContext is not initialization!!!")
    }
    //    val sc = Context.getContext(Context.sparkContext).asInstanceOf[SparkContext]
    //    sc.textFile(job.initCmdPath).collect().foreach(udf => {
    //      if (udf != null && udf.trim != "" && !udf.startsWith("#")) {
    //        sqlContext.sql(udf)
    //      }
    //    })
    //    Source.fromFile(job.initCmdPath).getLines().foreach(udf => {
    //      if (udf != null && udf.trim != "" && !udf.startsWith("#")) {
    //        sqlContext.sql(udf)
    //      }
    //    })

    // http
    HttpRequestUtils.httpGet(job.initCmdPath, "".getClass).split("\\n").foreach(udf => {
      if (udf != null && udf.trim != "" && !udf.startsWith("#")) {
        sqlContext.sql(udf)
      }
    })

  }

  override def receive: Actor.Receive = {
    case SssResultMessage(node: SssNode) => {
      val f = job.nodesMap.getOrElse(node.getTable, null)
      if (f != null) {
        f.finishOne()
        if (f.isFinish()) {
          finishNodes += node.getTable
          f.getTagTplTables.foreach(x => {
            val n = tplNodes.getOrElse(x, null)
            if (n != null) {
              if (n.getOriTables.isEmpty) {
                execTplNodes += n.getTplName
              } else {
                var flag = true
                n.getOriTables.foreach(y => {
                  if (!finishNodes.contains(y)) {
                    flag = false
                  }
                })
                if (flag) {
                  execTplNodes += n.getTplName
                }
              }
            }
          })
        }
      }
    }

    case SssStartMessage => {
      this.start()
    }
  }

}

