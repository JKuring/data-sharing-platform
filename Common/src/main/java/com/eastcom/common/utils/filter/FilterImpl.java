package com.eastcom.common.utils.filter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by linghang.kong on 2017/4/5.
 */
public class FilterImpl<T> {

    private String filterType;

    private T filterParameter;

    private Filter filter;

    private String define_filter_class = "";

    public FilterImpl(String filterType, T filterParameter) {
        this.filterType = filterType;
        this.filterParameter = filterParameter;
        if (this.filterParameter instanceof Integer) {
            this.filter = new IntegerFilter(this.filterType, (Integer) this.filterParameter);
        } else {
            // default
            this.filter = new StringFilter(this.filterType, (String) this.filterParameter);
        }
    }


    public boolean filter(String content) {
        return this.filter.doFilter(content);
    }

    public boolean filter(Integer content) {
        return this.filter.doFilter(content);
    }

    public boolean filterInt(String content) {
        return filter(Integer.valueOf(content));
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public T getFilterParameter() {
        return filterParameter;
    }

    public void setFilterParameter(T filterParameter) {
        this.filterParameter = filterParameter;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getDefine_filter_class() {
        return define_filter_class;
    }

    public void setDefine_filter_class(String define_filter_class) {
        this.define_filter_class = define_filter_class;
        if (this.define_filter_class.length() > 0) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                Class clazz = classLoader.loadClass(this.define_filter_class);
                if (clazz != null) {
                    Constructor constructor = clazz.getConstructor(String.class, String.class);
                    this.filter = (Filter) constructor.newInstance(this.filterType, this.filterParameter);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
