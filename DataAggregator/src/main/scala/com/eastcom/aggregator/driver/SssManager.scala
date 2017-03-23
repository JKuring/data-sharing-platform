package com.eastcom.aggregator.driver

import java.util.Date

import akka.actor.Actor
import com.eastcom.aggregator.confparser.SssNode
import com.eastcom.aggregator.context.Context
import com.eastcom.aggregator.message.{SssJobMessage, SssResultMessage}
import org.apache.log4j.Logger
import org.springframework.amqp.core.MessageProperties

/**
 * Created by slp on 2016/2/18.
 */
class SssManager(tplPath: String, timeid: String) extends Actor {
  private val logging = Logger.getLogger(getClass)
  private val hiveExecutor = new SssHiveExecutor(tplPath, timeid)
  private val hbaseExecutor = new SssHbaseExecutor(tplPath, timeid)
  private val hiveOldExecutor = new SssHiveOldExecutor(tplPath, timeid)
//  private val mqProducer= CommonMeaageProducer.producerCollection.get("q_aggr_spark")

  private val startTime = "startTime"
  private val endTime = "endTime"
  private val status = "status"

  override def receive: Receive = {
    case SssJobMessage(node: SssNode) => {
      try {
        val startTime = new Date().getTime
        logging.info(s" [ SSS_JOB ] [ ${node.getType} ] Start exec job with table [ ${node.getTplName} ] at time [ $timeid ] ...")
        try
          exec(node)
//          mqProducer.send(new Message(("").getBytes, message.getMessageProperties))
        catch {
          case e: Exception => {
            Thread.sleep(30000l)
            exec(node)
          }
        }
        val eastime = (new Date().getTime - startTime) / 1000
        logging.info(s" [ SSS_JOB ] [ ${node.getType} ] Finish exec job with table [ ${node.getTplName} ] at time [ $timeid ] eastime [ $eastime ] s !")
      } catch {
        case e: Exception => logging.error(s" [ SSS_JOB ] [ ${node.getType} ] Exec job with table [ ${node.getTplName} ] at time [ $timeid ] fail !!!", e)
      }
      context.sender() ! SssResultMessage(node)
    }
  }

  private def exec(node: SssNode): Unit = {
    if (Context.hiveTypeOld == node.getType) {
      hiveOldExecutor.executor(node)
    } else if (Context.hbaseType == node.getType) {
      hbaseExecutor.executor(node)
    } else if (Context.hiveType == node.getType) {
      hiveExecutor.executor(node);
    }
  }

  private def getMessageProperties(messageProperties: MessageProperties, result: Int) = {
    messageProperties.setHeader(endTime, System.currentTimeMillis)
    messageProperties.setHeader(status, result)
    messageProperties
  }
}
