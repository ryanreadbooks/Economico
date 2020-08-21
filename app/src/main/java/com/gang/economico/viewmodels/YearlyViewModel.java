package com.gang.economico.viewmodels;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.gang.economico.databases.BillDatabase;
import com.gang.economico.databases.BillDatabaseDao;
import com.gang.economico.model.MonthlyStatistic;
import com.gang.economico.model.YearlyCategoryStatistic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 管理年度数据界面的ViewModel
 */
public class YearlyViewModel extends AndroidViewModel {

    private static final String TAG = "YearlyViewModel";
    private static BillDatabaseDao mBillDao;

    private MutableLiveData<Integer> mYearLiveData;                      // 查看的年份
    private int mQueryCondition;                                        // 查询条件
    private MutableLiveData<String> mTotalAmountLiveData;               // 数据的总金额
    private List<MonthlyStatistic> mYearlyList;
    private MutableLiveData<List<MonthlyStatistic>> mYearlyLiveData;   // 每个月份的数据列表
    private List<YearlyCategoryStatistic> mCategorizedList;
    private MutableLiveData<List<YearlyCategoryStatistic>> mCategorizedLiveData; // 各个分类的数据
    private MutableLiveData<List<Float>> mMonthBillsLiveData;

    public YearlyViewModel(@NonNull Application application) {
        super(application);
        BillDatabase billDatabase = BillDatabase.getBillDatabase(application.getApplicationContext());
        mBillDao = billDatabase.getBillDatabaseDao();

        // 默认查询条件 当前年份 + 支出数据
        mYearLiveData = new MutableLiveData<>();
        mYearLiveData.setValue(Calendar.getInstance().get(Calendar.YEAR));
        mQueryCondition = AccountsViewModel.QUERY_SPENDING;
        mTotalAmountLiveData = new MutableLiveData<>();
        mYearlyList = new ArrayList<>();
        mYearlyLiveData = new MutableLiveData<>(mYearlyList);

        mCategorizedList = new ArrayList<>();
        mCategorizedLiveData = new MutableLiveData<>(mCategorizedList);

        mMonthBillsLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Integer> getYearLiveData() {
        return mYearLiveData;
    }

    public void setYearLiveData(int year) {
        mYearLiveData.setValue(year);
    }

    public MutableLiveData<String> getTotalAmountLiveData() {
        return mTotalAmountLiveData;
    }

    private void setTotalAmountLiveData(String newAmount) {
        mTotalAmountLiveData.postValue(newAmount);
    }

    public MutableLiveData<List<MonthlyStatistic>> getYearlyLiveData() {
        return mYearlyLiveData;
    }

    private void setYearlyLiveData(List<MonthlyStatistic> list) {
        if (list != null) {
            if (mYearlyList.size() != 0) {
                mYearlyList.clear();
            }
            mYearlyList.addAll(list);
            mYearlyLiveData.postValue(mYearlyList);
        }
    }

    public MutableLiveData<List<YearlyCategoryStatistic>> getCategorizedLiveData() {
        return mCategorizedLiveData;
    }

    private void setCategorizedLiveData(List<YearlyCategoryStatistic> categorized) {
        if (categorized != null) {
            if (mCategorizedList.size() != 0) {
                mCategorizedList.clear();
            }
            mCategorizedList.addAll(categorized);
            mCategorizedLiveData.postValue(mCategorizedList);
        }
    }

    public MutableLiveData<List<Float>> getMonthBillsLiveData() {
        return mMonthBillsLiveData;
    }

    private void setMonthBillsLiveData(List<Float> l) {
        if (l != null) {
            mMonthBillsLiveData.postValue(l);
        }
    }

    public int getQueryCondition() {
        return mQueryCondition;
    }

    public void setQueryCondition(int newCondition) {
        mQueryCondition = newCondition;
    }

    /**
     * Overview 页面数据加载
     */
    public void loadOverviewData() {
        // 查询条件为该类内部的条件 -> mYearLiveData 和 mQueryCondition
        if (mYearLiveData.getValue() != null) {
            new Thread(() -> {
                List<MonthlyStatistic> resultList = new ArrayList<>();
                int queryYear = mYearLiveData.getValue();
                try {
                    // 异步操作
                    resultList = new GetYearlyBillsAsyncTask(mBillDao).execute(queryYear, mQueryCondition).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "run: result " + resultList);
                setYearlyLiveData(resultList);
                // 求年度年的总金额
                BigDecimal totalAmountDecimal = new BigDecimal("0.00");
                List<String> tempAlready = new ArrayList<>();  // 已经有的月份
                for (MonthlyStatistic monthlyStatistic : resultList) {
                    totalAmountDecimal = totalAmountDecimal.add(new BigDecimal(monthlyStatistic.getMonthlyAmount()));
                    tempAlready.add(monthlyStatistic.getMonth());
                }
                Log.d(TAG, "loadOverviewData: temp already " + tempAlready);
                List<Float> amountList = new ArrayList<>();
                for (int i = 1; i <= 12; i++) {
                    if (tempAlready.contains(String.valueOf(i))) {
                        Log.d(TAG, "loadOverviewData: contains " + i);
                        int index = tempAlready.indexOf(i+"");
                        amountList.add(Float.valueOf(resultList.get(index).getMonthlyAmount()));
                    }
                    else {
                        amountList.add(0.0f);
                    }
                }
                Log.d(TAG, "loadOverviewData: month bills "+ amountList);
                setMonthBillsLiveData(amountList);
                Log.d(TAG, "loadOverviewData: total amount " + totalAmountDecimal.toPlainString());
                setTotalAmountLiveData(totalAmountDecimal.toPlainString());
            }).start();
        }
    }

    /**
     * Category 页面数据加载
     */
    public void loadCategorizedData() {
        if (mYearLiveData.getValue() != null) {
            new Thread(() -> {
                int queryYear = mYearLiveData.getValue();
               List<YearlyCategoryStatistic> resultList = new ArrayList<>();
                try {
                    resultList = new GetYearlyCategorizedBillsAsyncTask(mBillDao).execute(queryYear, mQueryCondition).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "loadCategorizedData: category result " + resultList);
                setCategorizedLiveData(resultList);
            }).start();
        }
    }
}


/**
 * 异步查询数据可获得年度数据 按月分组
 */
class GetYearlyBillsAsyncTask extends AsyncTask<Integer, Void, List<MonthlyStatistic>> {

    private BillDatabaseDao mDao;
    GetYearlyBillsAsyncTask(BillDatabaseDao dao) {
        mDao = dao;
    }

    @Override
    protected List<MonthlyStatistic> doInBackground(Integer... integers) {
        int year = integers[0];
        int condition = integers[1];
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "select record_month, sum(amount) from bill_table " +
                        "where record_year = ? and isSpending = ? " +
                        "group by record_month " +
                        "order by record_month asc",
                new Object[] {year, condition}
        );
        return mDao.retrieveYearlyBillsFromDB(query);
    }
}


class GetYearlyCategorizedBillsAsyncTask extends AsyncTask<Integer, Void, List<YearlyCategoryStatistic>> {

    private BillDatabaseDao mDao;
    GetYearlyCategorizedBillsAsyncTask(BillDatabaseDao dao) {
        mDao = dao;
    }

    @Override
    protected List<YearlyCategoryStatistic> doInBackground(Integer... integers) {
        int year = integers[0];
        int condition = integers[1];
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "select major_category, sum(amount) as total_yearly_amount from bill_table " +
                        "where record_year = ? and isSpending = ?" +
                        "group by major_category " +
                        "order by total_yearly_amount desc",
                new Object[] {year, condition});
        return mDao.retrieveYearlyCategorizedFromDB(query);
    }
}
