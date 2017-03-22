package com.eastcom.dataloader.utils;


import com.eastcom.dataloader.utils.kerberos.HBaseKerberos;
import com.eastcom.dataloader.utils.time.TimeTransform;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.mapreduce.ImportTsv;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        logger.info("importtsv.columns: {}.", configuration.get("importtsv.columns"));
        logger.info("importtsv.bulk.output: {}.", configuration.get("importtsv.bulk.output"));
        Job job = ImportTsv.createSubmittableJob(configuration, new String[]{tableName, loadingPath});
//        int status = ToolRunner.run(new ImportTsv(), new String[]{tableName, loadingPath});
        return job.waitForCompletion(true);
        // 对输出参数的校验
//        ImportTsv importTsv = new ImportTsv();
//        importTsv.setConf(configuration);
//        String[] otherArgs = (new GenericOptionsParser(importTsv.getConf(), new String[]{tableName, loadingPath})).getRemainingArgs();
//        if(otherArgs.length < 2) {
//            logger.error("Wrong number of arguments: " + otherArgs.length);
//            return result;
//        } else {
//            if (null == importTsv.getConf().get("importtsv.mapper.class")) {
//                String[] timstamp = importTsv.getConf().getStrings("importtsv.columns");
//                if (timstamp == null) {
//                    logger.error("No columns specified. Please specify with -Dimporttsv.columns=...");
//                    return result;
//                }
//
//                int rowkeysFound = 0;
//                String[] job = timstamp;
//                int attrKeysFound = timstamp.length;
//
//                int arr$;
//                for (arr$ = 0; arr$ < attrKeysFound; ++arr$) {
//                    String len$ = job[arr$];
//                    if (len$.equals("HBASE_ROW_KEY")) {
//                        ++rowkeysFound;
//                    }
//                }
//
//                if (rowkeysFound != 1) {
//                    logger.error("Must specify exactly one column as HBASE_ROW_KEY");
//                    return result;
//                }
//
//                int var12 = 0;
//                String[] var14 = timstamp;
//                arr$ = timstamp.length;
//
//                int var16;
//                for (var16 = 0; var16 < arr$; ++var16) {
//                    String i$ = var14[var16];
//                    if (i$.equals("HBASE_TS_KEY")) {
//                        ++var12;
//                    }
//                }
//
//                if (var12 > 1) {
//                    logger.error("Must specify at most one column as HBASE_TS_KEY");
//                    return result;
//                }
//
//                attrKeysFound = 0;
//                String[] var15 = timstamp;
//                var16 = timstamp.length;
//
//                for (int var17 = 0; var17 < var16; ++var17) {
//                    String col = var15[var17];
//                    if (col.equals("HBASE_ATTRIBUTES_KEY")) {
//                        ++attrKeysFound;
//                    }
//                }
//
//                if (attrKeysFound > 1) {
//                    logger.error("Must specify at most one column as HBASE_ATTRIBUTES_KEY");
//                    return result;
//                }
//
//                if (timstamp.length - (rowkeysFound + var12 + attrKeysFound) < 1) {
//                    logger.error("One or more columns in addition to the row key and timestamp(optional) are required");
//                    return result;
//                }
//            }
//
//            long var11 = importTsv.getConf().getLong("importtsv.timestamp", System.currentTimeMillis());
//            importTsv.getConf().setLong("importtsv.timestamp", var11);
//            Job var13 = ImportTsv.createSubmittableJob(importTsv.getConf(), otherArgs);
//            return var13.waitForCompletion(true) ? true : false;
//        }
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
            logger.info("Incremental load complete for table=" + tableName);

            logger.info("Removing output directory {}", dataPath);
            try {
                if (!FileSystem.get(configuration).delete(dataPath, true)) {
                    logger.error("Removing output directory {} failed", dataPath);
                }
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
