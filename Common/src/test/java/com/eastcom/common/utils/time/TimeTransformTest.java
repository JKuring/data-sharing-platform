package com.eastcom.common.utils.time;

import org.junit.Test;

/**
 * Created by linghang.kong on 2017/3/27.
 */
public class TimeTransformTest {
    @Test
    public void getTimestamp() throws Exception {
        System.out.println(TimeTransform.getDate(TimeTransform.getTimestamp("201703271758")));
    }

}