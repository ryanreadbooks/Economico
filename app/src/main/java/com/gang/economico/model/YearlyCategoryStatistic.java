package com.gang.economico.model;

import androidx.room.ColumnInfo;

import java.math.BigDecimal;

/**
 * 年度分类数据表示
 */
public class YearlyCategoryStatistic{

    @ColumnInfo(name = "major_category")
    private String mCateName;
    @ColumnInfo(name = "total_yearly_amount")
    private String mAmount;

    public YearlyCategoryStatistic(String cateName, String amount) {
        mCateName = cateName;
        mAmount = amount;
    }

    public String getCateName() {
        return mCateName;
    }

    public void setCateName(String cateName) {
        mCateName = cateName;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    public BigDecimal getAmountDecimal() {
        return new BigDecimal(mAmount);
    }

    @Override
    public String toString() {
        return "YearlyCategoryStatistic{" +
                "mCateName='" + mCateName + '\'' +
                ", mAmount='" + mAmount + '\'' +
                '}';
    }
}
