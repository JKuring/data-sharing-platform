package com.eastcom.common.message;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linghang.kong on 2017/3/22.
 * 为了对应scala中不能spring 注入，所以使用静态类，
 * 同时满足多种mq模板的载入。
 */
public class CommonMeaageProducer {

    public static Map<String,RabbitTemplate> producerCollection = new HashMap<>();

}
