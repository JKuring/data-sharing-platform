package com.eastcom.aggregator.fileparser


import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ListBuffer

object SqlFileParser {

  def parse(sqlText: String): Seq[String] = {
    var lines: Array[String] = sqlText.split("\n")
    val qsb = new StringBuilder()
    for (line: String <- lines) {
      if (!line.startsWith("--")) {
        qsb.append(line + "\n");
      }
    }
    val result: ListBuffer[String] = new ListBuffer
    var command: String = "";
    for (oneCmd: String <- qsb.toString.split(";")) {
      if (StringUtils.endsWith(oneCmd, "\\")) {
        command += StringUtils.chop(oneCmd) + ";"
      } else {
        command += oneCmd;
        if (StringUtils.isNotBlank(command)) {
          result.append(command)
          command = ""
        }
      }
    }
    result
  }

}