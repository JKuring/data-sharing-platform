package com.eastcom.common.utils.filter;

import org.junit.Test;

/**
 * Created by linghang.kong on 2017/4/5.
 */
public class FilterImplTest {
    @Test
    public void filter() throws Exception {
        long starTime = System.currentTimeMillis();
        Integer initParam = 11;
        FilterImpl filterImpl = new FilterImpl<>(">", "12");
        filterImpl.setDefine_filter_class("com.eastcom.common.utils.filter.IntegerFilter");
//        filterImpl.setFilterParameter(10);
//        filterImpl.setFilterType(">");
        // long 的性能要比 int的性能差100倍以上
        int j = 0;

        for (int i = 0; i < 1000000; i++) {
            if (filterImpl.filter(initParam)) {
                j++;
            }
        }
        System.out.println(System.currentTimeMillis() - starTime);
        System.out.println(j);
    }

}