package com.eastcom.common.utils.hdfs.util;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * linghang.kong
 * 获取目录下所有文件的状态
 */
public class FileHelper {

    public static FileStatus[] listStatus(FileSystem fs, Path path, PathFilter filter) throws
            IOException {
        return loopDir(fs, path, filter);
    }

    private static FileStatus[] loopDir(FileSystem fs, Path dir, PathFilter filter) throws
            IOException {
        List<FileStatus> result = new ArrayList<>();
        FileStatus[] listStatus = fs.listStatus(dir, filter);
        for (FileStatus status : listStatus) {
            if (status.isDirectory()) {
                FileStatus[] dir2 = loopDir(fs, status.getPath(), filter);
                result.addAll(Arrays.asList(dir2));
            } else {
                result.add(status);
            }
        }
        return result.toArray(new FileStatus[result.size()]);
    }

}
