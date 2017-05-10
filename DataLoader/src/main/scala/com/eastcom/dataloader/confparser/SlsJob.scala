package com.eastcom.dataloader.confparser

import scala.collection.mutable.Map

/**
  * Created by slp on 2016/2/18.
  */
class SlsJob(val initCmdPath: String, val configServiceUrl: String) {
  // 存储 job 的 map
  val nodesMap = Map[String, SlsNode]()

  def getSessions = nodesMap.size

  def +(node: SlsNode): Unit = {
    // 已模板名称作为job 的name
    nodesMap += (node.getTplName -> node)
  }
}
