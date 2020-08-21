package com.gang.economico.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.gang.economico.R;
import com.gang.economico.ui.fragments.IncomeFragment;
import com.gang.economico.ui.fragments.MineFragment;
import com.gang.economico.ui.fragments.SpendingFragment;
import com.gang.economico.ui.fragments.OverviewFragment;
import com.gang.economico.ui.customs.YearMonthPickerDialog;
import com.gang.economico.viewmodels.BillViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
* Description: 主界面Activity类
*/
public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";
    public static final int MAX_YEAR_TO_SET = 2015;
    public static final int DO_NADA = 0;
    public static final int INSERT_BILL = 1;
    public static final int UPDATE_BILL = 2;
    public static final int INSERT_BILL_SUCCESS = 3;
    public static final int UPDATE_BILL_SUCCESS = 4;
    public static final int REQUEST_DISPLAY_CATEGORY = 5;
    public static final int BILL_CHANGED_IN_CATEGORY_ACTIVITY = 6;
    public static final int HAS_BILL_DELETED_IN_CATEGORY_ACTIVITY = 7;
    public static final String[] TAB_TITLE = {"总览", "支出", "收入", "我的"};

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private FloatingActionButton mEditBtn;
    private List<Fragment>  mFragmentList;

    private BillViewModel mBillViewModel;

    private float btnX1;
    private float btnY1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBillViewModel = new ViewModelProvider(this).get(BillViewModel.class);
        initActivity();
        initFragment();
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getItemCount() {
                return 4;
            }
        });
        // TabLayout和ViewPager2相关联
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(TAB_TITLE[position]);
            }
        });
        tabLayoutMediator.attach();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode " + requestCode);
        Log.d(TAG, "onActivityResult: resultCode " + resultCode);
        // 插入新信息成功 或者 更新信息成功
        if ((requestCode == INSERT_BILL && resultCode == INSERT_BILL_SUCCESS) 
                || (requestCode == UPDATE_BILL && resultCode == UPDATE_BILL_SUCCESS)
                || (requestCode == REQUEST_DISPLAY_CATEGORY && resultCode == BILL_CHANGED_IN_CATEGORY_ACTIVITY)
                || (resultCode == HAS_BILL_DELETED_IN_CATEGORY_ACTIVITY)) {
            Log.d(TAG, "onActivityResult: needs update view");
            assert data != null;
            int newBillYear = data.getIntExtra("new_bill_year", Calendar.getInstance().get(Calendar.YEAR));
            int newBillMonth = data.getIntExtra("new_bill_month", (Calendar.getInstance().get(Calendar.MONTH) + 1));
            // 重新加载数据
            mBillViewModel.getMonthlyBills(newBillYear, newBillMonth);
        }
    }

    // 初始化主界面，绑定一些控件
    @SuppressLint("ClickableViewAccessibility")
    private void initActivity() {
        mTabLayout = findViewById(R.id.main_tab);
        mViewPager = findViewById(R.id.main_viewpager);
        mEditBtn = findViewById(R.id.to_edit_view_btn);
        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                // 点击加号的作用就是插入数据，将请求发送给EditActivity
                intent.putExtra("request_operation", INSERT_BILL);
                startActivityForResult(intent, INSERT_BILL);
            }
        });
        // 两个滑动手势的监听，FAB按钮上划选择展示的日期
        // FAB上划弹出时间选择框
        mEditBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnX1 = event.getX();
                        btnY1 = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float btnX2 = event.getX();
                        float btnY2 = event.getY();
                        // 用户上划或者左划弹出Dialog选择日期
                        if ((btnY1 - btnY2) > 40 || (btnX1 - btnX2) > 40) {
                            List<String> yearList = new ArrayList<>();
                            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            int gap = currentYear - MAX_YEAR_TO_SET;
                            for (int i = gap; i >= 0;  i--) {
                                // 动态设置可以供用户设置查询的年份，最远可以设置到2015年一月一日
                                yearList.add((MAX_YEAR_TO_SET + i) + "");
                            }
                            // 通过Builder创建Dialog
                            YearMonthPickerDialog.Builder builder = new YearMonthPickerDialog.Builder(MainActivity.this);
                            final YearMonthPickerDialog picker = builder.setYearTab(yearList).create();
                            builder.setOnYearMonthSetListener(new YearMonthPickerDialog.Builder.OnYearMonthSetListener() {
                                @Override
                                public void onYearMonthSet(String year, int month) {
                                    Log.d(TAG, "onYearMonthSet: year: " + year + " month: " + month);
                                    // 判断是不是未来的事件
                                    int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
                                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                    if (currentMonth < month && currentYear == Integer.parseInt(year)){
                                        Snackbar.make(mEditBtn, "未来的账目还没有", BaseTransientBottomBar.LENGTH_SHORT).show();
                                    }
                                    else {
                                        // 选择的事件条件满足
                                        Log.d(TAG, "onYearMonthSet: year: " + year + " month: " + month);
                                        // 选择了查看的日期，重新在数据库中查询数据并显示
                                        mBillViewModel.getMonthlyBills(Integer.parseInt(year), month);
                                        picker.dismiss();
                                    }
                                }
                            });
                            picker.show();
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    // 初始化主界面中的4个分页面
    // 我的设置Fragment找另外一个方式实现，不用Fragment
    private void initFragment() {
        mFragmentList = new ArrayList<>();
        mFragmentList.add(new OverviewFragment());
        mFragmentList.add(new SpendingFragment());
        mFragmentList.add(new IncomeFragment());
        mFragmentList.add(new MineFragment());
    }
}
