package com.eastcom.common.utils.hdfs.filefilter;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import java.io.IOException;

public class NonTmpOldFileFilter implements PathFilter {
    private int count = 0;
    private int num = 0;
    private long time = 0L;
    private FileSystem fs;

    public NonTmpOldFileFilter(int count) {
        this.count = count;
    }

    public NonTmpOldFileFilter(int count, long time, FileSystem fs) {
        this.count = count;
        this.time = time;
        this.fs = fs;
    }

    public boolean accept(Path path) {
        try {
            if (path.getName().endsWith(".tmp")
                    || ((count > 0) && (num >= count))
                    || fs.getFileStatus(path).getModificationTime() >= this.time)
                return false;
        } catch (IOException e) {
            return false;
        }
        num++;
        return true;
    }
}