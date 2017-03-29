package com.eastcom.dataloader.utils.hdfs.filefilter;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

/**
 * Created by slp on 2016/4/1.
 */
public class NonTmpFileFilter implements PathFilter {

    @Override
    public boolean accept(Path path) {
        return !path.getName().endsWith(".tmp");
    }
}
