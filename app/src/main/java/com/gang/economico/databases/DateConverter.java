package com.gang.economico.databases;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    // 将Date类型转换为Long对象
    @TypeConverter
    public static Date fromTimestamp (Long value) {
        return value == null ? null : new Date(value);
    }

    // 将Long类型转换为Date对象
    @TypeConverter
    public static Long dateToTimestamp (Date date) {
        return date == null ? null : date.getTime();
    }
}
