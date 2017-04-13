package com.eastcom.common.service;

import org.junit.Test;

/**
 * Created by linghang.kong on 2017/3/29.
 */
public class HttpRequestUtilsTest {
    @Test
    public void httpPost() throws Exception {

//        Map<String,String> map = HttpRequestUtils.httpPost("http://10.221.247.7:8080/stream/tool/redis-memory",HashMap.class);
//        String map = HttpRequestUtils.httpPost("http://10.221.247.7:8080/stream/tool/redis-memory",String.class);

        String map = HttpRequestUtils.httpPost("work/conf/load_control_hour.conf", String.class);


//        for (String key: map.split("\\n")
//             ) {
//            System.out.println("=====");
//            System.out.println(key);
////            System.out.println(map.get(key));
//        }
        System.out.println(map);

    }

}