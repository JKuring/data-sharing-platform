package com.eastcom.dataloader.driver


import com.eastcom.dataloader.confparser.SlsNode
import com.eastcom.dataloader.context.{Context, SqlFileParser}
import com.eastcom.dataloader.exception.SlsException
import org.apache.log4j.Logger
import org.apache.spark.sql.hive.HiveContext

/**
  * Created by slp on 2016/2/17.
  */
class SlsHiveExecutor(configServiceUrl: String) extends SlsExecutor(configServiceUrl) {

  val logging = Logger.getLogger(getClass)

  val sqlContext = Context.getContext(Context.hiveContext).asInstanceOf[HiveContext]

  def executor(node: SlsNode) = {
    if (sqlContext == null) {
      throw new SlsException("HiveContext is not initialization!!!")
    }

    // 切换DB
    sqlContext.sql(s"use ${node.getSchema}")


    // 解析sql语句，替换时间参数
    val sqlText = Context.getSql(configServiceUrl, node.getTplName, node)
    // 对sql语句进行格式化，分行，添加“;”
    val sqls = SqlFileParser.parse(sqlText)
    for (sql: String <- sqls) {
      logging.info(s"Execute Sql: $sql")
      sqlContext.sql(sql)
    }

  }
}
