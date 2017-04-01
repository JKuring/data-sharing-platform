package com.eastcom.dataloader.context

import java.util.regex.Matcher._
import java.util.regex.Pattern
import java.util.regex.Pattern._

import com.eastcom.common.service.HttpRequestUtils
import com.eastcom.dataloader.confparser.SlsNode
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat._

import scala.collection.mutable.Map

/**
  * Created by slp on 2016/2/18.
  */
object Context {
  val hiveContext = "hiveContext"
  val hbaseContext = "hbaseContext"
  val sparkContext = "sparkContext"
  val character = "utf-8"
  val contextMap = Map[String, AnyRef]()

  var timeid = ""

  var finished = false

  val logging = Logger.getLogger(getClass)

  def +(name: String, context: AnyRef): Unit = {
    contextMap += (name -> context)
  }

  def getContext(name: String): AnyRef = {
    contextMap.getOrElse(name, null)
  }

  /**
    * 获取sql模板，然后替换模板中所有时间有关的参数
    *
    * @param tplPath
    * @param tplname
    * @param node
    * @return
    */
  def getSql(tplPath: String, tplname: String, node: SlsNode): String = {
    //    val sc = Context.getContext(Context.sparkContext).asInstanceOf[SparkContext]
    //    var tpl: String = sc.textFile(s"${tplPath}/${tplname}.tpl").collect().mkString(" \n")

    // http
    var tpl: String = HttpRequestUtils.httpGet(tplPath, "".getClass).split("\\n").mkString(" \n")

    val stat_month = if (timeid.length >= 6) timeid.substring(0, 6) else timeid
    val stat_date = if (timeid.length >= 8) timeid.substring(0, 8) else timeid
    val stat_hour = if (timeid.length >= 10) timeid.substring(0, 10) else timeid
    val stat_minute = if (timeid.length >= 12) timeid.substring(0, 12) else timeid

    tpl = tpl.replaceAll("#\\{stat_month\\}", stat_month)
      .replaceAll("#\\{stat_day\\}", stat_date)
      .replaceAll("#\\{stat_hour\\}", stat_hour)
      .replaceAll("#\\{stat_minute\\}", stat_minute)
      .replaceAll("#\\{table_name\\}", node.getTable)
      .replaceAll("#\\{schema_name\\}", node.getSchema)

    tpl = replaceTimePlaceHolder(tpl)
    logging.info(s"[ SQL ] [ $tpl ]")
    tpl
  }

  val hiveType = "hive"
  val hbaseType = "hbase"

  def isFinish() = finished

  def shutdown() = {
    Context.getContext(Context.sparkContext).asInstanceOf[SparkContext].stop()
    finished = true
  }

  /**
    * 支持对模板中时间参数做替换,格式如下:
    *
    * #{time [加减操作(+/-) 数字+时间单位] [时间格式]}
    *
    * 其中[]为可选,时间格式默认为yyyy-MM-dd HH:mm:ss
    *
    * #{time}
    * #{time+15m}
    * #{time-15m}
    * #{time+1d yyyyMMdd}
    */
  def replaceTimePlaceHolder(template: String): String = {
    try {
      var tpl = template

      val timeidPattern = "yyyyMMddHHmmssSSS".substring(0, timeid.length)
      val time = DateTime.parse(timeid, forPattern(timeidPattern))
      val regex = "#\\{time\\s*(([+-])\\s*(\\d+)\\s*(\\S+)){0,1}\\s*(\\s+(.+?)){0,1}\\s*\\}"
      val pattern = Pattern.compile(regex, CASE_INSENSITIVE | MULTILINE)
      var matcher = pattern.matcher(tpl);
      while (matcher.find()) {
        val matched = matcher.group();
        val operate = matcher.group(1);
        var format = matcher.group(6);
        if (format == null) {
          format = "yyyy-MM-dd HH:mm:ss";
        }
        format = format.trim();

        var dateTime = time;
        if (operate != null) {
          val operator = matcher.group(2);
          val mi = matcher.group(3);
          var unit = matcher.group(4);
          val amount = Integer.parseInt(mi);
          if ("+".equals(operator)) {
            unit match {
              case "m" => dateTime = dateTime.plusMinutes(amount)
              case "H" => dateTime = dateTime.plusHours(amount)
              case "d" => dateTime = dateTime.plusDays(amount)
              case "M" => dateTime = dateTime.plusMonths(amount)
              case "y" => dateTime = dateTime.plusYears(amount)
              case _ => dateTime = dateTime.plusMinutes(amount)
            }
          } else if ("-".equals(operator)) {
            unit match {
              case "m" => dateTime = dateTime.minusMinutes(amount)
              case "H" => dateTime = dateTime.minusHours(amount)
              case "d" => dateTime = dateTime.minusDays(amount)
              case "M" => dateTime = dateTime.minusMonths(amount)
              case "y" => dateTime = dateTime.minusYears(amount)
              case _ => dateTime = dateTime.minusMinutes(amount)
            }
          }
        }
        val timeStr = dateTime.toString(format);
        tpl = tpl.replaceAll(quote(matched), quoteReplacement(timeStr));
        matcher = pattern.matcher(tpl);
      }
      tpl
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        template
    }
  }
}
