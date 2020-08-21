package com.gang.economico.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gang.economico.entities.Budget;

/**
 * 每个月预算数据库
 */
@Database(entities = {Budget.class}, version = 1, exportSchema = false)
public abstract class BudgetDatabase extends RoomDatabase {

    private static BudgetDatabase sBudgetDatabase;
    private BudgetDao mBudgetDao;

    public static synchronized BudgetDatabase getBudgetDatabase(Context context) {
        if (sBudgetDatabase == null) {
            sBudgetDatabase = Room.databaseBuilder(context.getApplicationContext(), BudgetDatabase.class, "budget_table")
                    .build();
        }
        return sBudgetDatabase;
    }

    public abstract BudgetDao getBudgetDao();

}
