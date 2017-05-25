package com.eastcom.common.utils.hdfs.util;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * linghang.kong
 * 获取目录下所有文件的状态
 */
public class FileHelper {

    public static FileStatus[] listStatus(FileSystem fs, Path path, PathFilter filter) {
        return loopDir(fs, path, filter);
    }

    private static FileStatus[] loopDir(FileSystem fs, Path dir, PathFilter filter) {
        List<FileStatus> result = new ArrayList<>();
        try {
            FileStatus[] listStatus = fs.listStatus(dir, filter);
            for (FileStatus status : listStatus) {
                if (status.isDirectory()) {
                    FileStatus[] dir2 = loopDir(fs, status.getPath(), filter);
                    result.addAll(Arrays.asList(dir2));
                } else {
                    result.add(status);
                }
            }
        } catch (Exception e) {
            // 不存在的目录直接返回空数组 ， 不在扔出异常
        }
        return result.toArray(new FileStatus[result.size()]);
    }

}
