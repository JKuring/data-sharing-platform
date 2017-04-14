package com.eastcom.common.utils;

import org.junit.Test;

import java.util.Arrays;

/**
 * Created by linghang.kong on 2017/3/23.
 */
public class MergeArraysTest {
    @Test
    public void merge() throws Exception {

        String[] a = {"1", "2", "3"};
        String[] b = {"4", "5", "6"};
        String[] c = {"7", "8", "9"};


//        for (String s : MergeArrays.merge(a, b, c)
//                ) {
//            System.out.println(s);
//        }

        System.out.println(Arrays.toString(MergeArrays.merge(a, b, c)));

    }

}