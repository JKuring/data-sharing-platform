package com.eastcom.aggregator.confparser

import com.eastcom.aggregator.bean.MQConf

/**
  * Created by linghang.kong on 2017/3/24.
  */
object MqConfParser {

  def parser(userName: String, password: String, host: String, port: String, exchange: String, routingKey: String): MQConf = {
    val mqconf = new MQConf()
    mqconf.setUserName(userName)
    mqconf.setPassword(password)
    mqconf.setHost(host)
    mqconf.setPort(port)
    mqconf.setExchange(exchange)
    mqconf.setRoutingKey(routingKey)
    mqconf
  }
}
