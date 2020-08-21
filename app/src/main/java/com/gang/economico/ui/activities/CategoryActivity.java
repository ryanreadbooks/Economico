package com.gang.economico.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.gang.economico.R;
import com.gang.economico.entities.BillRecord;
import com.gang.economico.model.CategoryQueryConditions;
import com.gang.economico.ui.adapters.CategoryPrimaryAdapter;
import com.gang.economico.ui.customs.CustomaryDialog;
import com.gang.economico.ui.customs.YearMonthPickerDialog;
import com.gang.economico.viewmodels.BillViewModel;
import com.gang.economico.viewmodels.CategoryViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private static final String TAG = "CategoryActivity";

    private Toolbar mToolbar;
    private RecyclerView mPrimaryRecyclerview;

    private CategoryViewModel mCategoryViewModel;
    private TextView mTimeTv;
    private TextView mCateNameTv;
    private TextView mNumTv;
    private TextView mAmountTv;
    private ImageView mCateIv;

    private CategoryPrimaryAdapter mPrimaryAdapter;
    private CategoryQueryConditions mCateCondition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("cate_data");
        assert bundle != null;
        // 从上一个Activity来的数据 当作界面的初始数据来源
        mCateCondition = bundle.getParcelable("cate_to_display");
        mCategoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        // adapters初始化
        mPrimaryAdapter = new CategoryPrimaryAdapter();

        bindView();

        // 主数据绑定观察者
        mCategoryViewModel.getCateListLiveData().observe(this, new Observer<List<List<BillRecord>>>() {
            @Override
            public void onChanged(List<List<BillRecord>> lists) {
                mPrimaryAdapter.setPrimaryLists(lists);
                mPrimaryAdapter.notifyDataSetChanged();
            }
        });
        // 初次查询数据
        mCategoryViewModel.getCategorizedData(mCateCondition);

        // 分类总金额观察者
        mCategoryViewModel.getCateAmountLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String totalAmount = "￥" + s;
                mAmountTv.setText(totalAmount);
            }
        });

        // 数据条数观察者
        mCategoryViewModel.getCateCountLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String numString = "共" + s + "条数据";
                mNumTv.setText(numString);
            }
        });
    }


    /**
     * 控件绑定
     */
    @SuppressLint("PrivateResource")
    private void bindView() {
        mToolbar = findViewById(R.id.category_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("分类报表");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.abc_vector_test);
        }

        mTimeTv = findViewById(R.id.category_time);
        String dateString = mCateCondition.getYear() + "年" + mCateCondition.getMonth() + "月";
        mTimeTv.setText(dateString);

        mCateNameTv = findViewById(R.id.category_major_cate);
        mCateNameTv.setText(mCateCondition.getName());

        mNumTv = findViewById(R.id.category_num);
        mAmountTv = findViewById(R.id.category_cate_amount);
        String totalAmount = "￥" + mCateCondition.getTotalAmount();
        mAmountTv.setText(totalAmount);

        mCateIv = findViewById(R.id.category_cate_img);
        mCateIv.setImageResource(mCateCondition.getImgRes());

        mPrimaryRecyclerview = findViewById(R.id.category_primary_list);
        mPrimaryRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mPrimaryRecyclerview.setAdapter(mPrimaryAdapter);
        // 内层RecyclerView点击事件监听 点击某一条事件可以进入修改
        mPrimaryAdapter.setOnInnerItemClickListener(billRecord -> {
            Log.d(TAG, "bindView: " + billRecord);
            // 把这个传递到EditActivity中
            Intent intent = new Intent(this, EditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("updating_bill", billRecord);
            intent.putExtra("request_operation", MainActivity.UPDATE_BILL);
            intent.putExtra("updating_data", bundle);
            startActivityForResult(intent, MainActivity.UPDATE_BILL);
        });

        // 内层RecyclerView长按事件监听 长按某一条事件可以删除该条消息
        mPrimaryAdapter.setOnInnerLongClickListener(billRecord -> {

            CustomaryDialog.Builder builder = new CustomaryDialog.Builder(this);
            builder.setIcon(0)
                    .setTitle("确认删除?")
                    .setContent("是否确认删除该条目, 删除后不可恢复")
                    .setCancel("取消", Dialog::dismiss)
                    .setConfirm("确认删除", dialog -> {
                        BillViewModel.deleteBill(billRecord);
                        // 更新列表
                        mCategoryViewModel.getCategorizedData(mCateCondition);
                        Intent data = new Intent();
                        data.putExtra("new_bill_year", mCateCondition.getYear());
                        data.putExtra("new_bill_month", mCateCondition.getMonth());
                        setResult(MainActivity.HAS_BILL_DELETED_IN_CATEGORY_ACTIVITY, data);
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 从编辑页面返回的结果
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: category activity requestCode " + requestCode);
        Log.d(TAG, "onActivityResult: category activity resultCode " + resultCode);
        // 在EditActivity中修改了选中的信息
        if (requestCode == MainActivity.UPDATE_BILL && resultCode == MainActivity.UPDATE_BILL_SUCCESS) {
            // 更新页面
            int newBillYear = data.getIntExtra("new_bill_year", Calendar.getInstance().get(Calendar.YEAR));
            int newBillMonth = data.getIntExtra("new_bill_month", (Calendar.getInstance().get(Calendar.MONTH) + 1));
            mCateCondition.setYear(newBillYear);
            mCateCondition.setMonth(newBillMonth);
            mCategoryViewModel.getCategorizedData(mCateCondition);
            // 同时设置返回MainActivity时的是否由数据更新标记，以便MainActivity重新刷新数据
            setResult(MainActivity.BILL_CHANGED_IN_CATEGORY_ACTIVITY, data);
        }
    }

    /**
     * 菜单注入
     * @param menu 菜单
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_info, menu);
        return true;
    }

    /**
     * 为toolbar上的按钮设置点击事件
     * @param item 菜单上面的每一个item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                supportFinishAfterTransition();
                break;
            case R.id.calendar_switch:
                // 日历选择
                List<String> yearList = new ArrayList<>();
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int gap = currentYear - 2015;
                for (int i = gap; i >= 0;  i--) {
                    // 动态设置可以供用户设置查询的年份，最远可以设置到2015年一月一日
                    yearList.add((2015 + i) + "");
                }
                YearMonthPickerDialog.Builder builder = new YearMonthPickerDialog.Builder(this);
                YearMonthPickerDialog picker = builder.setYearTab(yearList).create();
                builder.setOnYearMonthSetListener((year, month) -> {
                    Log.d(TAG, "onYearMonthSet: year: " + year + " month: " + month);
                    // 判断是不是未来的事件
                    int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
                    if (currentMonth < month && currentYear == Integer.parseInt(year)){
                        Snackbar.make(mToolbar, "未来的账目还没有", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                    else {
                        // 选择的事件条件满足
                        Log.d(TAG, "onYearMonthSet: year: " + year + " month: " + month);
                        // 选择了查看的日期，重新在数据库中查询数据并显示
                        // 更新查询条件
                        mCateCondition.setYear(Integer.parseInt(year));
                        mCateCondition.setMonth(month);
                        String newDateString = year + "年" + month + "月";
                        mTimeTv.setText(newDateString);
                        mCategoryViewModel.getCategorizedData(mCateCondition);
                        picker.dismiss();
                    }
                });
                picker.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        mPrimaryRecyclerview.setAdapter(null);
        mPrimaryRecyclerview.setLayoutManager(null);
        mPrimaryAdapter.clearData();
        mPrimaryAdapter = null;
    }
}
