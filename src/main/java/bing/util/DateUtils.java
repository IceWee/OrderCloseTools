package bing.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 日期工具类
 *
 * @author IceWee
 */
public class DateUtils {

    private static final String PATTERN_YYYY_MM = "yyyy-MM";

    private static final String PATTERN_ORDER_DATETIME = "yyyyMMddHHmmssSSS";

    private static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 获取起始月份中间的全部月份 yyyy-MM
     *
     * @param startMonth
     * @param endMonth
     * @return
     * @throws ParseException
     */
    public static List<String> getMonthList(String startMonth, String endMonth) throws ParseException {
        List<String> list = new ArrayList<>();
        Date start = org.apache.commons.lang3.time.DateUtils.parseDate(startMonth, PATTERN_YYYY_MM);
        Date end = org.apache.commons.lang3.time.DateUtils.parseDate(endMonth, PATTERN_YYYY_MM);
        while (start.before(end)) {
            list.add(DateFormatUtils.format(start, PATTERN_YYYY_MM));
            start = org.apache.commons.lang3.time.DateUtils.addMonths(start, 1);
        }
        list.add(DateFormatUtils.format(end, PATTERN_YYYY_MM));
        return list;
    }

    /**
     * 格式化订单号中的日期
     *
     * @param date
     * @return
     */
    public static String formatOrderDate(Date date) {
        return DateFormatUtils.format(date, PATTERN_ORDER_DATETIME);
    }

    public static String formatOrderDate(long date) {
        return DateFormatUtils.format(new Date(date), PATTERN_ORDER_DATETIME);
    }

    public static String formatDateTime(Date date) {
        return DateFormatUtils.format(date, PATTERN_DATETIME);
    }

    public static String formatDateTime(long date) {
        return DateFormatUtils.format(new Date(date), PATTERN_DATETIME);
    }

    public static Date parseYearMonth(String yyyyMM) throws ParseException {
        return org.apache.commons.lang3.time.DateUtils.parseDate(yyyyMM, PATTERN_YYYY_MM);
    }

    /**
     * 获取某月分内的随机时间毫秒
     *
     * @param yyyyMM
     * @return
     * @throws ParseException
     */
    public static long randomLong(String yyyyMM) throws ParseException {
        Date date = parseYearMonth(yyyyMM);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long begin = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        long end = calendar.getTimeInMillis();
        return randomLong(begin, end);
    }

    /**
     * 获取随机时间
     *
     * @param yyyyMM 2017-04
     * @param minHour 最小小时
     * @param maxHour 最大小时
     * @return
     * @throws ParseException
     */
    public static long randomLong(String yyyyMM, int minHour, int maxHour) throws ParseException {
        long randomLong = randomLong(yyyyMM);
        Date date = new Date(randomLong);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, randomInteger(minHour, maxHour));
        calendar.set(Calendar.MINUTE, randomInteger(0, 59));
        calendar.set(Calendar.SECOND, randomInteger(0, 59));
        return calendar.getTimeInMillis();
    }

    public static long randomLong(long begin, long end) {
        long ranLong = begin + (long) (Math.random() * (end - begin));
        //如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
        if (ranLong == begin || ranLong == end) {
            return randomLong(begin, end);
        }
        return ranLong;
    }

    public static int randomInteger(int begin, int end) {
        return begin + (int) (Math.random() * (end - begin));
    }

    /**
     * 获取随机下一刻
     *
     * @param currentMillis
     * @param maxMillis
     * @return
     */
    public static long nextRandomLong(long currentMillis, int maxMillis) {
        return randomLong(currentMillis, currentMillis + maxMillis);
    }

    // W2017 05 26 10 02 54 872 730462
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        String str = StringUtils.join(list.toArray(), "','");
        str = "'" + str + "'";
        System.err.println(str);
    }

}
