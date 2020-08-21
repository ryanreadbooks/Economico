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
import com.gang.economico.entities.BillRecord;
import com.gang.economico.model.CategoryQueryConditions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 存放分类信息的ViewModel, 收入分类和支出分类用同一个Activity 但内容不一样
 */
public class CategoryViewModel extends AndroidViewModel {

    private static final String TAG = "CategoryViewModel";
    // 数据库引用 Dao引用
    private static BillDatabase mBillDatabase;
    private static BillDatabaseDao mDatabaseDao;
    // 要展示的数据的列表
    private List<List<BillRecord>> mCateList;
    private MutableLiveData<List<List<BillRecord>>> mCateListLiveData;
    // 总金额 数据条数
    private MutableLiveData<String> mCateAmountLiveData;
    private MutableLiveData<String> mCateCountLiveData;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        mBillDatabase = BillDatabase.getBillDatabase(application.getApplicationContext());
        mDatabaseDao = mBillDatabase.getBillDatabaseDao();

        mCateList = new ArrayList<>();
        mCateListLiveData = new MutableLiveData<>(mCateList);

        mCateAmountLiveData = new MutableLiveData<>();
        mCateCountLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<String> getCateAmountLiveData() {
        return mCateAmountLiveData;
    }

    public void setCateAmountLiveData(String newAmount) {
        mCateAmountLiveData.setValue(newAmount);
    }

    public MutableLiveData<String> getCateCountLiveData() {
        return mCateCountLiveData;
    }

    public void setCateCountLiveData(String newCount) {
        mCateCountLiveData.setValue(newCount);
    }

    public MutableLiveData<List<List<BillRecord>>> getCateListLiveData() {
        return mCateListLiveData;
    }

    /**
     * 从数据库中获取指定分类的信息，并设置LiveData
     */
    private void setCateListLiveData() {

    }

    /**
     * 外部更新界面请求统一入口方法
     * @param queryConditions 查询条件
     */
    public void getCategorizedData(CategoryQueryConditions queryConditions) {
        List<BillRecord> primaryResult = new ArrayList<>();
        BigDecimal totalAmount = new BigDecimal("0.00");
        try {
            primaryResult.addAll(new GetCategorizedBillsAsyncTask(mDatabaseDao).execute(queryConditions).get());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        // 对数据库返回的结果进行筛选，把每日的数据分开
        if (mCateList.size() != 0) {
            mCateList.clear();
        }
        // 更新主要的数据列表
        List<Integer> daysHad = new ArrayList<>();  // 临时记录已经有的列表
        for (BillRecord billRecord : primaryResult) {
            totalAmount = totalAmount.add(billRecord.getAmountDecimal());
            int day = billRecord.getRecordDay();
            if (daysHad.contains(day)) {
                // 新增已有的天数
                mCateList.get(daysHad.indexOf(day)).add(billRecord);
            }
            else {
                // 新建没有的天数
                List<BillRecord> newDayList = new ArrayList<>();
                newDayList.add(billRecord);
                mCateList.add(newDayList);
                daysHad.add(day);
            }
        }
        // 更新LiveData
        mCateCountLiveData.setValue(String.valueOf(primaryResult.size()));
        mCateAmountLiveData.setValue(totalAmount.toPlainString());
        mCateListLiveData.setValue(mCateList);
        Log.d(TAG, "getCategorizedData: result " + primaryResult);
        Log.d(TAG, "getCategorizedData: " + mCateList);
    }
}


/**
 * 异步从数据库取出分类信息
 */
class GetCategorizedBillsAsyncTask extends AsyncTask<CategoryQueryConditions, Void, List<BillRecord>> {

    private BillDatabaseDao mDao;
    public GetCategorizedBillsAsyncTask(BillDatabaseDao dao) {
        mDao = dao;
    }

    @Override
    protected List<BillRecord> doInBackground(CategoryQueryConditions... queryConditions) {
        int queryYear = queryConditions[0].getYear();
        int queryMonth = queryConditions[0].getMonth();
        String queryCategory = queryConditions[0].getName();
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "select * from bill_table where record_year = ? and record_month = ? and major_category = ? order by record_day desc",
                new Object[] {queryYear, queryMonth, queryCategory});

        return mDao.retrieveCategorizedBillsFromDB(query);
    }
}
