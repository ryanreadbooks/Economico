package com.gang.economico.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.entities.BillRecord;
import com.gang.economico.ui.activities.EditActivity;
import com.gang.economico.ui.activities.MainActivity;
import com.gang.economico.ui.adapters.MainInfoListAdapter;
import com.gang.economico.ui.customs.CustomaryDialog;
import com.gang.economico.ui.customs.ProgressRingView;
import com.gang.economico.viewmodels.BillViewModel;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

public class OverviewFragment extends Fragment {

    private static final String TAG = "overview fragment";

    private View mView;
    private RecyclerView mBillsRecyclerView;
    private ImageView mNoDataView;
    private TextView mSurplusView;
    private TextView mTipsView;
    private ProgressRingView mBudgetRingView;

    private MainInfoListAdapter mListAdapter;
    private BillViewModel mBillViewModel;
    // 总预算的BigDecimal
    private BigDecimal mTotalBudgetsDecimal;
    // 总预算
    private String mTotalBudgets;
    // 主数据观察者
    private Observer<List<BillRecord>> mMainObserver;
    // 该月预算观察者
    private Observer<String> mBudgetObserver;
    private boolean hasBudget = false;

    private boolean isFirstLoad = true;
    public OverviewFragment() {}

    // onCreate在生命周期总只会调用一次，因此一些只需要进行一次的初始化的工作就要放在onCreate()方法里面，比如观察者的注册
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: overview fragment");
        super.onCreate(savedInstanceState);
        // 找到BillViewModel
        mBillViewModel = new ViewModelProvider(requireActivity()).get(BillViewModel.class);
        // RecyclerView的适配器
        mListAdapter = new MainInfoListAdapter();

        int initYear = Calendar.getInstance().get(Calendar.YEAR);
        int initMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        mBillViewModel.getMonthlyBills(initYear, initMonth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView overview fragment");
        mView = inflater.inflate(R.layout.fragment_overview, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);

        // 控件查找绑定
        bindView();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (isFirstLoad) {
            // 文字信息的显示
            if (mBillViewModel.getSpendingLiveData().getValue() != null) {
                BigDecimal totalSpending = new BigDecimal(mBillViewModel.getSpendingLiveData().getValue());
                tipsTextDisplay(mBillViewModel.getSurplusLiveData().getValue(), totalSpending, hasBudget);
            }

            // RecyclerView绑定适配器
            if (mBillsRecyclerView.getAdapter() == null) {
                mBillsRecyclerView.setAdapter(mListAdapter);
            }
            // 适配器的数据注入，数据列表来自BillViewModel中
            mListAdapter.setBillRecordList(mBillViewModel.getMainListLiveData().getValue());

            // RecyclerView和那张图片的隐藏逻辑
            if (mBillViewModel.getMainListLiveData().getValue().size() == 0) {
                mNoDataView.setVisibility(View.VISIBLE);
            }
            else {
                mNoDataView.setVisibility(View.INVISIBLE);
            }

            // 给BillViewModel中的MainListLiveData注册观察者
            if (mMainObserver == null) {
                mMainObserver = billRecords -> {

                    mListAdapter.setBillRecordList(billRecords);
                    mListAdapter.notifyDataSetChanged();
                    if (mBillViewModel.getSpendingLiveData().getValue() != null) {
                        BigDecimal totalSpendingDecimal = new BigDecimal(mBillViewModel.getSpendingLiveData().getValue());
                        String surplusStr = mBillViewModel.getSurplusLiveData().getValue();
                        // header文字提示信息的显示
                        tipsTextDisplay(surplusStr, totalSpendingDecimal, hasBudget);
                    }
                    // 当月无数据的图片显示
                    if (billRecords.size() == 0) {
                        mNoDataView.setVisibility(View.VISIBLE);
                    }
                    else {
                        mNoDataView.setVisibility(View.INVISIBLE);
                    }
                };
                mBillViewModel.getMainListLiveData().observe(requireActivity(), mMainObserver);
            }

            // 给BillViewModel中的BudgetLiveData注册观察者
            if (mBudgetObserver == null) {
                mBudgetObserver = s -> {
                    // 当本月的预算发生变化时更新界面
                    // 更新界面包括 1 -> 提示信息的更新(根据预算所剩下的数额); 2 ->预算环的最大值的重新设置
                    // mTotalBudgets 和 mTotalBudgetsDecimal 的更新
                    // 提示信息的更新
                    if (mBillViewModel.getSpendingLiveData().getValue() != null) {
                        BigDecimal totalSpendingDecimal = new BigDecimal(mBillViewModel.getSpendingLiveData().getValue());
                        String surplusStr = mBillViewModel.getSurplusLiveData().getValue();
                        // header文字提示信息的显示
                        if ("not set".equals(s)){
                            hasBudget = false;
                            Log.d(TAG, "onCreateView: does not have budget");
                        }
                        else {
                            hasBudget = true;
                            mTotalBudgets = s;
                            mTotalBudgetsDecimal = new BigDecimal(mTotalBudgets);
                            Log.d(TAG, "onCreateView: has budget");
                            // 预算环的最大值的重新设置
                            mBudgetRingView.setMaxValue(Float.parseFloat(s));
                        }
                        Log.d(TAG, "onCreateView: inside mBudgetObserver");
                        tipsTextDisplay(surplusStr, totalSpendingDecimal, hasBudget);
                    }
                };
                mBillViewModel.getBudgetLiveData().observe(requireActivity(), mBudgetObserver);
            }
            isFirstLoad = false;
        }
    }

    private void bindView() {
        mBillsRecyclerView = mView.findViewById(R.id.main_info_list);
        mBillsRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        // 设置列表的点击事件监听, 单击可以修改该条目, 修改的界面就是编辑的界面
        mListAdapter.setOnBillClickListener((itemView, billRecord) -> {
            // 把这个传递到EditActivity中
            Intent intent = new Intent(requireActivity(), EditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("updating_bill", billRecord);
            intent.putExtra("request_operation", MainActivity.UPDATE_BILL);
            intent.putExtra("updating_data", bundle);
            requireActivity().startActivityForResult(intent, MainActivity.UPDATE_BILL);
        });
        // 设置列表长按时事件
        mListAdapter.setOnBillLongClickListener((billRecord) -> {
            // 定义长按删除 先弹出Dialog
            Log.d(TAG, "bindView: " + billRecord.toString());
            CustomaryDialog.Builder builder = new CustomaryDialog.Builder(requireActivity());
            builder.setIcon(0)
                    .setTitle("确认删除?")
                    .setContent("是否确认删除该条目, 删除后不可恢复")
                    .setCancel("取消", Dialog::dismiss)
                    .setConfirm("确认删除", dialog -> {
                        BillViewModel.deleteBill(billRecord);
                        int year = billRecord.getRecordYear();
                        int month = billRecord.getRecordMonth();
                        mBillViewModel.getMonthlyBills(year, month);
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });

        // onCreateView时初始化界面
        mNoDataView = mView.findViewById(R.id.main_no_data);
        mSurplusView = mView.findViewById(R.id.surplus);
        mTipsView = mView.findViewById(R.id.tips_info_text);
        mBudgetRingView = mView.findViewById(R.id.overview_budget_remain_ringview);
        String budget = mBillViewModel.getBudgetLiveData().getValue();
        if (budget != null) {
            if ("not set".equals(budget)) {
                mBudgetRingView.setMaxValue(0.0f);
            }
            else {
                mBudgetRingView.setMaxValue(Float.parseFloat(budget));
            }
        }
    }

    /**
     * 提示信息的显示
     * @param surplusStr 当月结余的信息
     * @param totalSpending 当月总支出
     */
    private  void tipsTextDisplay(String surplusStr, BigDecimal totalSpending, boolean hasBudget) {
        String tipsText = "";
        float currentValue = 0.0f;
        // 没有设置预算
        if (!hasBudget) {
            tipsText = "本月未设置预算";
        }
        else {
            // 剩余预算 = 总预算 - 已支出值
            BigDecimal remainingBudgets = mTotalBudgetsDecimal.subtract(totalSpending);
            // 预算情况判断
            if (remainingBudgets.floatValue() <= 0) {
                // 预算已使用完
                tipsText = "本月预算已用完, 超支" + remainingBudgets.negate().toPlainString() + "元!";
                currentValue = 0.0f;
            }
            else {
                float remainingPercentage = remainingBudgets.divide(mTotalBudgetsDecimal, 2, BigDecimal.ROUND_HALF_EVEN).floatValue();
                currentValue = remainingBudgets.floatValue();
                if (remainingPercentage >= 0.5) {
                    // 预算充足 还剩下50%以上
                    tipsText = "本月剩余预算充足, 剩余" + remainingBudgets.toPlainString() + "元.";
                }
                else if (remainingPercentage >= 0.2 && remainingPercentage < 0.5) {
                    // 预算提醒 还剩下20% ~ 50%之间
                    tipsText = "本月使用预算已过半, 剩余" + remainingBudgets.toPlainString() + "元, 请注意.";
                }
                else if (remainingPercentage > 0.0 && remainingPercentage < 0.2) {
                    // 预算准备耗尽 还剩下0% ~ 20%之间
                    tipsText = "本月预算即将耗尽, 剩余" + remainingBudgets.toPlainString() + "元, 请谨慎消费.";
                }
            }
        }
        // 当月结余信息
        mSurplusView.setText(surplusStr);
        // 提示还剩下多少预算的信息
        mTipsView.setText(tipsText);
        mBudgetRingView.setCurrentValue(currentValue);

    }

    @Override
    public void onPause() {
        // Log.d(TAG, "onPause: overview fragment");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onStart() {
        // Log.d(TAG, "onStart: overview fragment");
        super.onStart();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: overview fragment");
        super.onDestroy();
        mBillViewModel.getMainListLiveData().removeObserver(mMainObserver);
        mMainObserver = null;
        mBillViewModel.getBudgetLiveData().removeObserver(mBudgetObserver);
        mBudgetObserver = null;
        isFirstLoad = true;
    }

}
