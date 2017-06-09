package com.eastcom.dataloader

import java.io.File

import com.eastcom.dataloader.context.SqlFileParser
import com.eastcom.dataloader.utils.HBaseContextCluster
import org.apache.commons.io.FileUtils
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Put, Scan}
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.security.token.Token
import org.apache.log4j.Logger
import org.apache.spark.deploy.SparkHadoopUtil
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by linghang.kong on 2017/5/24.
  */
object HBaseTest {
  private final val logging = Logger.getLogger(getClass)


  def main(args: Array[String]): Unit = {

    val Array(user, tableName, hdfsPath, localPath, sql) = args

    try {
      // 配置spark configuration
      val sparkConf = new SparkConf()


      // 创建SparkContext
      val sc = new SparkContext(sparkConf.setAppName(s"spark load job at time=${System.currentTimeMillis()}"))

      // yarn token
      var cred = SparkHadoopUtil.get.getCurrentUserCredentials()
      val tmp = cred.getAllTokens.iterator()
      while (tmp.hasNext){
        logging.info("yarn token:"+tmp.next().encodeToUrlString())
      }


      // 创建HiveContext
      val sqlContext = new HiveContext(sc)

      var result:DataFrame = null
      val sqls = SqlFileParser.parse(sql)
      for (sql: String <- sqls) {
        logging.info(s"Execute Sql: $sql")
        result = sqlContext.sql(sql)
      }

      val conf = HBaseConfiguration.create()
      conf.set("username.client.keytab.file", localPath)
      // set "hbaseuser1" as the new create user name
      conf.set("username.client.kerberos.principal", user)

      //    conf.set("hbase.zookeeper.quorum", zookeeper_hosts)
      //    conf.set("hbase.zookeeper.property.clientPort", if (zookeeper_port == "null") "2181" else zookeeper_port)
      //
      //    //设置查询的表名
      //    conf.set(TableInputFormat.INPUT_TABLE, tableName)
      // 创建HiveContext
      //    val sqlContext = new HiveContext(sc)
      logging.info("username.client.keytab.file: " + conf.get("username.client.keytab.file"))
      logging.info("hbase.regionserver.keytab.file: " + conf.get("hbase.regionserver.keytab.file"))
      logging.info("hbase.regionserver.port: " + conf.get("hbase.regionserver.port"))
      logging.info("username.client.keytab.file: " + conf.get("username.client.keytab.file"))
      logging.info("HADOOP_TOKEN_FILE_LOCATION: " + System.getenv("HADOOP_TOKEN_FILE_LOCATION"))
      //    conf.set("hbase.mapred.output.quorum",zookeeper_hosts)

      //      val creds = SparkHadoopUtil.get.getCurrentUserCredentials()
      //
      //      logging.info("creds: " + creds.getAllTokens.toArray.mkString)
      //
      //      val ugi = UserGroupInformation.getCurrentUser
      //      ugi.addCredentials(creds)
      //      // specify that this is a proxy user
      //      ugi.setAuthenticationMethod(AuthenticationMethod.PROXY)
      //      UserGroupInformation.setLoginUser(ugi)
      //
      //      val u = UserGroupInformation.getLoginUser

      class Column(column: String) extends java.io.Serializable {
        private val Array(family_, qualifier_) = column.split(":", -1)
        val family = family_.trim.getBytes("utf-8")
        val qualifier = if (!"".equalsIgnoreCase(qualifier_)) {
          qualifier_.trim.getBytes("utf-8")
        } else {
          null
        }
      }

      val fileSystem = FileSystem.get(conf)
      fileSystem.copyToLocalFile(false, new Path(hdfsPath), new Path(localPath))

      val localFile = new File(localPath)

      if (localFile.exists()) {
        logging.info(localPath + " file exist.")
      }


      //      UserGroupInformation.setConfiguration(conf)
      //      UserGroupInformation.loginUserFromKeytab(user, localPath)

      val strToken = FileUtils.readFileToString(new File(localPath))
      val token = new Token()
      token.decodeFromUrlString(strToken)
      logging.info("service: " + token.encodeToUrlString())

      val u = UserGroupInformation.getCurrentUser()

      u.addToken(token)

      val credentials = u.getCredentials


      val jobConf = new JobConf(conf)
      jobConf.setCredentials(credentials)
      if (credentials.numberOfTokens() > 0) {
        // 添加token
        //    val strToken = FileUtils.readFileToString(new File(localPath))
        //    val token = new Token()
        //    token.decodeFromUrlString(strToken)
        //    logging.info("service: "+token.getService)
        //
        //    val u =UserGroupInformation.getCurrentUser()
        //
        //    u.addToken(token)
        //
        //    logging.info("getUserName: "+u.getUserName)
        //    logging.info("getProxyUser: "+u.getProxyUser)
        //    logging.info("getAccessId: "+u.getAccessId)
        //    logging.info("getAccessKey: "+u.getAccessKey)
        //    if (u.hasKerberosCredentials) {
        //      logging.info("UGI OK!")
        //    }

        //    u.doAs(new PrivilegedExceptionAction[Void]{
        //      override def run(): Void = {
        //        val connection = ConnectionFactory.createConnection(conf)
        //        val admin = connection.getAdmin
        //        if(admin.tableExists(TableName.valueOf(tableName))){
        //          logging.info("table exist.")
        //        }
        //        connection.close()
        //        null
        //      }
        //    })


        //    u.doAs(new PrivilegedExceptionAction[Void] {
        //      override def run(): Void = {
        //        val hBaseContext = new HBaseContext(sc,conf,strToken)
        //        val scan = new Scan()
        //        scan.setBatch(100)
        //        val scanResult=hBaseContext.hbaseScanRDD(tableName,scan)
        //
        //        logging.info("TestResult: "+scanResult.collect().mkString)
        //        null
        //      }
        //    })

        val hBaseContext = new HBaseContextCluster(sc, jobConf, strToken)

//        val column = new Column("cf:")
//        logging.info("sql: "+sql)
//        val scanResult = hBaseContext.bulkPut(result.rdd, tableName,
//          (row: Row) => {
//            logging.info("row 0:"+row.getString(0))
//            val put = new Put(row.getString(0).getBytes("utf-8"))
//            put.setWriteToWAL(false)
//            logging.info("row 1:"+row.getString(1))
//            put.add(column.family, column.qualifier, row.getString(1).getBytes("utf-8"))
//            put
//          },
//          autoFlush = false)

        val scan = new Scan()
        scan.setBatch(100)
        val  scanResult = hBaseContext.hbaseScanRDD(tableName,scan)
        scanResult.saveAsTextFile("/user/east_wys/hbaseScanTest")
        logging.info("TestResult: " +scanResult.count() )
      }


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
    } catch {
      case e: Exception => {
        logging.error(e.getMessage)
        logging.error(e.getCause)
        logging.error(e.fillInStackTrace())
      }
    } finally {
      if (new File(localPath).delete()) {
        logging.info("succeeded to delete file!")
      } else {
        FileUtils.deleteDirectory(new File(localPath))
      }
    }
  }

}
