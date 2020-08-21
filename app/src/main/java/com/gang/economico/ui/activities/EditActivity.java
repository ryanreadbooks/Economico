package com.gang.economico.ui.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gang.economico.R;
import com.gang.economico.entities.CategoryModel;
import com.gang.economico.entities.BillRecord;
import com.gang.economico.ui.customs.ListDialog;
import com.gang.economico.ui.fragments.EditIncomeCategoryFragment;
import com.gang.economico.ui.fragments.EditSpendingCategoryFragment;
import com.gang.economico.ui.customs.CommentDialog;
import com.gang.economico.viewmodels.EditViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class EditActivity extends AppCompatActivity{

    public static final String TAG = "Edit Activity";

    private Toolbar mToolbar;
    private ViewPager2 mEditViewPager;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private TextView mInputTextView;
    private ImageView mSelectedPayment;
    private TextView mEditComment;

    // 现在记录的是支出还是收入，true -> 支出, false -> 收入
    private boolean isSpending = true;
    private String mCommentStr = "";
    private List<CategoryModel> mPaymentList;

    private TextView mDatePicker;
    private TextView mNumConfirm;

    public EditViewModel mEditViewModel;
    // 请求码
    private int resCode;
    // 数据更新的时候维护一个对象
    private BillRecord mUpdatingBill;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        bindView();
        mEditViewModel = new ViewModelProvider(this).get(EditViewModel.class);

        // 支出和收入图表转换viewpager设置
        mFragmentList.add(new EditSpendingCategoryFragment());
        mFragmentList.add(new EditIncomeCategoryFragment());
        mEditViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });
        mEditViewPager.setOffscreenPageLimit(1);
        // 给viewpager的滑动添加监听器
        mEditViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // 页面选中之后的操作
                if (position == 0 && !isSpending) {
                    isSpending = true;
                    mEditViewModel.setSelectedCategoryLiveData(R.drawable.ic_spending_regular, "普通");
                    Log.d(TAG, "onPageSelected: 0");
                }
                else if (position == 1 && isSpending) {
                    isSpending = false;
                    mEditViewModel.setSelectedCategoryLiveData(R.drawable.ic_income_salary, "工资");
                    Log.d(TAG, "onPageSelected: 1");
                }
                // 调用invalidateOptionsMenu()重新加载menu
                // 即回调onPrepareOptionsMenu()方法
                invalidateOptionsMenu();
            }
        });

        // 键盘输入的数字在ViewModel中管理，并通过LiveData来实现更新
        // 键盘输入处理ViewModel
        mEditViewModel.getDisplayBufferLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String str) {
                String inputStr = "￥" + mEditViewModel.getDisplayBufferLiveData().getValue();
                mInputTextView.setText(inputStr);
            }
        });
        // 选择分类发生变化
        mEditViewModel.getSelectedCategoryLiveData().observe(this, new Observer<CategoryModel>() {
            @Override
            public void onChanged(CategoryModel categoryModel) {
                ImageView selectedCategory = findViewById(R.id.edit_selected_category);
                selectedCategory.setImageResource(categoryModel.getImgResInt());
                ObjectAnimator animator = ObjectAnimator.ofFloat(selectedCategory,
                        "rotation",
                        0, -15, 15, 0)
                        .setDuration(500);
                animator.start();
            }
        });
        // 支付方式发生变化
        mEditViewModel.getSelectedPaymentLiveData().observe(this, new Observer<CategoryModel>() {
            @Override
            public void onChanged(CategoryModel categoryModel) {
                mSelectedPayment.setImageResource(categoryModel.getImgResInt());
            }
        });
        try {
            generatePaymentListData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 选择的日期发生变化
        mEditViewModel.getSelectedDateLiveData().observe(this, new Observer<Calendar>() {
            @Override
            public void onChanged(Calendar calendar) {
                String textDate = "";
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                if (year == new GregorianCalendar().get(Calendar.YEAR)) {
                    textDate = month + "-" + day;
                }
                else {
                    textDate = year + "-" +  month + "-" + day;
                }
                mDatePicker.setText(textDate);
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        // 请求码
        resCode = getIntent().getIntExtra("request_operation", -1);
        if (resCode == MainActivity.UPDATE_BILL) {
            Bundle bundle = getIntent().getBundleExtra("updating_data");
            assert bundle != null;
            mUpdatingBill = bundle.getParcelable("updating_bill");
            // 展示原来的数据
            String categoryName = mUpdatingBill.getMajorCategory();
            // 收入还是支出的设置
            mEditViewPager.setCurrentItem(mUpdatingBill.isSpending() ? 0 : 1);
            // 选中分类图标设置
            Log.d(TAG, "onCreate: " + getSharedPreferences("category_img_res", MODE_PRIVATE).getInt(categoryName, R.drawable.ic_spending_regular));
            mEditViewModel.setSelectedCategoryLiveData(getSharedPreferences("category_img_res", MODE_PRIVATE)
                    .getInt(categoryName, R.drawable.ic_spending_regular), categoryName);
            // 设置金额
            mEditViewModel.setDisplayBufferLiveData(mUpdatingBill.getAmount());
            // 设置备注
            if (mUpdatingBill.getComment().equals("")) {
                mCommentStr = "";
                mEditComment.setText(R.string.comment_here);
            }
            else {
                mCommentStr = mUpdatingBill.getComment();
                mEditComment.setText(mCommentStr);
            }
            // 设置支付方式
            int imgRes = getSharedPreferences("category_img_res", MODE_PRIVATE).getInt(mUpdatingBill.getPaymentMethod(), R.drawable.ic_alipay);
            mEditViewModel.setSelectedPaymentLiveData(imgRes + "", mUpdatingBill.getPaymentMethod());
            // 设置日期
            Calendar calendar = new GregorianCalendar(mUpdatingBill.getRecordYear(), mUpdatingBill.getRecordMonth() - 1, mUpdatingBill.getRecordDay());
            mEditViewModel.setSelectedDateLiveData(calendar);
        }
    }

    // 控件绑定
    @SuppressLint("PrivateResource")
    private void bindView() {
        // ToolBar功能设置
        mToolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("记一笔");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.abc_vector_test);
        }

        mEditViewPager = findViewById(R.id.edit_is_spending_vp);
        mInputTextView = findViewById(R.id.edit_input_string);
        mSelectedPayment = findViewById(R.id.edit_selected_payment);
        // 选择账户
        mSelectedPayment.setOnClickListener(v -> {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mSelectedPayment,
                    "rotation",
                    0, -15, 15, 0);
            animator.start();

            ListDialog.Builder builder = new ListDialog.Builder(EditActivity.this);
            ListDialog listDialog = builder.setItems(mPaymentList).create();
            builder.setTitle("选择账户")
                    .setListItemClickListener((view, position) -> {
                        mEditViewModel.setSelectedPaymentLiveData(mPaymentList.get(position).getImgRes(),
                                mPaymentList.get(position).getCategoryName());
                        listDialog.dismiss();
                    });
            listDialog.show();
        });

        mDatePicker = findViewById(R.id.edit_date_picker);
        mNumConfirm = findViewById(R.id.edit_num_confirm);

        mEditComment = findViewById(R.id.edit_comments);
        mEditComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出Dialog添加备注
                CommentDialog.Builder builder = new CommentDialog.Builder(EditActivity.this);
                builder.setIcon(R.drawable.ic_comment)
                        .setEditMaxLength(25)
                        .setTitle("添加备注")
                        .setConfirm("确认", new CommentDialog.Builder.OnConfirmClickListener() {
                            @Override
                            public void onConfirmClick(EditText editText, String confirmStr) {
                                if (TextUtils.isEmpty(editText.getText())) {
                                    mCommentStr = "";
                                    mEditComment.setText(R.string.comment_here);
                                }
                                else {
                                    mCommentStr = editText.getText().toString();
                                    mEditComment.setText(mCommentStr);
                                }
                            }
                        })
                        .setCancel("取消", new CommentDialog.Builder.OnCancelClickListener() {
                            @Override
                            public void onCancelClick(String cancelStr) {
                            }
                        })
                        .setDefaultText(mCommentStr)
                        .create()
                        .show();
            }
        });
    }

    /**
     * Description: ToolBar上的按键的处理
    */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 这里不应该启动新的Activity,而是调用系统的返回键返回前一页
                onBackPressed();
                supportFinishAfterTransition();
                break;
            case R.id.edit_category_income:
            case R.id.edit_category_spending:
                changeCategory();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Description: ToolBar上的菜单注入，只在创建Activity时候调用一次
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_switch_category_layout, menu);
        return true;
    }

    /**
     * Description: 处理菜单项的点击，以实现菜单图标的动态变化, 通过设置是否可见来实现单个图标的变化
    */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mEditViewPager.getCurrentItem() == 0) {
            menu.findItem(R.id.edit_category_income).setVisible(false);
            menu.findItem(R.id.edit_category_spending).setVisible(true);
        }else if (mEditViewPager.getCurrentItem() == 1) {
            menu.findItem(R.id.edit_category_income).setVisible(true);
            menu.findItem(R.id.edit_category_spending).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void generatePaymentListData() throws IOException {
        // 从文件中读取账户信息
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = openFileInput("payment_list");
            ois = new ObjectInputStream(fis);
            mPaymentList = (ArrayList<CategoryModel>) ois.readObject();
            Log.d(TAG, "generatePaymentListData: " + mPaymentList);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            if (fis != null) {
                fis.close();
            }
            if (ois != null) {
                ois.close();
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        // 此处测试用，应该使用在数据库的数据动态添加
//        mPaymentList = new ArrayList<>();
//        mPaymentList.add(new CategoryModel(R.drawable.ic_alipay + "", "支付宝", false));
//        mPaymentList.add(new CategoryModel(R.drawable.ic_wechat + "", "微信", false));
//        mPaymentList.add(new CategoryModel(R.drawable.ic_cash + "", "现金", false));
//        mPaymentList.add(new CategoryModel(R.drawable.ic_chinese_bank + "", "中国银行", false));
//        mPaymentList.add(new CategoryModel(R.drawable.ic_cbc + "", "建设银行", false));
    }

    private void changeCategory() {
        if (isSpending) {
            isSpending = false;
            // 用viewpager实现页面切换
            mEditViewPager.setCurrentItem(1, true);
            mEditViewModel.setSelectedCategoryLiveData(R.drawable.ic_income_salary, "工资");
        }
        else {
            // 如果当前界面是income，则切换至spending
            isSpending = true;
            mEditViewPager.setCurrentItem(0, true);
            mEditViewModel.setSelectedCategoryLiveData(R.drawable.ic_spending_regular, "普通");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        supportFinishAfterTransition();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: edit activity destroyed");
        mPaymentList.clear();
        mPaymentList = null;
        mFragmentList.clear();
        mFragmentList = null;
        mEditViewModel = null;
        mUpdatingBill = null;
        mEditViewPager.setAdapter(null);
    }

    /**
     * Description: 数字键盘输入处理
     * 在activity_edit中的键盘的每个按键的style中设置了使用onClick
     */
    public void keyboardClick(View v) {
        switch (v.getId()) {
            case R.id.edit_num_0:
                mEditViewModel.setInputBuffer('0');
                break;
            case R.id.edit_num_1:
                mEditViewModel.setInputBuffer('1');
                break;
            case R.id.edit_num_2:
                mEditViewModel.setInputBuffer('2');
                break;
            case R.id.edit_num_3:
                mEditViewModel.setInputBuffer('3');
                break;
            case R.id.edit_num_4:
                mEditViewModel.setInputBuffer('4');
                break;
            case R.id.edit_num_5:
                mEditViewModel.setInputBuffer('5');
                break;
            case R.id.edit_num_6:
                mEditViewModel.setInputBuffer('6');
                break;
            case R.id.edit_num_7:
                mEditViewModel.setInputBuffer('7');
                break;
            case R.id.edit_num_8:
                mEditViewModel.setInputBuffer('8');
                break;
            case R.id.edit_num_9:
                mEditViewModel.setInputBuffer('9');
                break;
            case R.id.edit_num_dot:
                mEditViewModel.setInputBuffer('.');
                break;
            case R.id.edit_num_plus:
                Toast.makeText(EditActivity.this, "Not implemented! stay tuned", Toast.LENGTH_LONG).show();
                break;
            case R.id.edit_date_picker:
                datePickerOperation();
                break;
            case R.id.edit_num_confirm:
                confirmOperation();
                break;
            case R.id.edit_num_c:
                mEditViewModel.clearInputBuffer();
                break;
            case R.id.edit_num_delete:
                mEditViewModel.backSpace();
                break;
            default:
                break;
        }
    }

    /**
     * 按下确认键的处理
     */
    private void confirmOperation() {
        if ("0.00".equals(mEditViewModel.getDisplayBufferLiveData().getValue())) {
            Toast.makeText(EditActivity.this, "金额不能为0", Toast.LENGTH_LONG).show();
            return;
        }
        boolean isSuccess = mEditViewModel.triggerConfirm(mCommentStr, isSpending, "日常账本", resCode, mUpdatingBill);
        // 操作成功
        if (isSuccess) {
            if (mEditViewModel.getEditState() == EditViewModel.HAS_EDIT_OPERATION) {
                // 如果MainActivity要求EditActivity协助处理插入新数据
                Intent resultIntent = new Intent();
                int newBillYear = mEditViewModel.getSelectedDateLiveData().getValue().get(Calendar.YEAR);
                int newBillMonth = mEditViewModel.getSelectedDateLiveData().getValue().get(Calendar.MONTH) + 1;

                resultIntent.putExtra("new_bill_year", newBillYear);
                resultIntent.putExtra("new_bill_month", newBillMonth);
                if (resCode == MainActivity.INSERT_BILL) {
                    setResult(MainActivity.INSERT_BILL_SUCCESS, resultIntent);
                }
                // 如果MainActivity要求EditActivity协助处理更新一条数据
                else if (resCode == MainActivity.UPDATE_BILL) {
                    setResult(MainActivity.UPDATE_BILL_SUCCESS, resultIntent);
                }
            }
        }
        else {
            Log.d(TAG, "confirm button clicked but operation failed");
        }
        String tips;
        if (isSuccess) {
            tips = "保存成功";
        }
        else {
            tips = "保存失败";
        }
        Snackbar.make(mNumConfirm, tips, BaseTransientBottomBar.LENGTH_LONG).show();
    }

    /**
     * 按下日期选择按钮的处理
     */
    private void datePickerOperation() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(EditActivity.this, R.style.CommentDialogTheme);
        // 设置日期可选择范围 [2015-1-1, 今天]
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getWindow().setWindowAnimations(R.style.CommentDialogAnim);
        datePickerDialog.getDatePicker().setMinDate(new GregorianCalendar(2015, 0, 1).getTimeInMillis());
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mEditViewModel.setSelectedDateLiveData(new GregorianCalendar(year, month, dayOfMonth));
            }
        });
        Calendar sc = mEditViewModel.getSelectedDateLiveData().getValue();
        assert sc != null;
        int year = sc.get(Calendar.YEAR);
        int month = sc.get(Calendar.MONTH);
        int day = sc.get(Calendar.DAY_OF_MONTH);
        datePickerDialog.getDatePicker().updateDate(year, month, day);
        datePickerDialog.show();
    }
}
