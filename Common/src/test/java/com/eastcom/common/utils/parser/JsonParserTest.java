package com.eastcom.common.utils.parser;


import static org.junit.Assert.*;

/**
 * Created by linghang.kong on 2017/4/19.
 */
public class JsonParserTest {
    public static void main(String[] args) {
        String a = "h";
        System.out.println(JsonParser.parseJsonToObject(a.getBytes(),String.class));
    }
}