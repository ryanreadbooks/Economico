package com.gang.economico.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gang.economico.R;
import com.gang.economico.model.CategoryQueryConditions;
import com.gang.economico.model.StatisticModel;
import com.gang.economico.ui.activities.CategoryActivity;
import com.gang.economico.ui.activities.MainActivity;
import com.gang.economico.ui.adapters.CategoryStatAdapter;
import com.gang.economico.ui.customs.HistogramView;
import com.gang.economico.ui.customs.PieChartView;
import com.gang.economico.viewmodels.BillViewModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
* Description: 支出信息界面对应Fragment
*/
public class SpendingFragment extends Fragment {

    private static final String TAG = "SpendingFragment";
    private View mView;
    private HistogramView mHistogramView;
    private PieChartView mPieChartView;
    private RecyclerView mSpendingCategoryStatList;
    private TextView mSpendingTv;
    private TextView mAvgSpendingTv;
    private TextView mSpendingSurplusTv;

    private CategoryStatAdapter mStatAdapter;
    private BillViewModel mBillViewModel;

    private Observer<String> mSurplusObserver;
    private Observer<String> mSpendingObserver;
    private Observer<List<StatisticModel>> mSpendingStatObserver;
    private Observer<List<Float>> mDailySpendingObserver;

    private boolean isFirstLoad = true;

    public SpendingFragment() {}

    // 初始化工作
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: SpendingFragment");
        super.onCreate(savedInstanceState);
        mStatAdapter = new CategoryStatAdapter();
        // 拿到BillViewModel
        mBillViewModel = new ViewModelProvider(requireActivity()).get(BillViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: SpendingFragment");
        mView = inflater.inflate(R.layout.fragment_spending, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);

        bindView();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (isFirstLoad) {
            // 注册一系列观察者
            if (mSurplusObserver == null) {
                mSurplusObserver = (s) -> {
                    spendingInfoDisplay(s, true);
                };
                mBillViewModel.getSurplusLiveData().observe(requireActivity(), mSurplusObserver);
            }
            if (mSpendingObserver == null) {
                mSpendingObserver = s -> {
                    spendingInfoDisplay(s, false);
                };
                mBillViewModel.getSpendingLiveData().observe(requireActivity(), mSpendingObserver);
            }
            // 分类统计数据
            if (mSpendingStatObserver == null) {
                mSpendingStatObserver = statisticModels -> {
                    if (mBillViewModel.getSpendingLiveData().getValue() != null) {
                        int totalSpendingAmount = (int)Float.parseFloat(mBillViewModel.getSpendingLiveData().getValue());
                        mStatAdapter.setMaxProgress(totalSpendingAmount);
                        // 重新设置饼图数据
                        mPieChartView.refreshDisplayData(statisticModels);
                    }
                    mStatAdapter.setCategoryStatList(statisticModels);
                    mStatAdapter.notifyDataSetChanged();
                };
                mBillViewModel.getSpendingStatLiveData().observe(requireActivity(), mSpendingStatObserver);
            }
            // 每日支出统计
            if (mDailySpendingObserver == null) {
                mDailySpendingObserver = floats -> {
                    if (floats != null) {
                        mHistogramView.refreshDisplayData(floats);
                        mHistogramView.setSelectedBarText(mBillViewModel.getViewModelMonth() + "-", "使用", "元");
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                        String prefix = "";
                        if (currentYear != mBillViewModel.getViewModelYear()) {
                            prefix = mBillViewModel.getViewModelYear() + "年";
                        }
                        String str = prefix + mBillViewModel.getViewModelMonth() + "月支出情况";
                        mHistogramView.setTitle(str);
                    }
                };
                mBillViewModel.getDailySpendingLiveData().observe(requireActivity(), mDailySpendingObserver);
            }

            // header信息显示
            if (mBillViewModel.getSurplusLiveData().getValue() != null) {
                spendingInfoDisplay(mBillViewModel.getSurplusLiveData().getValue(), true);
            }
            if (mBillViewModel.getSpendingLiveData().getValue() != null) {
                spendingInfoDisplay(mBillViewModel.getSpendingLiveData().getValue(), false);
            }

            if (mBillViewModel.getSpendingStatLiveData().getValue() != null) {
                mStatAdapter.setCategoryStatList(mBillViewModel.getSpendingStatLiveData().getValue());
                mPieChartView.refreshDisplayData(mBillViewModel.getSpendingStatLiveData().getValue());
                // int totalSpendingAmount = Integer.parseInt(mBillViewModel.getSpendingLiveData().getValue());
                // mStatAdapter.setMaxProgress(totalSpendingAmount);
            }

            if (mBillViewModel.getDailySpendingLiveData().getValue() != null) {
                mHistogramView.refreshDisplayData(mBillViewModel.getDailySpendingLiveData().getValue());
            }

            mSpendingCategoryStatList.setAdapter(mStatAdapter);
            // 单项点击监听 请求一个Activity展示分类数据
            mStatAdapter.setOnCateItemClickListener(statisticModel -> {
                Intent intent = new Intent(requireActivity(), CategoryActivity.class);
                CategoryQueryConditions categoryToDisplay = new CategoryQueryConditions(
                        statisticModel.getImgRes(),
                        statisticModel.getCategoryName(),
                        mBillViewModel.getViewModelYear(),
                        mBillViewModel.getViewModelMonth(),
                        statisticModel.getAmountDecimal().toPlainString()
                );
                Bundle bundle = new Bundle();
                bundle.putParcelable("cate_to_display", categoryToDisplay);
                intent.putExtra("cate_data", bundle);
                requireActivity().startActivityForResult(intent, MainActivity.REQUEST_DISPLAY_CATEGORY);
            });
            isFirstLoad = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "spending onActivityResult: request" + requestCode);
        Log.d(TAG, "spending onActivityResult: result" + resultCode);
    }

    private void bindView() {
        // 默认显示时间
        int month = mBillViewModel.getViewModelMonth();
        mHistogramView = mView.findViewById(R.id.fragment_spending_histogram);
        mHistogramView.setShowNumTop(false);
        mHistogramView.setSelectedBarText(month + "-", "使用", "元");
        String str = month + "月支出情况";
        mHistogramView.setTitle(str);
        mHistogramView.setOnBarClickListener(new HistogramView.OnBarClickListener() {
            @Override
            public void onBarClick(int pos, float value) {
            }
        });

        mPieChartView = mView.findViewById(R.id.stat_spending_pie_chart);

        mSpendingCategoryStatList = mView.findViewById(R.id.stat_spending_category_rv);
        mSpendingCategoryStatList.setLayoutManager(new LinearLayoutManager(mView.getContext(), RecyclerView.VERTICAL, false));

        mSpendingTv = mView.findViewById(R.id.header_spending_total_amount);
        mAvgSpendingTv = mView.findViewById(R.id.header_spending_avg_amount);
        mSpendingSurplusTv = mView.findViewById(R.id.header_spending_surplus_amount);
    }

    // 支出信息header
    private void spendingInfoDisplay(String spendingStr, boolean type) {
        if (type) {
            mSpendingSurplusTv.setText(spendingStr);
        }
        else {
            mSpendingTv.setText(spendingStr);
            int dayOfMonth = 0;
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            if (currentYear == mBillViewModel.getViewModelYear() && currentMonth == mBillViewModel.getViewModelMonth()) {
                // 这个月还没有过去 平均支出 = 当前支出 / 本月已经过完的天数
                dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            }
            else {
                // 选择查看的时间已经是过去式 则 平均支出 = 那个月的总支出 / 那个月的总天数
                Calendar c = new GregorianCalendar(mBillViewModel.getViewModelYear(), mBillViewModel.getViewModelMonth() - 1, 1);
                dayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            BigDecimal dayDecimal = new BigDecimal(dayOfMonth);
            BigDecimal totalSpendingDecimal = new BigDecimal(spendingStr);
            BigDecimal avgSpendingDecimal = totalSpendingDecimal.divide(dayDecimal, 2, RoundingMode.UP);
            mAvgSpendingTv.setText(avgSpendingDecimal.toPlainString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBillViewModel.getSurplusLiveData().removeObserver(mSurplusObserver);
        mSurplusObserver = null;
        mBillViewModel.getSpendingLiveData().removeObserver(mSpendingObserver);
        mSpendingObserver = null;
        mBillViewModel.getSpendingStatLiveData().removeObserver(mSpendingStatObserver);
        mSpendingStatObserver = null;
        mBillViewModel.getDailySpendingLiveData().removeObserver(mDailySpendingObserver);
        mDailySpendingObserver = null;
        isFirstLoad = true;
    }

}
