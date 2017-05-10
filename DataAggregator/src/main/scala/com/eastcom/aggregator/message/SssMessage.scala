package com.eastcom.aggregator.message

import com.eastcom.aggregator.confparser.SssNode


/**
  * Created by slp on 2016/2/18.
  */
sealed trait SssMessage

case class SssJobMessage(node: SssNode) extends SssMessage

case class SssStartMessage() extends SssMessage

case class SssResultMessage(node: SssNode) extends SssMessage

