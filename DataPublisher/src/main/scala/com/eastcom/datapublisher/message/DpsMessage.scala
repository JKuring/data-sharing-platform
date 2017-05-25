package com.eastcom.datapublisher.message

import com.eastcom.datapublisher.driver.{DpsFtpNode, DpsHbaseNode, DpsNode}
import org.apache.hadoop.fs.FileStatus


sealed trait DpsMessage

case class DpsHBaseJobMessage(node: DpsHbaseNode) extends DpsMessage

case class DpsFtpJobMessage(node: DpsFtpNode) extends DpsMessage

case class DpsStartMessage() extends DpsMessage

case class DpsResultMessage(node: DpsNode) extends DpsMessage

