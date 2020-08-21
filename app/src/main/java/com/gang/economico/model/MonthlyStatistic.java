package com.gang.economico.model;


import androidx.room.ColumnInfo;

public class MonthlyStatistic {

    @ColumnInfo(name = "record_month")
    private String mMonth;
    @ColumnInfo(name = "sum(amount)")
    private String mMonthlyAmount;

    public MonthlyStatistic(String month, String monthlyAmount) {
        mMonth = month;
        mMonthlyAmount = monthlyAmount;
    }

    public String getMonth() {
        return mMonth;
    }

    public void setMonth(String month) {
        mMonth = month;
    }

    public String getMonthlyAmount() {
        return mMonthlyAmount;
    }

    public void setMonthlyAmount(String monthlyAmount) {
        mMonthlyAmount = monthlyAmount;
    }

    @Override
    public String toString() {
        return "MonthlyStatistic{" +
                "mMonth='" + mMonth + '\'' +
                ", mMonthlyAmount='" + mMonthlyAmount + '\'' +
                '}';
    }
}
