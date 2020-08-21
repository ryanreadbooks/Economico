package com.gang.economico.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gang.economico.R;
import com.gang.economico.settings.BudgetsManagement;
import com.gang.economico.entities.Budget;
import com.gang.economico.model.MineSetting;
import com.gang.economico.ui.activities.AccountsActivity;
import com.gang.economico.ui.activities.AnnualBillsActivity;
import com.gang.economico.ui.activities.CategorySettingActivity;
import com.gang.economico.ui.adapters.MineSettingAdapter;
import com.gang.economico.ui.customs.CommentDialog;
import com.gang.economico.viewmodels.BillViewModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class MineFragment extends Fragment {

    private static final String TAG = "MineFragment";
    private static final int BUDGET_SETTING = 0;
    private static final int CATEGORY_SETTING = 1;
    private static final int VIEW_ANNUAL_BILLS = 2;
    private static final int ACCOUNTS_MANAGEMENT = 3;
    private static final int EXPORT_DATA = 4;
    private static final int IMPORT_DATA = 5;
    private static final int SYSTEM_SETTING = 6;

    private View mView;
    private RecyclerView mSettingRv;
    private List<MineSetting> mStrategiesList;
    private MineSettingAdapter mSettingAdapter;
    private BillViewModel mBillViewModel;

    private boolean isFirstLoad = true;

    public MineFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingAdapter = new MineSettingAdapter();
        mStrategiesList = new ArrayList<>();

        mBillViewModel = new ViewModelProvider(requireActivity()).get(BillViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        mView = inflater.inflate(R.layout.fragment_mine, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        setSettingStrategies();
        bindView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    private void bindView() {
        mSettingRv = mView.findViewById(R.id.mine_recycler_view);
        mSettingRv.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL,false));

        // 适配器设置
        mSettingAdapter.setSettingModels(mStrategiesList);
        mSettingAdapter.setOnItemClickListener((pos, settingName) -> {
            switch (pos) {
                case BUDGET_SETTING:
                    // 使用Dialog设置预算
                    budgetSetting();
                    break;
                case CATEGORY_SETTING:
                    categorySetting();
                    break;
                case VIEW_ANNUAL_BILLS:
                    // 弹出一个新页面展示年度数据
                    viewAnnualBills();
                    Log.d(TAG, "bindView: view annual bills");
                    break;
                case ACCOUNTS_MANAGEMENT:
                    accountsManagement();
                    break;
                case EXPORT_DATA:
                    Toast.makeText(requireActivity(), "stay tuned for the export function", Toast.LENGTH_SHORT).show();
                    break;
                case IMPORT_DATA:
                    Toast.makeText(requireActivity(), "stay tuned for the import function", Toast.LENGTH_SHORT).show();
                    break;
                case SYSTEM_SETTING:
                    systemSetting();
                    break;
            }
        });
        mSettingRv.setAdapter(mSettingAdapter);
    }

    /**
     * 弹出对话框并且设置预算
     */
    private void budgetSetting() {
        CommentDialog.Builder builder = new CommentDialog.Builder(requireContext());
        int year = mBillViewModel.getViewModelYear();
        int month = mBillViewModel.getViewModelMonth();
        String title = "设置" + year + "年" + month + "月预算";
        String alreadyAmount;
        // 如果该月没有设置预算 则输入框内不显示默认数值
        // final boolean hasBudget = !"not set".equals(alreadyAmount);
        boolean hasBudget;
        Budget alreadyBudget = BudgetsManagement.getBudget2(requireContext(), year, month);
        if (alreadyBudget == null) {
            // 没有设置预算
            hasBudget = false;
            alreadyAmount = "";
        }else {
            // 设置了预算
            hasBudget = true;
            alreadyAmount = alreadyBudget.getBudgetAmount();
        }

        CommentDialog dialog = builder.setTitle(title)
                .setIcon(R.drawable.ic_comment)
                .setEditHint(title)
                .setEditInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .setDefaultText(alreadyAmount)
                .setEditMaxLength(10)
                .setCancel("取消", cancelStr -> {
                })
                .setConfirm("确定", (editText, confirmStr) -> {
                    // 没有输入预算
                    if (TextUtils.isEmpty(editText.getText())) {
                        Log.d(TAG, "budgetSetting: empty");
                        return;
                    }
                    // 按下确认之后, 即修改BillViewModel中的预算的LiveData同时也把更新的数据插回数据库中
                    // 先查看数据库是否已经有原来的预算
                    String inputAmount = editText.getText().toString();
                    // 格式转换
                    BigDecimal amountDecimal = new BigDecimal(inputAmount);
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String inputAmountToDB = decimalFormat.format(amountDecimal);
                    if (!hasBudget) {
                        // 原先没有数据
                        // 按确定则添加
                        BudgetsManagement.addNewBudget(requireContext(), year, month, inputAmountToDB);
                    }
                    else {
                        // 原先已经有数据
                        // 这里只是修改预算的金额 按确定则修改原有预算
                        alreadyBudget.setBudgetAmount(inputAmountToDB);
                        BudgetsManagement.updateBudget(requireContext(), alreadyBudget);
                    }
                    // BillViewModel中也要设置
                    mBillViewModel.setBudgetLiveData(inputAmountToDB);
                })
                .create();
        dialog.show();
    }

    /**
     * 分类管理
     */
    private void categorySetting() {
        startActivity(new Intent(requireActivity(), CategorySettingActivity.class));
    }

    /**
     * 查看年度数据
     */
    private void viewAnnualBills() {
        startActivity(new Intent(requireActivity(), AnnualBillsActivity.class));
    }

    /**
     * 账户管理
     */
    private void accountsManagement() {
        // 跳转到新的Activity
        Intent intent = new Intent(requireActivity(), AccountsActivity.class);
        startActivity(intent);
    }

    /**
     * 系统设置
     */
    private void systemSetting() {

    }

    private void setSettingStrategies() {
        mStrategiesList.add(new MineSetting("预算管理", "设置您每个月的预算", R.drawable.ic_mine_budget));
        mStrategiesList.add(new MineSetting("分类管理", "添加您自定义的收入或支出分类", R.drawable.ic_mine_category_manage));
        mStrategiesList.add(new MineSetting("年度数据", "查看指定年份的所有收入支出信息", R.drawable.ic_mine_stat));
        mStrategiesList.add(new MineSetting("账户管理", "增加或者修改您的账户信息, 包括账户名等", R.drawable.ic_mine_accounts));
        mStrategiesList.add(new MineSetting("导出数据", "将当前已有数据导出成Excel表格", R.drawable.ic_mine_export));
        mStrategiesList.add(new MineSetting("导入数据", "从Excel表格中导入数据", R.drawable.ic_mine_import));
        mStrategiesList.add(new MineSetting("系统设置", "该应用设置和相关信息", R.drawable.ic_mine_setting));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mStrategiesList != null) {
            mStrategiesList.clear();
            mStrategiesList = null;
        }
    }
}
