package com.gang.economico.model;

public class MineSetting{

    private String mTitle;
    private String mDescription;
    private int mIconResId;

    public MineSetting(String title, String description, int iconResId) {
        mTitle = title;
        mDescription = description;
        mIconResId = iconResId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public int getIconResId() {
        return mIconResId;
    }

    public void setIconResId(int iconResId) {
        mIconResId = iconResId;
    }

    @Override
    public String toString() {
        return "MineSetting{" +
                "mTitle='" + mTitle + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mIconResId='" + mIconResId + '\'' +
                '}';
    }
}
