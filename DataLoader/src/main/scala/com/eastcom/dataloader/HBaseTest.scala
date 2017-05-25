package com.eastcom.dataloader

import com.cloudera.spark.hbase.HBaseContext
import com.eastcom.dataloader.SlsLauncher.getClass
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.log4j.Logger
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext

/**
  * Created by linghang.kong on 2017/5/24.
  */
object HBaseTest {
  private final val logging = Logger.getLogger(getClass)


  def main(args: Array[String]): Unit = {

    val Array(tableName,hdfsPath,zookeeper_hosts,zookeeper_port) = args
    // 配置spark configuration
    val sparkConf = new SparkConf()


    // 创建SparkContext
    val sc = new SparkContext(sparkConf.setAppName(s"spark load job at time=${System.currentTimeMillis()}"))

    val conf = HBaseConfiguration.create()
//    conf.set("hbase.zookeeper.quorum", zookeeper_hosts)
//    conf.set("hbase.zookeeper.property.clientPort", if (zookeeper_port == "null") "2181" else zookeeper_port)
//
//    //设置查询的表名
//    conf.set(TableInputFormat.INPUT_TABLE, tableName)
    // 创建HiveContext
//    val sqlContext = new HiveContext(sc)
    logging.info("hbase.regionserver.keytab.file: "+conf.get("hbase.regionserver.keytab.file"))
    logging.info("hbase.regionserver.port: "+conf.get("hbase.regionserver.port"))

    val hBaseContext = new HBaseContext(sc,conf)


// source API
//    val usersRDD = sc.newAPIHadoopRDD(conf, classOf[TableInputFormat],
//      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
//      classOf[org.apache.hadoop.hbase.client.Result])
//
////    val count = usersRDD.count()
////    logging.info("Users RDD Count:" + count)
//    usersRDD.cache()
//    logging.info("TestResult: "+usersRDD.collect().mkString)
//
//    //遍历输出
//    usersRDD.foreach { case (_, result) =>
//      val key = Bytes.toInt(result.getRow)
//      val name = Bytes.toString(result.getValue("basic".getBytes, "name".getBytes))
//      val age = Bytes.toInt(result.getValue("basic".getBytes, "age".getBytes))
//      logging.info("Row key:" + key + " Name:" + name + " Age:" + age)
//    }




    val scan = new Scan()
    scan.setBatch(100)
    val scanResult=hBaseContext.hbaseRDD(tableName,scan)

    logging.info("TestResult: "+scanResult.collect().mkString)

//    val result = sqlContext.sql(sql)
//
//    val columns = cf.split(",")
//    val fc = for {
//      i <- 0 until columns.size
//    } yield new Column(columns(i))
//
//    hBaseContext.bulkPut(result.rdd, tableName,
//      (row: Row) => {
//        val put = new Put(row.getString(0).getBytes(Context.character))
//        put.setWriteToWAL(false)
//        for (i <- 0 until (if (fc.size > row.length + 1) row.length + 1 else fc.size)) {
//          val c = fc(i)
//          put.add(c.family, c.qualifier, row.getString(i + 1).getBytes(Context.character))
//        }
//        put
//      },
//      autoFlush = false)

//    Thread.currentThread().wait(1000)
    logging.info("close SparkContext.")
    sc.stop();
  }

}