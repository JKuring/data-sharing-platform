package com.eastcom.dataloader.message


import com.eastcom.dataloader.confparser.SlsNode
import org.apache.hadoop.fs.FileStatus

/**
  * Created by slp on 2016/2/18.
  */
sealed trait SlsMessage

case class SlsJobMessage(node: SlsNode, fileStatus: Array[FileStatus]) extends SlsMessage

case class SlsStartMessage() extends SlsMessage

case class SlsResultMessage(node: SlsNode) extends SlsMessage

