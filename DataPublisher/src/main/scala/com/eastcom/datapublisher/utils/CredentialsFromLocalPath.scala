package com.eastcom.datapublisher.utils

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.security.token.Token
import org.apache.hadoop.security.{Credentials, UserGroupInformation}
import org.apache.log4j.Logger

/**
  * Created by linghang.kong on 2017/6/14.
  */
class CredentialsFromLocalPath(conf: Configuration, srcPath: String, dstPath: String) {
  private final val logging = Logger.getLogger(getClass)
  val fileSystem = FileSystem.get(conf)
  //    fileSystem.close()
  val localFile = new File(dstPath)
  fileSystem.copyToLocalFile(false, new Path(srcPath), new Path(dstPath))
  private var strToken: String = _

  if (localFile.exists()) {
    logging.info(dstPath + " file exist.")
  }

  strToken = FileUtils.readFileToString(new File(dstPath))

  def getCredential(): Credentials = {
    try {

      val token = new Token()
      token.decodeFromUrlString(strToken)
      logging.info("service: " + token.encodeToUrlString())

      val u = UserGroupInformation.getCurrentUser()
      // 不需要重复添加token，在重编译的HBaseContextCluster中已添加，分开使用是因为RDD中单独需要token。
      //      u.addToken(token)
      u.getCredentials
    } catch {
      case e: Exception => {
        logging.error("Failed to fetch hbase credentials.", e)
        null
      }
    } finally {
      if (new File(dstPath).delete()) {
        logging.info("ucceeded to delete file!")
      } else {
        FileUtils.deleteDirectory(new File(dstPath))
      }
    }
  }

  def getStrToken(): String = {
    strToken
  }
}
