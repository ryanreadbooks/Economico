package com.gang.economico.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "category_table")
public class CategoryModel implements Serializable {

    private static final long serialVersionUID = -6672355238900707241L;
    @PrimaryKey(autoGenerate = true)
    private int cid;
    @ColumnInfo(name = "img_res")
    private String mImgRes;
    @ColumnInfo(name = "category_name")
    private String mCategoryName;
    @ColumnInfo(name = "isSpending")
    private boolean isSpending;

    public CategoryModel(String imgRes, String categoryName, boolean spending) {
        mImgRes = imgRes;
        mCategoryName = categoryName;
        isSpending = spending;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getImgRes() {
        return mImgRes;
    }

    public void setImgRes(String imgRes) {
        mImgRes = imgRes;
    }

    public String getCategoryName() {
        return mCategoryName;
    }

    public void setCategoryName(String categoryName) {
        mCategoryName = categoryName;
    }

    public boolean isSpending() {
        return isSpending;
    }

    public void setSpending(boolean spending) {
        isSpending = spending;
    }

    public int getImgResInt() {
        return Integer.parseInt(mImgRes);
    }

    @Override
    public String toString() {
        return "CategoryModel{" +
                "cid=" + cid +
                ", mImgRes='" + mImgRes + '\'' +
                ", mCategoryName='" + mCategoryName + '\'' +
                ", isSpending=" + isSpending +
                '}';
    }
}
