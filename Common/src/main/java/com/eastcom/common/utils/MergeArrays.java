package com.eastcom.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linghang.kong on 2017/3/23.
 */
public class MergeArrays {

    public static String[] merge(String[]... arrays ){
        int lengths = 0;
        for (String[] a: arrays
             ) {
            lengths+=a.length;
        }
        List<String> tmp = new ArrayList<>();

        for (String[] a: arrays
             ) {
            for (String s: a
                 ) {
                tmp.add(s);
            }
        }
        return tmp.toArray(new String[lengths]);
    }
}
