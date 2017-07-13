package com.eastcom.aggregator.driver

import java.util.Date

import akka.actor.Actor
import com.eastcom.aggregator.bean.MQConf
import com.eastcom.aggregator.confparser.SssNode
import com.eastcom.aggregator.context.Context
import com.eastcom.aggregator.message.{SssJobMessage, SssResultMessage}
import com.eastcom.common.interfaces.service.Executor
import com.eastcom.common.message.RabbitMQConnection
import com.eastcom.common.utils.parser.MqHeadParser
import com.eastcom.common.utils.time.TimeTransform
import com.rabbitmq.client.AMQP.BasicProperties
import org.apache.log4j.Logger

import scala.collection.mutable.ArrayBuffer

/**
  * Created by slp on 2016/2/18.
  */
class SssManager(configServiceUrl: String, timeid: String, val mqConf: MQConf, val headProperties: Array[String]) extends Actor {
  private val logging = Logger.getLogger(getClass)
  // 同批任务 时间和配置路径相同，但tpl与业务有关，执行时为同批次任务，所以conf只能对一批同一类型的任务，即不能既加载hive又加载hbase
  private val hiveExecutor = new SssHiveExecutor(configServiceUrl, timeid)
  private val hbaseExecutor = new SssHbaseExecutor(configServiceUrl, timeid)
  private val hiveOldExecutor = new SssHiveOldExecutor(configServiceUrl, timeid)

  logging.debug(s"MQ info: ${mqConf.getUserName}, ${mqConf.getHost}, ${mqConf.getExchange}, ${mqConf.getRoutingKey}")
  logging.debug(s"${mqConf.getPassword}, ${mqConf.getPort}, parameters length: ${mqConf.getParameters.length}")
  private val mqConnection = new RabbitMQConnection(mqConf.getUserName, mqConf.getPassword, mqConf.getHost, Integer.parseInt(mqConf.getPort))

  private val taskId = "taskId"

  private val startTime = "startTime"
  private val endTime = "endTime"
  private val status = "status"
  private val jobType = "jobType"

  private val tableName = "tableName"
  private val schema = "schema"
  private val partition = "partition"
  private val timeId = "timeId"


  override def receive: Receive = {
    case SssJobMessage(node: SssNode) => {
      logging.info(s"start to connect MQ!")
      var result = Executor.FAILED
      var message = "ok"
      try {
        val connection = mqConnection.createConnection()
        val channel = connection.createChannel()
        logging.info("inish connecting MQ!")
        try {
          val startTime = new Date().getTime
          logging.info(s" [ SSS_JOB ] [ ${node.getType} ] Start exec job with table [ ${node.getTplName} ] at time [ $timeid ] ...")
//          try {
            exec(node)
            result = Executor.SUCESSED
//          } catch {
//            case e: Exception => {
//              Thread.sleep(30000l)
//              exec(node)
//              result = Executor.SUCESSED
//            }
//          }
          val eastime = (new Date().getTime - startTime) / 1000
          logging.info(s" [ SSS_JOB ] [ ${node.getType} ] Finish exec job with table [ ${node.getTplName} ] at time [ $timeid ] eastime [ $eastime ] s !")
        } catch {
          case e: Exception => logging.error(s" [ SSS_JOB ] [ ${node.getType} ] Exec job with table [ ${node.getTplName} ] at time [ $timeid ] fail !!!", e)
            result = Executor.FAILED
            message = e.getMessage
        } finally {
          try {
            logging.info("Sending publish message.")
            val tmp = ArrayBuffer[String]()
            tmp ++= headProperties
            tmp += this.tableName += node.getTable
            tmp += this.schema += node.getSchema
            tmp += this.partition += node.getPartitions
            tmp += this.timeId += timeid
            channel.basicPublish(mqConf.getExchange, mqConf.getRoutingKey, getMessageProperties(tmp.toArray,node.getTplName, result), s"Finish aggregating task: ${node.getType}, jobs parameter: ${node.getTplName}, message: $message".getBytes)
            connection.close()
            logging.info("Finish sending.")
          }
          catch {
            case e: Exception => logging.error("Failed to return MQ message.")
          }
        }
      } catch {
        case e: Exception => logging.error(s"Failed to execute jos, tpl: ${node.getTplName}.", e)
      }
      logging.info("Finish SssManager.")
      context.sender() ! SssResultMessage(node)
    }
  }

  private def exec(node: SssNode): Unit = {
    logging.info(s"Execute the job cicode: ${node.getTplCiCode}.");
    if (Context.hiveTypeOld == node.getType) {
      hiveOldExecutor.executor(node)
    } else if (Context.hbaseType == node.getType) {
      hbaseExecutor.executor(node)
    } else if (Context.hiveType == node.getType) {
      hiveExecutor.executor(node)
    }
    logging.info(s"Finish executing the job cicode: ${node.getTplCiCode}.");
  }


  private def getMessageProperties(messageProperties: Array[String],tplName:String, result: Int) = {
    val tmp = MqHeadParser.getHeadProperties(messageProperties)
    tmp.put(taskId,tmp.get(taskId)+"_"+tplName)
    tmp.put(endTime, Long.box(System.currentTimeMillis))
    tmp.put(status, String.valueOf(result))
    new BasicProperties.Builder().headers(tmp).build
  }
}
