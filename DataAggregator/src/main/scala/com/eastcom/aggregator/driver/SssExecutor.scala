package com.eastcom.aggregator.driver

/**
  * Created by slp on 2016/2/23.
  */
class SssExecutor(tplPath: String, timeid: String) extends Serializable {
  val stat_month = if (timeid.length >= 6) timeid.substring(0, 6) else timeid
  val stat_date = if (timeid.length >= 8) timeid.substring(0, 8) else timeid
  val stat_hour = if (timeid.length >= 10) timeid.substring(0, 10) else timeid

  //201610201100
  val stat_minute = if (timeid.length >= 12) timeid.substring(0, 12) else timeid
}
