package com.eastcom.dataloader.driver


import com.eastcom.dataloader.utils.hdfs.filefilter.NonTmpFileFilter
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.log4j.Logger

/**
 * Created by slp on 2016/4/11.
 */
object FileHelp {

  val logging = Logger.getLogger(getClass)

  def moveFiles(fs: FileSystem, srcPath: String, tagPath: String) = {
    val files = fs.listStatus(new Path(srcPath), new NonTmpFileFilter())
    files.foreach(x => {
      val path = x.getPath
      fs.rename(path, new Path(tagPath, path.getName))
    })
  }

  def moveFiles(fs: FileSystem, tagPath: String, fileStatuses: Array[FileStatus]) = {
    var index = 0
    fileStatuses.foreach(x => {
      val path = x.getPath
      fs.rename(path, new Path(tagPath, path.getName + "-" + index))
      index = index + 1;
    })
  }

  def rmFiles(fs: FileSystem, srcPath: String) = {
    val files = fs.listStatus(new Path(srcPath), new NonTmpFileFilter())
    files.foreach(x => {
      fs.delete(x.getPath, true)
    })
  }

}
