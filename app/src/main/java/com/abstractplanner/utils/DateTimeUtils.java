package com.abstractplanner.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class DateTimeUtils {

    public static int compareDatesWithoutTime(Calendar firstDate, Calendar secondDate){
        if(firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR)
                && firstDate.get(Calendar.DAY_OF_YEAR) == secondDate.get(Calendar.DAY_OF_YEAR))
            return 0;
        else
            if(firstDate.get(Calendar.YEAR) > secondDate.get(Calendar.YEAR)
                    || firstDate.get(Calendar.DAY_OF_YEAR) > secondDate.get(Calendar.DAY_OF_YEAR))
            return 1;
        else
            return -1;
    }

    public static Calendar getInstanceDayInCurrentTimeZone(long dateTimeInMilliseconds, TimeZone fromTimeZone){
        Calendar calendar = new GregorianCalendar(fromTimeZone);
        calendar.setTimeInMillis(dateTimeInMilliseconds);

        return new GregorianCalendar(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static Calendar getInstanceInCurrentTimeZone(long dateTimeInMilliseconds, TimeZone fromTimeZone){
        Calendar calendar = new GregorianCalendar(fromTimeZone);
        calendar.setTimeInMillis(dateTimeInMilliseconds);

        return new GregorianCalendar(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

    public static Calendar getCalendarInstance(long dateTimeInMilliseconds){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTimeInMilliseconds);

        return calendar;
    }

    public static Calendar getTodayDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

}
