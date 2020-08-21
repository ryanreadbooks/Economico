package com.gang.economico.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.model.AccountModel;
import com.gang.economico.ui.adapters.AccountsAdapter;
import com.gang.economico.viewmodels.AccountsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * 账户管理的界面
 * 用于展示所有账户的收入和支出 同时可以设置账户有多少钱
 * 在这个界面内不支持修改和添加账单数据 只能够查看账户的账单数据
 */
public class AccountsActivity extends AppCompatActivity {

    private static final String TAG = "AccountsActivity";
    private RecyclerView mAccountsRv;
    private TextView mAccountHeaderTitle;
    private Toolbar mToolbar;
    private FloatingActionButton mFAB;

    private AccountsViewModel mViewModel;
    private AccountsAdapter mAdapter;
    private Observer<List<AccountModel>> mObserver;
    private int flag = AccountsViewModel.QUERY_SPENDING;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_accounts_management);
        mViewModel = new ViewModelProvider(this).get(AccountsViewModel.class);
        mAdapter = new AccountsAdapter(this);
        bindView();
        // 初始化数据 默认展示支出数据
        mViewModel.getBillsByAccount(AccountsViewModel.QUERY_SPENDING);

        if (mObserver == null) {
            mObserver = accountModels -> {
                Log.d(TAG, "onResume: " + accountModels);
                mAdapter.setAccountList(accountModels);
                mAdapter.setFlag(flag);
                mAdapter.notifyDataSetChanged();
                // 修改标题
                if (flag == AccountsViewModel.QUERY_SPENDING) {
                    mAccountHeaderTitle.setText("账户支出情况");
                }
                else if (flag == AccountsViewModel.QUERY_INCOME){
                    mAccountHeaderTitle.setText("账户收入情况");
                }
            };
            // 绑定观察者
            mViewModel.getAccountListLiveData().observe(this, mObserver);
        }
    }

    @SuppressLint("PrivateResource")
    private void bindView() {
        mAccountsRv = findViewById(R.id.mine_accounts_rv);
        mAccountsRv.setLayoutManager(new LinearLayoutManager(this));
        mAccountHeaderTitle = findViewById(R.id.mine_accounts_header_title);

        mToolbar = findViewById(R.id.mine_accounts_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("账户管理");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.abc_vector_test);
        }

        mFAB = findViewById(R.id.mine_accounts_add_account);
        mFAB.setOnClickListener(v -> {

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewModel.getAccountListLiveData().getValue() != null) {
            mAdapter.setAccountList(mViewModel.getAccountListLiveData().getValue());
        }
        mAccountsRv.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                supportFinishAfterTransition();
                break;
            case R.id.mine_account_spending:
                flag = AccountsViewModel.QUERY_SPENDING;
                mViewModel.getBillsByAccount(AccountsViewModel.QUERY_SPENDING);
                break;
            case R.id.mine_account_income:
                flag = AccountsViewModel.QUERY_INCOME;
                mViewModel.getBillsByAccount(AccountsViewModel.QUERY_INCOME);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.getAccountListLiveData().removeObserver(mObserver);
        mObserver = null;
        mAdapter = null;
        mAccountsRv.setAdapter(null);
        mAccountsRv.setLayoutManager(null);
        mFAB.setOnClickListener(null);
        mViewModel = null;
    }
}
