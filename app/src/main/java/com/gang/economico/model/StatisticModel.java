package com.gang.economico.model;

import java.math.BigDecimal;

/**
 * 支出或者收入页面的分类的账单条目
 * 属性: 图标 名称 金额(Decimal)
 */
public class StatisticModel implements Comparable<StatisticModel>{

    private int mImgRes;
    private String mCategoryName;
    private BigDecimal mAmountDecimal;

    public StatisticModel() {}

    public StatisticModel(int imgRes, String categoryName, String amount) {
        mImgRes = imgRes;
        mCategoryName = categoryName;
        // 该分类的金额
        mAmountDecimal = new BigDecimal(amount);
    }

    public int getImgRes() {
        return mImgRes;
    }

    public void setImgRes(int imgRes) {
        mImgRes = imgRes;
    }

    public String getCategoryName() {
        return mCategoryName;
    }

    public void setCategoryName(String categoryName) {
        mCategoryName = categoryName;
    }

    public BigDecimal getAmountDecimal() {
        return mAmountDecimal;
    }

    public void setAmountDecimal(BigDecimal amountDecimal) {
        mAmountDecimal = amountDecimal;
    }

    public void updateAmount(BigDecimal a) {
        mAmountDecimal = mAmountDecimal.add(a);
    }

    // 两个对象的比较 按照金额的大小进行比较 逆序排列
    @Override
    public int compareTo(StatisticModel o) {
        return o.getAmountDecimal().compareTo(mAmountDecimal);
    }

    @Override
    public String toString() {
        return "StatisticModel{" +
                "mCategoryName='" + mCategoryName + '\'' +
                ", mAmountDecimal=" + mAmountDecimal +
                '}';
    }
}
