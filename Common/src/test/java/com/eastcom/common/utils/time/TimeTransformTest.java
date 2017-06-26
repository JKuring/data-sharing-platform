package com.eastcom.common.utils.time;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Created by linghang.kong on 2017/3/27.
 */
public class TimeTransformTest {
    @Test
    public void getTimestamp() throws Exception {
        DateTime time = new DateTime(TimeTransform.getDate("201706260305"));
        System.out.println(time.getYear());
        System.out.println(time.getMonthOfYear());
        System.out.println(time.getDayOfMonth());
        System.out.println(time.getHourOfDay());
        System.out.println(time.getMinuteOfHour());
    }

}