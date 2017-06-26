package com.eastcom.common.utils.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by linghang.kong on 2016/8/25.
 */
public class TimeTransform {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
    private static SimpleDateFormat unsimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");


    public static String getDate(long timestamp) {

        String date = simpleDateFormat.format(new Date(timestamp));

        return date;
    }

    public static long getTimestamp(String date) throws ParseException {

        long timestamp = unsimpleDateFormat.parse(date).getTime();

        return timestamp;
    }

    public static Date getDate(String date) throws ParseException {

        Date d = unsimpleDateFormat.parse(date);

        return d;
    }

}
