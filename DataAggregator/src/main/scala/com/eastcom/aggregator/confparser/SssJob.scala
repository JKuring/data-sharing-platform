package com.eastcom.aggregator.confparser

import com.eastcom.aggregator.exception.SssException

import scala.collection.mutable.Map

/**
  * Created by slp on 2016/2/18.
  */
class SssJob(val initCmdPath: String, val tplPath: String, val sessions: Int, val timeid: String) {

  val nodesMap = Map[String, SssNodeList]()

  def +(node: SssNode): Unit = {
    val n = nodesMap.getOrElse(node.getTable, new SssNodeList)
    nodesMap += (node.getTable -> n.+(node))
  }

  //0 白色，未被访问过的节点标白色
  //-1 灰色，已经被访问过一次的节点标灰色
  //1 黑色，当该节点的所有后代都被访问过标黑色
  def check: this.type = {
    val nodesStatus = Map[String, Int]()
    nodesMap.foreach(x => {
      if (!this.checkNode(x._1, x._2, nodesStatus)) {
        throw new SssException(s"Config ERROR: The dag has at least one cycle!!!")
      }
    })
    this
  }

  private def checkNode(tableName: String, nodes: SssNodeList, nodesStatus: Map[String, Int]): Boolean = {
    val k = nodesStatus.getOrElse(tableName, 0)
    if (k == 0) {
      nodesStatus.put(tableName, -1)
      nodes.nodesList.foreach(node => {
        node.getOriTables.foreach(x => {
          val oriNode = nodesMap.getOrElse(x, null)
          if (oriNode == null) {
            throw new SssException(s"Config ERROR: The table [ ${node.getTable} ]'s original table [ $x ] is not find!!!")
          }
          if (this.checkNode(x, oriNode, nodesStatus)) {
            //添加每个节点的子节点，方便运行流程
            oriNode.addTagTplTable(node.getTplName)
          } else {
            return false
          }
        })
      })
      nodesStatus.put(tableName, 1)
      true
    } else if (k == 1) {
      true
    } else {
      false
    }
  }
}
