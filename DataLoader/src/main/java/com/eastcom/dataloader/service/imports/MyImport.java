package com.eastcom.dataloader.service.imports;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;

/**
 * Created by linghang.kong on 2017/3/31.
 */
public class MyImport implements Tool {
    @Override
    public int run(String[] strings) throws Exception {
        return 0;
    }

    @Override
    public void setConf(Configuration configuration) {

    }

    @Override
    public Configuration getConf() {
        return null;
    }
}
