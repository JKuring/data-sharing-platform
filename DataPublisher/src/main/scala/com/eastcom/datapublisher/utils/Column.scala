package com.eastcom.datapublisher.utils

import com.eastcom.datapublisher.context.AppContext

/**
  * Created by linghang.kong on 2017/6/16.
  */
class Column(column: String) extends Serializable {
  private val Array(family_, qualifier_) = column.split(":", -1)
  val family = family_.trim.getBytes(AppContext.character)
  val qualifier = if (!"".equalsIgnoreCase(qualifier_)) {
    qualifier_.trim.getBytes(AppContext.character)
  } else {
    null
  }
}
