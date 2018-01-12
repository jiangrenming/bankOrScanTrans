package common;

import android.annotation.SuppressLint;
import android.os.SystemClock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtil {

    // 时间格式
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String HHMMSS = "HHmmss";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String YYYYMMDDHHMMssSSS = "yyyyMMddHHmmssSSS";//有毫秒时间
    public static final String YYYY = "yyyy";

    /**
     * 格式化时间，将YYYYMMDDHHSS格式化为 YYYY/MM/DD HH:SS
     *
     * @param time 格式： YYYY/MM/DD HH:SS
     * @return
     * @createtor: Administrator
     * @date:2014-2-19 下午04:07:13
     */
    public static String formatTime(String time) {
        if (time.length() != 14) {
            return time;
        }

        return time.substring(0, 4) + "/" + time.substring(4, 6) + "/"
                + time.substring(6, 8) + " " + time.substring(8, 10) + ":"
                + time.substring(10, 12) + ":" + time.substring(12, 14);
    }


    /**
     * 将系统时间转换成年月日 时分秒
     * @param time
     * @return
     */
    public static String formatMillisecondAllDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String strDate = format.format(date);
        return strDate;
    }
    /**
     * 将毫秒转换成yyyy-MM-dd格式日期
     *
     * @param time 毫秒时间
     * @return 年-月-日
     */
    public static String formatMillisecondDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String strDate = format.format(date);
        return strDate;
    }

    /**
     * 将毫秒转换成HH:mm:ss格式时间
     *
     * @param time 毫秒时间
     * @return 时:分:秒
     */
    public static String formatMillisecondTimeSecond(long time) {
        String strTime = "";
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(time);
        strTime = format.format(date);
        return strTime;
    }


    /**
     * 将毫秒转换成HH:mm格式时间
     *
     * @param time 毫秒时间
     * @return 时:分:秒
     */
    public static String formatMillisecondTime(long time) {
        String strTime = "";
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Date date = new Date(time);
        strTime = format.format(date);
        return strTime;
    }

    /**
     * 设置系统时间
     *
     * @param year
     * @param month 0~11
     * @param day
     */
    public static void setCurrentDateTime(int year, int month, int day) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
    }

    /**
     * 设置系统时间
     *
     * @param year
     * @param month  0~11
     * @param day
     * @param minute
     * @param hour
     */
    public static void setCurrentDateTime(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.HOUR, hour);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
    }

    /**
     * 设置系统时间
     *
     * @param hour
     * @param minute
     */
    public static void setCurrentDateTime(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
    }

    /**
     * 获取当前的系统时间字符串
     *
     * @param dateFormat 日期格式，例如格式yyyyMMddhhmmss
     * @return
     */
    public static String getCurrentDate(String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(new Date());
    }

    /**
     * 获取当前的系统日期
     *
     * @return 日期格式，例如格式MMdd
     */
    public static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMdd");
        return formatter.format(new Date());
    }

    /**
     * 获取当前的系统时间
     *
     * @return 日期格式，例如格式HHmmss
     */
    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HHmmss");
        return formatter.format(new Date());
    }

    /**
     * 把时间戳转化为格式化日期
     *
     * @param pattern  时间格式
     * @param dateTime 时间戳
     * @return 格式化时间格式String
     */
    public static String timeLongToString(String pattern, long dateTime) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        return sDateFormat.format(new Date(dateTime + 0));
    }
    /**
     * 日期和时间格式化(格式如 2015/06/08 22:12:09)
     *
     * @param date 日期
     * @param time 时间
     * @return 日期和时间格式化
     */
    @SuppressLint("SimpleDateFormat")
    public static String timeFormat(String date, String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
        String reslt = "";
        try {
            Date date2 = simpleDateFormat2.parse(String.valueOf(Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR)) + date + time);
            reslt = simpleDateFormat.format(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reslt;
    }
    /**
     * 获取当前的系统日期
     * @return 日期格式，例如格式yyyyMMdd
     */
    public static String getCurrentYearDate() {
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        return simpleDateFormat.format(new Date());
    }

}
