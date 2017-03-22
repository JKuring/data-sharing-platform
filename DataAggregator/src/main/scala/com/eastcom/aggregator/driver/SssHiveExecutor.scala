package com.eastcom.aggregator.driver

import com.eastcom.aggregator.confparser.SssNode
import com.eastcom.aggregator.context.Context
import com.eastcom.aggregator.exception.SssException
import com.eastcom.aggregator.fileparser.SqlFileParser
import org.apache.log4j.Logger
import org.apache.spark.sql.hive.HiveContext

/**
 * Created by slp on 2016/2/17.
 */
class SssHiveExecutor(tplPath: String, timeid: String) extends SssExecutor(tplPath, timeid) {

  val logging = Logger.getLogger(getClass)
  val sqlContext = Context.getContext(Context.hiveContext).asInstanceOf[HiveContext]

  def executor(node: SssNode) = {
    if (sqlContext == null) {
      throw new SssException("HiveContext is not initialization!!!")
    }

    val sqlText = Context.getSql(tplPath, node.getTplName, stat_month, stat_date, stat_hour, stat_minute, timeid, node);
    val sqls = SqlFileParser.parse(sqlText)
    for (sql: String <- sqls) {
      sqlContext.sql(sql)
      logging.info(s"Execute Sql: $sql")
    }
  }
}
