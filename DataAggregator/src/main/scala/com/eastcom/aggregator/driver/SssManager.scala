package com.eastcom.aggregator.driver

import akka.actor.Actor
import com.eastcom.aggregator.bean.MQConf
import com.eastcom.aggregator.confparser.SssNode
import com.eastcom.aggregator.context.Context
import com.eastcom.aggregator.message.{SssJobMessage, SssResultMessage}
import com.eastcom.common.message.RabbitMQConnection
import com.eastcom.common.utils.parser.MqHeadParser
import com.eastcom.common.utils.time.TimeTransform
import com.rabbitmq.client.AMQP.BasicProperties
import org.apache.log4j.Logger

/**
  * Created by slp on 2016/2/18.
  */
class SssManager(tplPath: String, timeid: String, val mqConf: MQConf, val headProperties: Array[String]) extends Actor {
  private val logging = Logger.getLogger(getClass)
  private val hiveExecutor = new SssHiveExecutor(tplPath, timeid)
  private val hbaseExecutor = new SssHbaseExecutor(tplPath, timeid)
  private val hiveOldExecutor = new SssHiveOldExecutor(tplPath, timeid)
  private val mqConnection = new RabbitMQConnection(mqConf.getUserName, mqConf.getPassword, mqConf.getHost, Integer.parseInt(mqConf.getPort))

  private val startTime = "startTime"
  private val endTime = "endTime"
  private val status = "status"

  override def receive: Receive = {
    case SssJobMessage(node: SssNode) => {
      val connection = mqConnection.createConnection()
      val channel = mqConnection.getChannel(connection, mqConf.getExchange, mqConf.getRoutingKey)
      try {
        val startTime = new Date().getTime
        logging.info(s" [ SSS_JOB ] [ ${node.getType} ] Start exec job with table [ ${node.getTplName} ] at time [ $timeid ] ...")
        try {
          exec(node)
          channel.basicPublish(mqConf.getExchange, mqConf.getRoutingKey, false, getMessageProperties(headProperties, 2), "".getBytes)
        }catch {
          case e: Exception => {
            Thread.sleep(30000l)
            exec(node)
            channel.basicPublish(mqConf.getExchange, mqConf.getRoutingKey, false, getMessageProperties(headProperties, 2), "".getBytes)
          }
        }
        val eastime = (new Date().getTime - startTime) / 1000
        logging.info(s" [ SSS_JOB ] [ ${node.getType} ] Finish exec job with table [ ${node.getTplName} ] at time [ $timeid ] eastime [ $eastime ] s !")
      } catch {
        case e: Exception => logging.error(s" [ SSS_JOB ] [ ${node.getType} ] Exec job with table [ ${node.getTplName} ] at time [ $timeid ] fail !!!", e)
          channel.basicPublish(mqConf.getExchange, mqConf.getRoutingKey, false, getMessageProperties(headProperties, 2), "".getBytes)
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
      hiveExecutor.executor(node)
    }
  }


  private def getMessageProperties(messageProperties: Array[String], result: Int) = {
    val tmp = MqHeadParser.getHeadProperties(messageProperties)
    tmp.put(endTime, TimeTransform.getDate(System.currentTimeMillis))
    tmp.put(status, String.valueOf(result))
    new BasicProperties.Builder().headers(tmp).build
  }
}
