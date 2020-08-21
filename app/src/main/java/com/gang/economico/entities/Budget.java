package com.gang.economico.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Description: 预算实体类
 * Time: 4/30/2020
*/
@Entity(tableName = "budget_table")
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private int bid;
    @ColumnInfo(name = "budget_year")
    private int mBudgetYear;
    @ColumnInfo(name = "budget_month")
    private int mBudgetMonth;
    @ColumnInfo(name = "budget_amount")
    private String mBudgetAmount;


    public Budget(int budgetYear, int budgetMonth, String budgetAmount) {
        mBudgetYear = budgetYear;
        mBudgetMonth = budgetMonth;
        mBudgetAmount = budgetAmount;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public int getBudgetYear() {
        return mBudgetYear;
    }

    public void setBudgetYear(int budgetYear) {
        mBudgetYear = budgetYear;
    }

    public int getBudgetMonth() {
        return mBudgetMonth;
    }

    public void setBudgetMonth(int budgetMonth) {
        mBudgetMonth = budgetMonth;
    }

    public String getBudgetAmount() {
        return mBudgetAmount;
    }

    public void setBudgetAmount(String budgetAmount) {
        mBudgetAmount = budgetAmount;
    }
}
