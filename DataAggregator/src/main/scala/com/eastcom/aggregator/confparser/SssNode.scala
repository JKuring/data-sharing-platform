package com.eastcom.aggregator.confparser

import scala.collection.mutable.ArrayBuffer

/**
  * Created by slp on 2016/2/18.
  */
class SssNode(config: String) {
  private val Array(type_, schema, table, oritables, partitions, tplname_) = config.replaceAll("\\s+", "").split("\\|", -1)
  private val oriTables = if (oritables != "") {
    val oris = ArrayBuffer[String]()
    oritables.split(",").foreach(x => {
      oris += x.toLowerCase
    })
    oris.toArray
  } else {
    Array[String]()
  }

  private val tplname = (if (tplname_ != "") tplname_ else table).replaceAll(":", "_")

  def getType = type_.toLowerCase

  def getTable = table.toLowerCase

  def getOriTables = oriTables

  def getTplName = tplname.toLowerCase

  def getPartitions = partitions.toLowerCase

  def getSchema = schema
}
