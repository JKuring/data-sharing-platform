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
        switch (filterType) {
            case "gt":
                return gt(content);
            case "lt":
                return lt(content);
            case "eq":
                return eq(content);
            case "rg":
                return rg(content);
            default:
                return false;
        }
    }

    private boolean gt(String content) {
        if (content.length() > Integer.valueOf(parameter)) {
            return true;
        }
        return false;
    }

    private boolean lt(String content) {
        if (content.length() < Integer.valueOf(parameter)) {
            return true;
        }
        return false;
    }

    private boolean eq(String content) {
        if (content.length() == Integer.valueOf(parameter)) {
            return true;
        }
        return false;
    }

    private boolean rg(String content) {

        return true;
    }
}
