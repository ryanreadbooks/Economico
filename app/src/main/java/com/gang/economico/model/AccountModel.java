package com.gang.economico.model;


import androidx.room.ColumnInfo;


public class AccountModel {

    private int accountIconId;
    @ColumnInfo(name = "payment_method")
    private String accountName;
    @ColumnInfo(name = "total_amount")
    private String accountAmount;

    public AccountModel(String accountName, String accountAmount) {
        this.accountName = accountName;
        this.accountAmount = accountAmount;
    }

    public int getAccountIconId() {
        return accountIconId;
    }

    public void setAccountIconId(int accountIconId) {
        this.accountIconId = accountIconId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountAmount() {
        return accountAmount;
    }

    public void setAccountAmount(String accountAmount) {
        this.accountAmount = accountAmount;
    }

    @Override
    public String toString() {
        return "AccountModel{" +
                "accountIconId=" + accountIconId +
                ", accountName='" + accountName + '\'' +
                ", accountAmount='" + accountAmount + '\'' +
                '}';
    }
}
