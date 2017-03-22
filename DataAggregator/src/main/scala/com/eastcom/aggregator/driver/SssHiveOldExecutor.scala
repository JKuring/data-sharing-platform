package com.eastcom.aggregator.driver


import com.eastcom.aggregator.confparser.SssNode
import com.eastcom.aggregator.context.Context
import com.eastcom.aggregator.exception.SssException
import org.apache.spark.sql.hive.HiveContext

/**
 * Created by slp on 2016/2/17.
 */
class SssHiveOldExecutor(tplPath: String, timeid: String) extends SssExecutor(tplPath, timeid) {
  val sqlContext = Context.getContext(Context.hiveContext).asInstanceOf[HiveContext]

  def executor(node: SssNode) = {
    if (sqlContext == null) {
      throw new SssException("HiveContext is not initialization!!!")
    }

    sqlContext.sql(s"use ${node.getSchema}")
    val result = sqlContext.sql(Context.getSql(tplPath, node.getTplName, stat_month, stat_date, stat_hour, stat_minute, timeid, node))
    if (node.getPartitions != "") {
      result.write.partitionBy(node.getPartitions.split(","): _*).insertInto(s"${node.getTable}")
    } else {
      result.write.insertInto(node.getTable)
    }
  }
}
