package com.eastcom.common.utils;


import com.eastcom.common.utils.kerberos.HBaseKerberos;
import com.eastcom.common.utils.time.TimeTransform;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.mapreduce.ToolRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linghang.kong on 2016/12/26.
 */
public class HBaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(HBaseUtils.class);

    public static boolean createHFile(Configuration configuration, String tableName, String loadingPath) throws Exception {
        logger.info("Upload data to {}, the load path is {}.", tableName, loadingPath);
        logger.info("class.name: {}.", configuration.get("importtsv.class.name"));
        logger.info("columns: {}.", configuration.get("importtsv.columns"));
        logger.info("bulk.output: {}.", configuration.get("importtsv.bulk.output"));
        // original class in the hbase server package
//        Job job = ImportTsv.createSubmittableJob(configuration, new String[]{tableName, loadingPath});
//        return job.waitForCompletion(true);

        // spring tool runner
        ToolRunner toolRunner = new ToolRunner();
        toolRunner.setConfiguration(configuration);
        toolRunner.setToolClass(configuration.get("importtsv.class.name"));
        toolRunner.setArguments(new String[]{tableName, loadingPath});
        toolRunner.setCloseFs(true);
        return toolRunner.call() <= 0;
    }

    public static boolean upLoadHFile(Configuration configuration, HTable table, Path dataPath) {
        boolean result = false;
        String tableName = Bytes.toString(table.getName().getName());
        try {
            LoadIncrementalHFiles loadIncrementalHFiles = new LoadIncrementalHFiles(configuration);
            loadIncrementalHFiles.doBulkLoad(dataPath, table);
            result = true;
        } catch (Exception e) {
            logger.error("Can't upload the data of {} minutes, exception: {}.", dataPath.getName(), e.getMessage());
        } finally {
            if (table != null) {
                try {
                    table.close();
                } catch (IOException e) {
                    logger.error("Can't close the table {}, exception: {}.", table.getName(), e.getMessage());
                }
            }
            logger.info("Incremental load complete for table {}.", tableName);

            try {
                if (!FileSystem.get(configuration).delete(dataPath, true)) {
                    logger.error("Removing output directory {} failed", dataPath);
                }
                logger.info("Removing output directory {}", dataPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static byte[][] getSplitKeys(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String tmp = null;
        List<byte[]> buffer = new ArrayList<>();
        while ((tmp = reader.readLine()) != null) {
            buffer.add(Bytes.toBytes(tmp));
        }
        return buffer.toArray(new byte[buffer.size()][]);
    }

    public static String getCurrentTimeTableName(String tableName, long currentTime, long delay, String granularity) {
        String[] date = String.valueOf(TimeTransform.getDate(currentTime - delay)).split("\\-");
        String year = date[0];
        String mouth = date[1];
        String day = date[2];
        String[] time = date[3].split(":");
        String minute;
        int mm = Integer.parseInt(time[1]);
        mm = mm - mm % 5;
        if (mm < 10) {
            minute = "0" + mm;
        } else
            minute = String.valueOf(mm);

        if (granularity.equals("D")) {
            tableName = tableName + "_D_" + year + mouth + day + "00";
        }
        return tableName;
    }

    public static String getCurrentTimePath(long currentTime, int delay) {
        String[] date = String.valueOf(TimeTransform.getDate(currentTime - delay * 1000L)).split("\\-");
        String year = date[0];
        String mouth = date[1];
        String day = date[2];
        String[] time = date[3].split(":");
        String minute;
        int mm = Integer.parseInt(time[1]);
        mm = mm - mm % 5;
        if (mm < 10) {
            minute = "0" + mm;
        } else
            minute = String.valueOf(mm);
        return "/" + year + mouth + day + "/" + time[0] + "/" + minute;
    }

    public static HConnection getConnection(Configuration configuration) {
//        Configuration configuration = HBaseConfiguration.create();
        HConnection connection = null;
        if (configuration == null) {
            logger.error("configuration is null!");
            System.exit(1);
        } else {
            logger.info("successful Fetch configuration!");
            // add kerberos
            configuration = HBaseKerberos.getConfiguration(configuration);
        }
        try {
            connection = HConnectionManager.createConnection(configuration);
//            importTsv.connection = ConnectionFactory.createConnection(importTsv.configuration, Executors.newFixedThreadPool(10));
        } catch (IOException e) {
            logger.debug("hbase.client.connection.impl={}", configuration.get("hbase.client.connection.impl"));
            logger.error("get connection false! Exception: {}.", e.getMessage());
            StringBuffer out = new StringBuffer();
            StackTraceElement[] trace = e.getStackTrace();
            out.append(" processResult: " + e.toString());
            for (StackTraceElement s : trace) {
                out.append("\tat " + s.toString() + "\r\n");
            }
            logger.debug(out.toString());
            System.exit(1);
        }
        return connection;
    }
}
