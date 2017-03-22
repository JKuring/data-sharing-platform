package com.eastcom.aggregator.confparser

import scala.collection.mutable.ListBuffer

/**
  * Created by slp on 2016/2/18.
  */
class SssNodeList() {
  val nodesList = ListBuffer[SssNode]()
  val tagTplTables = ListBuffer[String]()
  var execNodes = 0

  def getTagTplTables = tagTplTables

  def addTagTplTable(tagTplTable: String): Unit = {
    tagTplTables += tagTplTable.toLowerCase
  }

  def +(node: SssNode): this.type = {
    nodesList += node
    this
  }

  def finishOne(): Unit = {
    execNodes += 1
  }

  def isFinish(): Boolean = {
    execNodes == nodesList.size
  }
}
