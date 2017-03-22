package com.eastcom.datacontroller.utils.time;

import org.junit.Test;

/**
 * Created by linghang.kong on 2017/3/14.
 */
public class TimeTransformTest {
    @Test
    public void getTimestamp() throws Exception {
        System.out.println(TimeTransform.getTimestamp("201703141653"));
        System.out.println(TimeTransform.getDate(1489481580000L));
    }

}