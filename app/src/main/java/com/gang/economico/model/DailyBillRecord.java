package com.gang.economico.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

/**
 * Description: 用于返回查询日数据汇总的类
 * Time: 4/22/2020
*/
public class DailyBillRecord {

    @ColumnInfo(name = "record_day")
    private int mRecordDay;
    // 这里的ColumnInfo里面的字段要和sql语句中查询的字段匹配，如果查询的字段施加了函数在上面，那么这里的ColumnInfo也要相应得加上对应得函数
    @ColumnInfo(name = "sum(amount)")
    @NonNull
    private String mAmount;

    public DailyBillRecord(int recordDay, String amount) {
        mRecordDay = recordDay;
        mAmount = amount;
    }

    public int getRecordDay() {
        return mRecordDay;
    }

    public void setRecordDay(int recordDay) {
        mRecordDay = recordDay;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    @Override
    public String toString() {
        return "DailyBillRecord{" +
                "mRecordDay=" + mRecordDay +
                ", mTotalAmount='" + mAmount + '\'' +
                '}';
    }
}
