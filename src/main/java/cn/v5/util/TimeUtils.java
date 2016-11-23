package cn.v5.util;


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * time helper class
 */
public class TimeUtils {
    public static TimeZone getTimeZone(int timeZoneOffset) {
        if(timeZoneOffset > 13 || timeZoneOffset < -12) {
            timeZoneOffset = 0;
        }

        String[] ids = TimeZone.getAvailableIDs(timeZoneOffset * 60 * 60 * 1000);

        return ids.length == 0 ? TimeZone.getDefault() : TimeZone.getTimeZone(ids[0]);

    }

    public static int getHour(TimeZone timeZone) {
        return Calendar.getInstance(timeZone).get(Calendar.HOUR_OF_DAY);
    }

    public static Date getYesterdayStart () {
        Calendar calendar = Calendar.getInstance();
        //如果没有这种设定的话回去系统的当期的时间
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date date = new Date(calendar.getTimeInMillis());
        return date;
    }

    public static Date getTodayStart () {
        Calendar calendar = Calendar.getInstance();
        //如果没有这种设定的话回去系统的当期的时间
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = new Date(calendar.getTimeInMillis());
        return date;
    }

    public static Date getLastWeekStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -1 * 7);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date date = new Date(calendar.getTimeInMillis());
        return date;
    }

    public static Date getWeekStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date date = new Date(calendar.getTimeInMillis());
        return date;
    }


    public static void main(String[] args) {
        for(String s : TimeZone.getAvailableIDs()) {
            System.out.println(s);
        }


        System.out.println(getHour(TimeZone.getTimeZone("Canada/Pacific")));
        System.out.println(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
		
        System.out.println(getTodayStart());
        System.out.println(getYesterdayStart());

        System.out.println(getWeekStart());
        System.out.println(getLastWeekStart());
    }
}
