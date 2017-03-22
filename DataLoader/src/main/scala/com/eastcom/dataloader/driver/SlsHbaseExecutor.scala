package com.eastcom.dataloader.driver

import com.cloudera.spark.hbase.HBaseContext
import com.eastcom.dataloader.confparser.SlsNode
import com.eastcom.dataloader.context.Context
import com.eastcom.dataloader.exception.SlsException
import org.apache.hadoop.hbase.client.Put
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext

/**
 * Created by slp on 2016/2/17.
 */
class SlsHbaseExecutor(tplPath: String) extends SlsExecutor(tplPath) {
  val sqlContext = Context.getContext(Context.hiveContext).asInstanceOf[HiveContext]
  val hbaseContext = Context.getContext(Context.hbaseContext).asInstanceOf[HBaseContext]

  def executor(node: SlsNode) = {
    if (sqlContext == null) {
      throw new SlsException("HiveContext is not initialized!!!")
    }
    if (hbaseContext == null) {
      throw new SlsException("HbaseContext is not initialized!!!")
    }

    val result = sqlContext.sql(Context.getSql(tplPath, node.getTplName, node))
    if (node.getPartitions != "") {
      val columns = node.getPartitions.split(",")
      val fc = for {
        i <- 0 until columns.size
      } yield new Column(columns(i))

      hbaseContext.bulkPut(result.rdd, node.getTable,
        (row: Row) => {
          val put = new Put(row.getString(0).getBytes(Context.character))
          put.setWriteToWAL(false)
          for (i <- 0 until (if (fc.size > row.length + 1) row.length + 1 else fc.size)) {
            val c = fc(i)
            put.add(c.family, c.qualifier, row.getString(i + 1).getBytes(Context.character))
          }
          put
        },
        autoFlush = false)
    }
  }

  class Column(column: String) extends Serializable {
    private val Array(family_, qualifier_) = column.split(":", -1)
    val family = family_.trim.getBytes(Context.character)
    val qualifier = if (!"".equalsIgnoreCase(qualifier_)) {
      qualifier_.trim.getBytes(Context.character)
    } else {
      null
    }
  }

}
