package com.eastcom.common.utils.parser;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linghang.kong on 2017/3/24.
 */
public class MqHeadParser {

    public static String[] getHeadArrays(Map<String, Object> headMap){
        List<String> tmp = new ArrayList<>();
        for (String key: headMap.keySet()
                ) {
            tmp.add(key);
            tmp.add((String) headMap.get(key));
        }
        return tmp.toArray(new String[tmp.size()]);
    }

    public static HashMap<String,Object> getHeadProperties(String[] params){
        if (params.length%2 ==0) {
            HashMap<String,Object> head = new HashMap();
            for (int i = 0; i < params.length; i = +2) {
                head.put(params[i], params[i + 1]);
            }
            return head;
        }
        return null;
    }
}
