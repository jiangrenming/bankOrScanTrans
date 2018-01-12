package com.nld.cloudpos.util;

import com.nld.logger.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import common.FileUtils;

/**
 * 将LogCat中当前应用的日志保存到mtms管理的目录下
 * 日志保存在"mnt/sdcard/mtms/log/"目录下
 *
 * @author cxg
 */


public class LogcatHelper {

    private static final int LOG_COUNT = 7; // 保存最近的7天的日志文件

    private String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;
    }

    private static LogcatHelper instance;

    private LogcatHelper() {
    }

    public static LogcatHelper getInstance() {
        if (instance == null) {
            instance = new LogcatHelper();
        }
        return instance;
    }

    public void deleteLog() {
        new Thread() {
            @Override
            public void run() {
                checkOutTimeAndDelectLogFile();
            }
        }.start();
    }

    /**
     * 检查过期日志并删除
     */
    private void checkOutTimeAndDelectLogFile() {
        String dirPath = CommonContants.LOG_LOCAL_PAHT;
        String curDate = getFileName();
        try {
            File pathFile = new File(dirPath);
            File[] files = pathFile.listFiles();
            String destDate = findLastMD(curDate);
            if (files == null || files.length == 0) {
                return;
            }
            for (File file : files) {
                if (file.getName().compareTo(destDate.substring(0, 6)) < 0) {
                    FileUtils.deleteFile(file, true);
                } else if (file.getName().compareTo(
                        destDate.substring(0, 6)) == 0) {
                    File[] logFiles = file.listFiles();
                    if (logFiles == null || logFiles.length == 0) {
                        return;
                    }
                    for (File logFile : logFiles) {
                        if (destDate.compareTo(logFile.getName().replace(
                                ".log", "")) >= 0) {
                            logFile.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.d("删除本地日志失败 : " + e.getMessage());
        }
    }

    private String findLastMD(String curDate) {
        try{
            int y = Integer.valueOf(curDate.substring(0, 4));
            int m = Integer.valueOf(curDate.substring(4, 6));
            int d = Integer.valueOf(curDate.substring(6, 8));

            d = d - LOG_COUNT;
            if (d <= 0) {
                d = 30 + d;
                if (m == 1) {
                    y = y - 1;
                    m = 12;
                } else {
                    m = m - 1;
                }
            }

            String year = String.valueOf(y);
            String mounth = String.valueOf(m);
            mounth = mounth.length() == 1 ? ("0" + mounth) : mounth;
            String day = String.valueOf(d);
            day = day.length() == 1 ? ("0" + day) : day;

            return year + mounth + day;
        } catch (Exception e){

        }
        return "";
    }
}
