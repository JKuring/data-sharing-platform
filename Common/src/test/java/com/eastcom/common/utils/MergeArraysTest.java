package com.eastcom.common.utils;

import org.junit.Test;

/**
 * Created by linghang.kong on 2017/3/23.
 */
public class MergeArraysTest {
    @Test
    public void merge() throws Exception {

        String[] a = {"1", "2", "3"};
        String[] b = {"4", "5", "6"};

        for (String s : MergeArrays.merge(a, b)
                ) {
            System.out.println(s);
        }


    }

}