package com.gang.economico.databases;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CalendarConverter {

    public static String calendarToString(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        return calendar == null ? null : dateFormat.format(calendar.getTime());
    }

    public static Calendar stringToCalendar(@NonNull String timeString) {
        Calendar calendar = new GregorianCalendar();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
            Date date = dateFormat.parse(timeString);
            assert date != null;
            calendar.setTime(date);
        }
        catch (ParseException e) {

        }

        return calendar;
    }
}
