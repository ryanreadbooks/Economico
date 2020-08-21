package com.gang.economico.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CategoryQueryConditions implements Parcelable {

    // 分类的图片资源id
    private int mImgRes;
    // 分类名称，用来查询
    private String name;
    // 年份 用来查询
    private int year;
    // 这里的月份就是人类理解的月份(1-12月)
    private int month;
    // 这个分类的总金额
    private String totalAmount;

    public CategoryQueryConditions() {}

    public CategoryQueryConditions(int imgRes, String name, int year, int month, String totalAmount) {
        mImgRes = imgRes;
        this.name = name;
        this.year = year;
        this.month = month;
        this.totalAmount = totalAmount;
    }

    protected CategoryQueryConditions(Parcel in) {
        mImgRes = in.readInt();
        name = in.readString();
        year = in.readInt();
        month = in.readInt();
        totalAmount = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mImgRes);
        dest.writeString(name);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeString(totalAmount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CategoryQueryConditions> CREATOR = new Creator<CategoryQueryConditions>() {
        @Override
        public CategoryQueryConditions createFromParcel(Parcel in) {
            return new CategoryQueryConditions(in);
        }

        @Override
        public CategoryQueryConditions[] newArray(int size) {
            return new CategoryQueryConditions[size];
        }
    };

    public int getImgRes() {
        return mImgRes;
    }

    public void setImgRes(int imgRes) {
        mImgRes = imgRes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
