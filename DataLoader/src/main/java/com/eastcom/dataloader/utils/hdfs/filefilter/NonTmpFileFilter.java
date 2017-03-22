package com.eastcom.dataloader.utils.hdfs.filefilter;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

/**
 * Created by slp on 2016/4/1.
 */
public class NonTmpFileFilter implements PathFilter {

	@Override
	public boolean accept(Path path) {
		if (path.getName().endsWith(".tmp")) {
			return false;
		}
		return true;
	}
}
