package com.eastcom.datacontroller.bean;

import com.eastcom.common.utils.parser.JsonParser;
import org.apache.hadoop.fs.Path;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by linghang.kong on 2017/5/11.
 */
public class HDFSJobsTest {

    public static void main(String[] args) {
        List<String> paths = new ArrayList<String>();
        paths.add(("/123"));
        paths.add(("/456"));
        HDFSJobs hdfsJobs = new HDFSJobs();
        hdfsJobs.setPath_num(2);
        hdfsJobs.setPaths(paths);

        JsonParser.parseJsonToObject(JsonParser.parseObjectToJson(hdfsJobs).getBytes(),HDFSJobs.class);

    }

}