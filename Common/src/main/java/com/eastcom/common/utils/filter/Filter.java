package com.eastcom.common.utils.filter;

/**
 * Created by linghang.kong on 2017/4/5.
 */
public interface Filter<E> {
    /**
     * @param content
     * @return
     */
    public boolean doFilter(E content);
}
