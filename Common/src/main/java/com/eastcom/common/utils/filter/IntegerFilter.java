package com.eastcom.common.utils.filter;

/**
 * Created by linghang.kong on 2017/4/5.
 */
public class IntegerFilter implements Filter<Integer> {

    private String filterType;
    private Integer parameter;

    public IntegerFilter(String filterType, Integer parameter) {
        this.filterType = filterType;
        this.parameter = parameter;
    }

    public IntegerFilter(String filterType, String parameter) {
        this.filterType = filterType;
        this.parameter = Integer.valueOf(parameter);
    }

    public boolean doFilter(String content) {
        return doFilter(Integer.valueOf(content));
    }

    @Override
    public boolean doFilter(Integer content) {
        switch (filterType) {
            case ">":
                return gt(content);
            case "<":
                return lt(content);
            case "=":
                return eq(content);
            default:
                return false;
        }
    }

    private boolean gt(int content) {
        return content > parameter;
    }

    private boolean lt(int content) {
        return content < parameter;
    }

    private boolean eq(int content) {
        return content == parameter;
    }

}
