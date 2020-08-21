package com.gang.economico.viewmodels;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.gang.economico.R;
import com.gang.economico.settings.BudgetsManagement;
import com.gang.economico.databases.BillDatabase;
import com.gang.economico.databases.BillDatabaseDao;
import com.gang.economico.entities.BillRecord;
import com.gang.economico.model.DailyBillRecord;
import com.gang.economico.model.StatisticModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Description: 持有数据库的引用，对外提供数据库的查询接口
 * Time: 4/16/2020
*/
public class BillViewModel extends AndroidViewModel {

    private static final String TAG = "BillViewModel";
    private static final int SELECT_SPENDING = 1;
    private static final int SELECT_INCOME = 0;

    // 数据库操作对象引用
    private static BillDatabaseDao mDatabaseDao;

    private MutableLiveData<String> mSpendingLiveData;  // 当月总支出
    private MutableLiveData<String> mIncomeLiveData;    // 当月总收入
    private MutableLiveData<String> mSurplusLiveData;   // 当月结余(总收入-总支出)
    // 主信息展示列表
    private List<BillRecord> mMainList;
    private MutableLiveData<List<BillRecord>> mMainListLiveData;
    // 更加细节的每日信息和每个分类的信息
    private List<StatisticModel> mSpendingStat;     // 当月数据所有支出的分类
    private List<StatisticModel> mIncomeStat;       // 当月数据所有收入的分类
    private MutableLiveData<List<StatisticModel>> mSpendingStatLiveData;
    private MutableLiveData<List<StatisticModel>> mIncomeStatLiveData;
    // 考虑是否用BigDecimal
    private List<Float> mDailySpending;                 // 当月数据的每日支出详情
    private List<Float> mDailyIncome;                   // 当月数据的每日收入详情
    private MutableLiveData<List<Float>> mDailySpendingLiveData;
    private MutableLiveData<List<Float>> mDailyIncomeLiveData;
    // 预算的LiveData
    private String mBudget;
    private MutableLiveData<String> mBudgetLiveData;

    // 当前记录的时间
    private int viewModelYear;
    private int viewModelMonth;

    private Context mContext;

    public BillViewModel(Application application) {
        super(application);
        mContext = application.getApplicationContext();
        // 数据库引用
        BillDatabase billDatabase = BillDatabase.getBillDatabase(mContext);
        mDatabaseDao = billDatabase.getBillDatabaseDao();

        mSpendingLiveData = new MutableLiveData<>();
        mIncomeLiveData = new MutableLiveData<>();
        mSurplusLiveData = new MutableLiveData<>();
        mMainList = new ArrayList<>();
        mMainListLiveData = new MutableLiveData<>(mMainList);

        mSpendingStat = new ArrayList<>();
        mIncomeStat = new ArrayList<>();
        mSpendingStatLiveData = new MutableLiveData<>(mSpendingStat);
        mIncomeStatLiveData = new MutableLiveData<>(mIncomeStat);

        mDailySpending = new ArrayList<>();
        mDailyIncome = new ArrayList<>();
        mDailySpendingLiveData = new MutableLiveData<>();
        mDailyIncomeLiveData = new MutableLiveData<>();

        // 默认时间
        viewModelYear = Calendar.getInstance().get(Calendar.YEAR);
        viewModelMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;    // 1~12月

        // 预算 默认取得当月的预算
        mBudget = BudgetsManagement.getBudget(mContext, viewModelYear, viewModelMonth);
        mBudgetLiveData = new MutableLiveData<>(mBudget);
    }

    public LiveData<String> getSpendingLiveData() {
        return mSpendingLiveData;
    }

    private void setSpendingLiveData(String newSpending) {
        mSpendingLiveData.postValue(newSpending);
    }

    public LiveData<String> getIncomeLiveData() {
        return mIncomeLiveData;
    }

    private void setIncomeLiveData(String newIncome) {
        mIncomeLiveData.postValue(newIncome);
    }

    public LiveData<String> getSurplusLiveData() {
        return mSurplusLiveData;
    }

    private void setSurplusLiveData(String newSurplus) {
        mSurplusLiveData.postValue(newSurplus);
    }

    public LiveData<List<BillRecord>> getMainListLiveData() {
        return mMainListLiveData;
    }

    public LiveData<List<StatisticModel>> getSpendingStatLiveData() {
        return mSpendingStatLiveData;
    }

    public LiveData<List<StatisticModel>> getIncomeStatLiveData() {
        return mIncomeStatLiveData;
    }

    public MutableLiveData<List<Float>> getDailySpendingLiveData() {
        return mDailySpendingLiveData;
    }

    public MutableLiveData<List<Float>> getDailyIncomeLiveData() {
        return mDailyIncomeLiveData;
    }

    public int getViewModelYear() {
        return viewModelYear;
    }

    public int getViewModelMonth() {
        return viewModelMonth;
    }

    public LiveData<String> getBudgetLiveData() {
        return mBudgetLiveData;
    }

    public void setBudgetLiveData(String newBudgetAmount) {
        if (newBudgetAmount != null) {
            mBudgetLiveData.postValue(newBudgetAmount);
        }
    }

    private void setMainListLiveData(List<BillRecord> recordList) {
        if (mMainList == null) {
            mMainList = new ArrayList<>();
        }
        if (mMainList.size() != 0) {
            mMainList.clear();
        }
        mMainList.addAll(recordList);
        // 开辟新线程进行较为耗时的分类和求和计算
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "background running ");
                BigDecimal totalSpending = new BigDecimal("0.00");
                BigDecimal totalIncome = new BigDecimal("0.00");
                List<String> spendingCategories = new ArrayList<>();          // 临时记录已经有的支出分类
                List<String> incomeCategories = new ArrayList<>();            // 临时记录已经有的收入分类
                if (mSpendingStat.size() != 0) {
                    mSpendingStat.clear();
                }
                if (mIncomeStat.size() != 0) {
                    mIncomeStat.clear();
                }
                for (BillRecord billRecord : mMainList) {
                    String categoryName = billRecord.getMajorCategory();
                    int cateImgRes = getApplication()
                            .getSharedPreferences("category_img_res", Context.MODE_PRIVATE)
                            .getInt(categoryName, R.drawable.ic_spending_regular);
                    // 如果为支出
                    if (billRecord.isSpending()) {
                        totalSpending = totalSpending.add(billRecord.getAmountDecimal());
                        // 支出分类的筛选
                        if (spendingCategories.contains(categoryName)) {
                            // 把当前这个billRecord的amount加到已有的stat中
                            int index = spendingCategories.indexOf(categoryName);
                            mSpendingStat.get(index).updateAmount(billRecord.getAmountDecimal());
                        }
                        else {
                            // 新建一个分类，并且把当前这个billRecord的amount加到新建的stat中
                            // 一个分类对应着一个StatisticModel对象 -- 分类名称 , 分类对应的金额大小
                            spendingCategories.add(categoryName);
                            mSpendingStat.add(new StatisticModel(cateImgRes, categoryName, billRecord.getAmount()));
                        }
                    }
                    // 如果是收入
                    else {
                        totalIncome = totalIncome.add(billRecord.getAmountDecimal());
                        if (incomeCategories.contains(categoryName)) {
                            int index = incomeCategories.indexOf(categoryName);
                            mIncomeStat.get(index).updateAmount(billRecord.getAmountDecimal());
                        }
                        else {
                            incomeCategories.add(categoryName);
                            mIncomeStat.add(new StatisticModel(cateImgRes, categoryName, billRecord.getAmount()));
                        }
                    }
                }
                // 排序
                Collections.sort(mSpendingStat);
                Collections.sort(mIncomeStat);
                BigDecimal surplus = totalIncome.subtract(totalSpending);
                // 将数据放入LiveData中
                setSpendingLiveData(totalSpending.toPlainString());
                setIncomeLiveData(totalIncome.toPlainString());
                setSurplusLiveData(surplus.toPlainString());
                // 同时更新设置预算
                setBudgetLiveData(BudgetsManagement.getBudget(mContext, viewModelYear, viewModelMonth));
                mSpendingStatLiveData.postValue(mSpendingStat);
                mIncomeStatLiveData.postValue(mIncomeStat);
                mMainListLiveData.postValue(mMainList);
            }
        }).start();


    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////----------------------------------------分割线----------------------------------------------//////////
///////---------------------------------下面是外部操作数据可相关方法----------------------------------///////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 静态方法插入一条数据
     * @param billRecord 插入的数据
     * @return  返回插入成功的uid
     * @throws ExecutionException **
     * @throws InterruptedException **
     */
    static Long insertBills(BillRecord billRecord) throws ExecutionException, InterruptedException {
        Long uid = new InsertBillsAsyncTask(mDatabaseDao).execute(billRecord).get();
        return uid;
    }


    /**
     * 静态方法删除数据
     * @param billRecord 待删除的记录
     */
    public static void deleteBill(BillRecord billRecord) {
        new DeleteBillAsyncTask(mDatabaseDao).execute(billRecord);
    }

    /**
     * 静态方法更新数据
     * @param billRecord 需要更新的那条数据
     * @return  更新成功的条数
     * @throws ExecutionException **
     * @throws InterruptedException **
     */
    static int updateBills(BillRecord billRecord) throws ExecutionException, InterruptedException {
        return new UpdateBillsAsyncTask(mDatabaseDao).execute(billRecord).get();
    }

    /**
     * 更新主要内容显示列表，这个是外界的主入口，只要外界触发变化的查询的时间，就会在这里统一操作
     *
     * @param year 年份
     * @param month 月份
     */
    public void getMonthlyBills(int year, int month){
        viewModelYear = year;
        viewModelMonth = month;
        List<BillRecord> result = null;
        try{
            result = new GetMonthlyBillsAsyncTask(mDatabaseDao).execute(year, month).get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        // 直接修改内部的list
        setMainListLiveData(result);
        // 同时更新每日收支汇总信息
        try {
            getDailyTotalBills(year, month);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定月份每日数据的汇总，由内部调用
     *
     * @param year 查询的年份
     * @param month 查询的月份
     * @throws ExecutionException **
     * @throws InterruptedException **
     */
    private void getDailyTotalBills(int year, int month) throws ExecutionException, InterruptedException {
        mDailySpending = new GetDailyTotalBillsAsyncTask(mDatabaseDao).execute(year, month, SELECT_SPENDING).get();
        mDailyIncome = new GetDailyTotalBillsAsyncTask(mDatabaseDao).execute(year, month, SELECT_INCOME).get();
        mDailySpendingLiveData.setValue(mDailySpending);
        mDailyIncomeLiveData.setValue(mDailyIncome);
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////----------------------------------------分割线----------------------------------------------//////////
///////---------------------------------下面是数据库异步操作相关类------------------------------------///////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
//////-----------------------Description: AsyncTask的三个泛型参数含义:----------------------------////////////
////---Params: 传进的参数类型, 当在外部手动调用execute(Params params)时传进的参数, 其中的params可以是多个参数---//////
////--------------------------------------Progress: 进度类型-------------------------------------///////////
///---------------------------------------Results: 返回值类型------------------------------------///////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 插入数据异步操作
     */
    private static class InsertBillsAsyncTask extends AsyncTask<BillRecord, Void, Long> {

        private BillDatabaseDao mDao;
        InsertBillsAsyncTask(BillDatabaseDao dao) {
            mDao = dao;
        }

        @Override
        protected Long doInBackground(BillRecord... billRecords) {
            // 返回插入的id
            return mDao.insertBillRecordToDB(billRecords[0]);
        }
    }


    /**
     * 更新数据异步操作
     */
    private static class UpdateBillsAsyncTask extends  AsyncTask<BillRecord,Void, Integer> {

        private BillDatabaseDao mDao;
        UpdateBillsAsyncTask(BillDatabaseDao dao) {
            mDao = dao;
        }

        @Override
        protected Integer doInBackground(BillRecord... billRecords) {
            // 返回更新的id
            return mDao.updateBillRecordInDB(billRecords);
        }
    }


    /**
     * 异步删除数据
     */
    private static class DeleteBillAsyncTask extends AsyncTask<BillRecord, Void, Void> {
        private BillDatabaseDao mDao;
        DeleteBillAsyncTask(BillDatabaseDao dao) {
            mDao = dao;
        }

        @Override
        protected Void doInBackground(BillRecord... billRecords) {
            mDao.deleteBillRecordFromDB(billRecords);
            return null;
        }
    }

    /**
     *
     * Description: 获取一个月的所有数据
     * 传入的参数说明:
     * Integer[0] == 具体要查询的条件参数 年
     * Integer[1] == 具体要查询的条件参数 月
     * Time: 4/17/2020
     */
    private static class GetMonthlyBillsAsyncTask extends AsyncTask<Integer, Integer, List<BillRecord>> {

        private BillDatabaseDao mDao;
        GetMonthlyBillsAsyncTask(BillDatabaseDao dao) {
            mDao = dao;
        }

        @Override
        protected List<BillRecord> doInBackground(Integer... integers) {
            int year = integers[0];
            int month = integers[1];
            SimpleSQLiteQuery query =
                    new SimpleSQLiteQuery("select * from bill_table where record_year = ? and record_month = ? order by record_day desc, uid desc",
                    new Object[]{year, month});
            return mDao.retrieveMonthlyBillsFromDB(query);
        }
    }

    /**
     * Description: 获取当月的每日的支出汇总
     * 参数说明：
     * Integer[0] == 具体要查询的条件参数 年
     * Integer[1] == 具体要查询的条件参数 月
     * Integer[2] == 查询当日的支出还是收入
     * Time: 4/22/2020
    */
    private static class GetDailyTotalBillsAsyncTask extends AsyncTask<Integer, Void, List<Float>> {

        private BillDatabaseDao mDao;
        GetDailyTotalBillsAsyncTask(BillDatabaseDao dao) {
            mDao = dao;
        }

        @Override
        protected List<Float> doInBackground(Integer... integers) {
            int year = integers[0];
            int month = integers[1];
            int flag = integers[2];
            String sqlQuery = "select record_day, sum(amount) from bill_table " +
                    "where record_year = ? and record_month = ? and isSpending = ? " +
                    "group by record_day " +
                    "order by record_day desc";
            SimpleSQLiteQuery query = new SimpleSQLiteQuery(sqlQuery, new Object[] {year, month, flag});
            List<DailyBillRecord> result = mDao.retrieveDailyTotalBillsFromDB(query);
            // 转为数组
            Calendar c = new GregorianCalendar(year, month - 1, 1);
            int maxDayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            List<Float> returnList = new ArrayList<>(maxDayOfMonth);
            float[] dailyAmountArr = new float[maxDayOfMonth];
            for (DailyBillRecord dailyBillRecord : result) {
                dailyAmountArr[dailyBillRecord.getRecordDay() - 1] = Float.parseFloat(dailyBillRecord.getAmount());
            }
            for (float v : dailyAmountArr) {
                returnList.add(v);
            }

            return returnList;
        }
    }
}
