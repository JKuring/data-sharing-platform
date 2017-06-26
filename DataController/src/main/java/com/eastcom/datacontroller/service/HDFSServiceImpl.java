package com.eastcom.datacontroller.service;

import com.eastcom.datacontroller.interfaces.dao.HDFSDao;
import com.eastcom.datacontroller.interfaces.service.HDFSService;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

/**
 * Created by linghang.kong on 2017/5/9.
 */
@Service
@Lazy
public class HDFSServiceImpl implements HDFSService {

    private static final Logger logger = LoggerFactory.getLogger(HDFSServiceImpl.class);

    @Autowired
    private HDFSDao<Path> hdfsDao;


    public HDFSServiceImpl() {
    }


    @Override
    public void createDir(Path path) {
        try {
            hdfsDao.create(path);
        } catch (IOException e) {
            logger.error("Failed to create dir {}.", path.getName(), e.fillInStackTrace());
        }
    }

    @Override
    public void deleteDir(Path path) {
        try {
            hdfsDao.delete(path);
        } catch (IOException e) {
            logger.error("Failed to delete dir {}.", path.getName(), e.fillInStackTrace());
        }

    }

    public void deleteByTime(Path rootPath, Date date, boolean DF, String timeFormat) {
        DateTime formatDate = new DateTime(date);
        if (DF) {
            try {
                String year = String.valueOf(formatDate.getYear());
                String month = getWideTimeString(formatDate.getMonthOfYear());
                String day = getWideTimeString(formatDate.getDayOfMonth());

                logger.info("Start to delete the files that out of {} time from {}.", date.toString(), rootPath.getName());
                FileSystem fileSystem = hdfsDao.getFileSystem();
                FileStatus[] rootFileStatus = fileSystem.listStatus(rootPath);
                logger.info("Found the {} dirs form {}.", rootFileStatus, rootPath.getName());
                for (FileStatus rootFS : rootFileStatus
                        ) {
                    Path rootFSPath = rootFS.getPath();
                    logger.info("the name of dir is {}.", rootFSPath.getName());
                    logger.debug("year: {}, month: {}, day: {}.", year, month, day);
                    int yyyymmdd = Integer.valueOf(year + month + day);
                    int timeIdData = Integer.valueOf(rootFSPath.getName());
                    logger.debug("yyyymmdd: {}, timeIdData: {}.", yyyymmdd, timeIdData);
                    if (yyyymmdd > timeIdData) {
                        logger.info("Delete dir: {}.", rootFSPath.getName());
                        deleteDir(rootFSPath);
                    } else if (yyyymmdd == timeIdData) {
                        FileStatus[] hourFileStatus = fileSystem.listStatus(rootFSPath);
                        for (FileStatus hourFS : hourFileStatus
                                ) {
                            Path hourFSPath = hourFS.getPath();
                            int hh = formatDate.getHourOfDay();
                            int hour = Integer.valueOf(hourFSPath.getName());
                            logger.debug("hh: {}, hour: {}.", hh, hour);
                            if (hh > hour) {
                                logger.info("Delete dir: {}.", hourFSPath.getName());
                                deleteDir(hourFSPath);
                            } else if (hh == hour) {
                                FileStatus[] minutesFileStatus = fileSystem.listStatus(hourFSPath);
                                for (FileStatus minutesFS : minutesFileStatus
                                        ) {
                                    Path minutesFSPath = minutesFS.getPath();
                                    if (formatDate.getMinuteOfHour() > Integer.valueOf(minutesFSPath.getName())) {
                                        logger.info("Delete dir: {}.", minutesFSPath.getName());
                                        deleteDir(minutesFSPath);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to fetch files status from the {} path, exception: {}.", rootPath.toString(), e.getMessage());
            }
        } else {
            Path workPath = new Path(rootPath, getTimeFormatPath(formatDate, timeFormat));
            deleteDir(workPath);
        }
    }

    private String getTimeFormatPath(DateTime date, String timeFormat) {
        if (timeFormat.length() > 0) {
            return timeFormat.replaceAll("YYYY", String.valueOf(date.getYear()))
                    .replaceAll("MM", String.valueOf(date.getMonthOfYear()))
                    .replaceAll("dd", String.valueOf(date.getDayOfMonth()))
                    .replaceAll("HH", String.valueOf(date.getHourOfDay()))
                    .replaceAll("mm", String.valueOf(date.getMinuteOfHour()));
        }
        return null;
    }

    private String getWideTimeString(int time) {
        return time > 10 ? "" + time : "0" + time;
    }
}
