package com.eastcom.common.utils.filter;

/**
 * Created by linghang.kong on 2017/4/5.
 */
public class StringFilter implements Filter<String> {


    private String filterType;
    private String parameter;

    public StringFilter(String filterType, String parameter) {
        this.filterType = filterType;
        this.parameter = parameter;
    }

    public boolean doFilter(String content) {

        if (content.length() == 0) {
            return true;
        }
        return false;
    }
}
