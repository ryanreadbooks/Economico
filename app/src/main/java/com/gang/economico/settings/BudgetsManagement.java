package com.gang.economico.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.gang.economico.databases.BudgetDao;
import com.gang.economico.databases.BudgetDatabase;
import com.gang.economico.entities.Budget;

import java.util.concurrent.ExecutionException;

/**
 * 预算管理和操作类
 * */
public class BudgetsManagement {

    public static final String TAG = "BudgetsManagement";

    /**
     * 添加预算
     * @param year 预算 年
     * @param month 预算 月(1~12)
     * @param amount 预算金额
     */
    public static void addNewBudget(Context context, int year, int month, String amount) {
        Budget newBudget = new Budget(year, month, amount);
        BudgetDatabase database = BudgetDatabase.getBudgetDatabase(context);
        BudgetDao dao = database.getBudgetDao();
        new AddNewBudgetAsyncTask(dao).execute(newBudget);
    }

    public static void updateBudget(Context context, Budget updatingBudget) {
        BudgetDatabase database = BudgetDatabase.getBudgetDatabase(context);
        BudgetDao dao = database.getBudgetDao();
        new UpdateBudgetAsyncTask(dao).execute(updatingBudget);
    }

    /**
     * 获取指定年月的预算
     * @param context **
     * @param year 年
     * @param month 月 (1~12)
     * @return 预算金额
     */
    public static String getBudget(Context context, int year, int month) {
        BudgetDatabase database = BudgetDatabase.getBudgetDatabase(context);
        BudgetDao dao = database.getBudgetDao();
        Budget resultBudget = null;
        String amount;
        try {
            resultBudget = new GetBudgetAsyncTask(dao).execute(year, month).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (resultBudget != null) {
            amount = resultBudget.getBudgetAmount();
        }
        else {
            amount = "not set";
        }
        return amount;
    }

    /**
     * 获取指定年月的预算
     * @param context **
     * @param year 年
     * @param month 月(1~12)
     * @return 整个预算对象
     */
    public static Budget getBudget2(Context context, int year, int month) {
        BudgetDatabase database = BudgetDatabase.getBudgetDatabase(context);
        BudgetDao dao = database.getBudgetDao();
        Budget resultBudget = null;
        try {
            resultBudget = new GetBudgetAsyncTask(dao).execute(year, month).get();
            Log.d(TAG, "getBudget: " + resultBudget);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return resultBudget;
    }
}


/**
 * 异步操作插入新预算信息
 */
class AddNewBudgetAsyncTask extends AsyncTask<Budget, Void, Void> {

    private BudgetDao mDao;
    AddNewBudgetAsyncTask(BudgetDao dao) {
        mDao = dao;
    }
    @Override
    protected Void doInBackground(Budget... budgets) {
        mDao.insertBudgetToDB(budgets[0]);
        return null;
    }
}


/**
 * 异步操作更新预算信息
 */
class UpdateBudgetAsyncTask extends AsyncTask<Budget, Void, Void> {

    private BudgetDao mDao;
    UpdateBudgetAsyncTask(BudgetDao dao) {
        mDao = dao;
    }

    @Override
    protected Void doInBackground(Budget... budgets) {
        mDao.updateBudgetInDB(budgets[0]);
        return null;
    }
}


/**
 * 异步操作查询预算信息
 */
class GetBudgetAsyncTask extends AsyncTask<Integer, Void, Budget> {

    private BudgetDao mDao;
    GetBudgetAsyncTask(BudgetDao dao) {
        mDao = dao;
    }

    @Override
    protected Budget doInBackground(Integer... integers) {
        int year = integers[0];
        int month = integers[1];
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("select * from budget_table where budget_year = ? and budget_month = ?",
                new Object[] {year, month});
        return mDao.retrieveBudgetFromDB(query);
    }
}
