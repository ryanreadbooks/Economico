package com.gang.economico.viewmodels;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.gang.economico.databases.BillDatabase;
import com.gang.economico.databases.BillDatabaseDao;
import com.gang.economico.model.AccountModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Account 页面的ViewModel
 */
public class AccountsViewModel extends AndroidViewModel {

    static final String TAG = "AccountsViewModel";
    public static final int QUERY_SPENDING = 1;
    public static final int QUERY_INCOME = 0;

    private static BillDatabase mBillDatabase;
    private static BillDatabaseDao mDao;
    private int mFlag;
    private List<AccountModel> mAccountList;
    private MutableLiveData<List<AccountModel>> mAccountListLiveData;

    // 初始化
    public AccountsViewModel(@NonNull Application application) {
        super(application);
        // 获得数据库引用
        mBillDatabase = BillDatabase.getBillDatabase(application.getApplicationContext());
        mDao = mBillDatabase.getBillDatabaseDao();
        // 默认查询支出数据
        mFlag = QUERY_SPENDING;
        mAccountList = new ArrayList<>();
        mAccountListLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<AccountModel>> getAccountListLiveData() {
        return mAccountListLiveData;
    }

    /**
     * 对外的查询方法，统一的入口
     * @param flag 查询条件
     */
    public void getBillsByAccount(int flag) {
        mFlag = flag;
        List<AccountModel> result = null;
        try {
            result = new GetBillsByAccountsAsyncTask(mDao).execute(flag).get();
            Log.d(TAG, "getBillsByAccount: result " + result);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (result != null) {
            mAccountList = result;
            mAccountListLiveData.setValue(mAccountList);
        }
    }

}


/**
 * 异步查询数据库中各个账户的数据
 */
class GetBillsByAccountsAsyncTask extends AsyncTask<Integer, Void, List<AccountModel>> {
    private static BillDatabaseDao mDao;
    GetBillsByAccountsAsyncTask(BillDatabaseDao dao) {
        mDao = dao;
    }

    /**
     * @param integers 查询收入还是支出
     * @return 查询结果
     */
    @Override
    protected List<AccountModel> doInBackground(Integer... integers) {
        int flag = integers[0];
        Log.d(AccountsViewModel.TAG, "doInBackground: flag " + flag);
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "select payment_method, sum(amount) as total_amount from bill_table " +
                        "where isSpending = ? " +
                        "group by payment_method " +
                        "order by total_amount desc",
                new Object[] {flag});

        return mDao.retrieveAccountsFromDB(query);
    }
}
