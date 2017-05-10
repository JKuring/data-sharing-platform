package com.eastcom.aggregator.context

import java.util.regex.Matcher._
import java.util.regex.Pattern
import java.util.regex.Pattern._

import com.eastcom.aggregator.confparser.SssNode
import com.eastcom.common.service.HttpRequestUtils
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat._

import scala.collection.mutable.Map

/**
  * Created by slp on 2016/2/18.
  */
object Context {
  var flag = false
  val hiveContext = "hiveContext"
  val hbaseContext = "hbaseContext"
  val sparkContext = "sparkContext"
  val character = "utf-8"
  val contextMap = Map[String, AnyRef]()

  val logging = Logger.getLogger(getClass)

  def +(name: String, context: AnyRef): Unit = {
    contextMap += (name -> context)
  }

  def getContext(name: String): AnyRef = {
    contextMap.getOrElse(name, null)
  }

  def getSql(configServiceUrl: String, tplname: String, stat_month: String, stat_date: String, stat_hour: String, stat_minute: String, timeid: String, node: SssNode): String = {
//    val sc = Context.getContext(Context.sparkContext).asInstanceOf[SparkContext]
//    var tpl: String = sc.textFile(s"${tplPath}/${tplname}.tpl").collect().mkString(" \n")

    var tpl: String = HttpRequestUtils.httpGet(configServiceUrl + tplname, "".getClass).split("\\n").mkString(" \n")

    tpl = tpl.replaceAll("#\\{stat_month\\}", stat_month)
      .replaceAll("#\\{stat_day\\}", stat_date)
      .replaceAll("#\\{stat_hour\\}", stat_hour)
      .replaceAll("#\\{stat_minute\\}", stat_minute)
      .replaceAll("#\\{table_name\\}", node.getTable)
      .replaceAll("#\\{schema_name\\}", node.getSchema)

    tpl = replaceTimePlaceHolder(timeid, tpl)
    logging.info(s"[ SQL ] [ $tpl ]")
    tpl
    //    Source.fromFile(s"${tplPath}/${tplname}.tpl").getLines().mkString(" \n")
    //      .replaceAll("#\\{stat_month\\}", stat_month)
    //      .replaceAll("#\\{stat_date\\}", stat_date)
    //      .replaceAll("#\\{stat_hour\\}", stat_hour)
  }

  val hiveType = "hive"
  val hiveTypeOld = "hive_old"
  val hbaseType = "hbase"

  def finish() = {
    logging.info("the current task is finish!")
    flag = true
  }

  def isFinish = flag

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
  def replaceTimePlaceHolder(timeid: String, template: String): String = {
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
