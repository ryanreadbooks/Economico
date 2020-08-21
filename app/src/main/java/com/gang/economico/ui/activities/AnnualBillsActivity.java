package com.gang.economico.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.DashPathEffect;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gang.economico.R;
import com.gang.economico.entities.CategoryModel;
import com.gang.economico.ui.customs.ListDialog;
import com.gang.economico.ui.fragments.MineAnnualCategoryFragment;
import com.gang.economico.ui.fragments.MineAnnualOverviewFragment;
import com.gang.economico.viewmodels.AccountsViewModel;
import com.gang.economico.viewmodels.YearlyViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.gang.economico.ui.activities.MainActivity.MAX_YEAR_TO_SET;

public class AnnualBillsActivity extends AppCompatActivity {

    private static final String TAG = "AnnualBillsActivity";
    private Toolbar mToolbar;
    private TextView mYearTv;
    private TextView mTitleTv;
    private TextView mAmountTv;
    private ViewPager2 mViewPager;
    private FloatingActionButton mYearSelectionBtn;

    private YearlyViewModel mViewModel;

    private int showSpending = AccountsViewModel.QUERY_SPENDING;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_annual_bills);
        mViewModel = new ViewModelProvider(this).get(YearlyViewModel.class);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        bindView();
        // 注册在Activity层面的观察者
        // 总金额
        mViewModel.getTotalAmountLiveData().observe(this, s -> mAmountTv.setText(s));
        // 查询的年
        mViewModel.getYearLiveData().observe(this, integer -> {
            String year = integer + "年";
            mYearTv.setText(year);
        });
    }

    @SuppressLint("PrivateResource")
    private void bindView() {
        mToolbar = findViewById(R.id.mine_annual_bill_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.abc_vector_test);
            actionBar.setTitle("年度数据统计");
        }

        mYearTv = findViewById(R.id.mine_annual_year);
        mTitleTv = findViewById(R.id.mine_annual_title);
        mAmountTv = findViewById(R.id.mine_annual_amount);
        mViewPager = findViewById(R.id.mine_annual_bill_viewpager);
        mViewPager.setOffscreenPageLimit(1);
        // 绑定适配器 这里用匿名类 如果有其它需求也可以自定义一个类
        // 这里比较简单 只有两个页面需要切换
        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? new MineAnnualOverviewFragment() : new MineAnnualCategoryFragment();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });
        // 年份选择按钮
        mYearSelectionBtn = findViewById(R.id.mine_annual_year_selection);
        // 弹出对话框用于选择年份
        mYearSelectionBtn.setOnClickListener(v -> {
            // 可供选择的年份
            List<CategoryModel> yearList = new ArrayList<>();
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int gap = currentYear - MAX_YEAR_TO_SET;
            for (int i = gap; i >= 0;  i--) {
                // 动态设置可以供用户设置查询的年份，最远可以设置到2015年
                yearList.add(new CategoryModel("", String.valueOf(MAX_YEAR_TO_SET + i), false));
            }

            ListDialog.Builder builder = new ListDialog.Builder(this);
            ListDialog listDialog = builder.create();
            builder.setItems(yearList)
                    .setTitle("选择需要查看的年份")
                    .setListItemClickListener(new ListDialog.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(View view, int position) {
                            // 设置viewModel中的选择条件
                            mViewModel.setYearLiveData(Integer.parseInt(yearList.get(position).getCategoryName()));
                            mViewModel.loadOverviewData();
                            mViewModel.loadCategorizedData();
                            yearList.clear();
                            listDialog.dismiss();
                        }
                    });
            listDialog.show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                supportFinishAfterTransition();
                break;
            case R.id.mine_account_spending:
                // 切换至查询支出
                if (showSpending == AccountsViewModel.QUERY_INCOME) {
                    mViewModel.setQueryCondition(AccountsViewModel.QUERY_SPENDING);
                    mViewModel.loadOverviewData();
                    mViewModel.loadCategorizedData();
                    showSpending = AccountsViewModel.QUERY_SPENDING;
                    mTitleTv.setText("本年度总支出");
                }
                break;
            case R.id.mine_account_income:
                // 切换至查询收入
                if (showSpending == AccountsViewModel.QUERY_SPENDING) {
                    mViewModel.setQueryCondition(AccountsViewModel.QUERY_INCOME);
                    mViewModel.loadOverviewData();
                    mViewModel.loadCategorizedData();
                    showSpending = AccountsViewModel.QUERY_INCOME;
                    mTitleTv.setText("本年度总收入");
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account_activity, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel = null;
        mViewPager.setAdapter(null);
        Log.d(TAG, "onDestroy: " + isFinishing());
    }
}
