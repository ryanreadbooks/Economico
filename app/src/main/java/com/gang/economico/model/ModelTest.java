package com.gang.economico.model;

public class ModelTest {

    private int imgResId;
    private String majorCategory;
    private String minorCategory;
    private int amount;
    private String amountText;
    private String mTime;
    private String mRemarks;

    public ModelTest(int imgResId, String majorCategory, String minorCategory, int amount, String time, String nRemarks) {
        this.imgResId = imgResId;
        this.majorCategory = majorCategory;
        this.minorCategory = minorCategory;
        this.amount = amount;
        this.mTime = time;
        this.mRemarks = nRemarks;
        this.amountText = "￥" + amount;
    }

    public int getImgResId() {
        return imgResId;
    }

    public void setImgResId(int imgResId) {
        this.imgResId = imgResId;
    }

    public String getMajorCategory() {
        return majorCategory;
    }

    public void setMajorCategory(String majorCategory) {
        this.majorCategory = majorCategory;
    }

    public String getMinorCategory() {
        return minorCategory;
    }

    public void setMinorCategory(String minorCategory) {
        this.minorCategory = minorCategory;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getAmountText() {
        return amountText;
    }

    public void setAmountText(String amountText) {
        this.amountText = amountText;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getRemarks() {
        return mRemarks;
    }

    public void setRemarks(String mRemarks) {
        this.mRemarks = mRemarks;
    }
}
