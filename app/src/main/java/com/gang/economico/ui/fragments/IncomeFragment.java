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

/*
* Description: 收入信息界面Fragment
*/
public class IncomeFragment extends Fragment {

    private final static String TAG = "income fragment";
    private View mView;
    private HistogramView mIncomeHistogramView;
    private PieChartView mIncomePieCharView;
    private RecyclerView mCategoryStatList;
    private TextView mIncomeTv;
    private TextView mIncomeSurplusTv;
    private TextView mAvgIncomeTv;

    private CategoryStatAdapter mStatAdapter;
    private BillViewModel mBillViewModel;

    private Observer<String> mSurplusObserver;
    private Observer<String> mIncomeObserver;
    private Observer<List<StatisticModel>> mIncomeStatObserver;
    private Observer<List<Float>> mDailyIncomeObserver;

    private boolean isFirstLoad = true;

    public IncomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: income fragment");
        super.onCreate(savedInstanceState);
        mBillViewModel = new ViewModelProvider(requireActivity()).get(BillViewModel.class);
        mStatAdapter = new CategoryStatAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: income");
        mView = inflater.inflate(R.layout.fragment_income, container, false);
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
            if (mIncomeObserver == null) {
                mIncomeObserver = (s) -> {
                    incomeInfoDisplay(s, false);
                };
                mBillViewModel.getIncomeLiveData().observe(requireActivity(), mIncomeObserver);
            }

            if (mSurplusObserver == null) {
                mSurplusObserver = new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        incomeInfoDisplay(s, true);
                    }
                };
                mBillViewModel.getSurplusLiveData().observe(requireActivity(), mSurplusObserver);
            }
            // 统计数据观察者
            if (mIncomeStatObserver == null) {
                mIncomeStatObserver = (statisticModels) -> {
                    if (mBillViewModel.getIncomeLiveData().getValue() != null) {
                        int totalIncomeAmount = (int)Float.parseFloat(mBillViewModel.getIncomeLiveData().getValue());
                        mStatAdapter.setMaxProgress(totalIncomeAmount);
                    }
                    mIncomePieCharView.refreshDisplayData(statisticModels);
                    mStatAdapter.setCategoryStatList(statisticModels);
                    mStatAdapter.notifyDataSetChanged();
                };
                mBillViewModel.getIncomeStatLiveData().observe(requireActivity(), mIncomeStatObserver);
            }
            // 每日收入观察者
            if (mDailyIncomeObserver == null) {
                mDailyIncomeObserver = (floats) -> {
                    if (floats != null) {
                        mIncomeHistogramView.refreshDisplayData(floats);
                        mIncomeHistogramView.setSelectedBarText(mBillViewModel.getViewModelMonth() + "-", "收入", "元");
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                        String prefix = "";
                        if (currentYear != mBillViewModel.getViewModelYear()) {
                            prefix = mBillViewModel.getViewModelYear() + "年";
                        }
                        String str = prefix + mBillViewModel.getViewModelMonth() + "月收入情况";
                        mIncomeHistogramView.setTitle(str);
                    }
                };
                mBillViewModel.getDailyIncomeLiveData().observe(requireActivity(), mDailyIncomeObserver);
            }

            // header信息显示
            if (mBillViewModel.getSurplusLiveData().getValue() != null) {
                incomeInfoDisplay(mBillViewModel.getSurplusLiveData().getValue(), true);
            }
            if (mBillViewModel.getIncomeLiveData().getValue() != null) {
                incomeInfoDisplay(mBillViewModel.getIncomeLiveData().getValue(), false);
            }

            if (mBillViewModel.getIncomeStatLiveData().getValue() != null) {
                mStatAdapter.setCategoryStatList(mBillViewModel.getIncomeStatLiveData().getValue());
                mIncomePieCharView.refreshDisplayData(mBillViewModel.getIncomeStatLiveData().getValue());
            }

            if (mBillViewModel.getDailyIncomeLiveData().getValue() != null) {
                mIncomeHistogramView.refreshDisplayData(mBillViewModel.getDailyIncomeLiveData().getValue());
            }

            mCategoryStatList.setAdapter(mStatAdapter);
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

    private void bindView() {
        int month = mBillViewModel.getViewModelMonth();
        mIncomeHistogramView = mView.findViewById(R.id.fragment_income_histogram);
        mIncomeHistogramView.setShowNumTop(false);
        mIncomeHistogramView.setSelectedBarText(month + "-", "收入", "元");
        String str = month + "月收入情况";
        mIncomeHistogramView.setTitle(str);
        mIncomeHistogramView.setOnBarClickListener(new HistogramView.OnBarClickListener() {
            @Override
            public void onBarClick(int pos, float value) {
            }
        });

        mIncomePieCharView =mView.findViewById(R.id.stat_income_pie_chart);

        mCategoryStatList = mView.findViewById(R.id.stat_income_category_rv);
        mCategoryStatList.setLayoutManager(new LinearLayoutManager(mView.getContext(), RecyclerView.VERTICAL, false));

        mIncomeTv = mView.findViewById(R.id.header_income_total_amount);
        mAvgIncomeTv = mView.findViewById(R.id.header_income_avg_amount);
        mIncomeSurplusTv = mView.findViewById(R.id.header_income_surplus_amount);
    }

    // 收入信息header
    private void incomeInfoDisplay(String incomeStr, boolean type) {
        if (type) {
            mIncomeSurplusTv.setText(incomeStr);
        }
        else {
            mIncomeTv.setText(incomeStr);
            int dayOfMonth = 0;
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            if (currentYear == mBillViewModel.getViewModelYear() && currentMonth == mBillViewModel.getViewModelMonth()) {
                // 这个月还没有过去 平均收入 = 当前收入 / 本月已经过完的天数
                dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            }
            else {
                // 选择查看的时间已经是过去式 则 平均收入 = 那个月的总收入 / 那个月的总天数
                Calendar c = new GregorianCalendar(mBillViewModel.getViewModelYear(), mBillViewModel.getViewModelMonth() - 1, 1);
                dayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            BigDecimal dayDecimal = new BigDecimal(dayOfMonth);
            BigDecimal totalIncomeDecimal = new BigDecimal(incomeStr);
            BigDecimal avgIncomeDecimal = totalIncomeDecimal.divide(dayDecimal, 2, RoundingMode.UP);
            mAvgIncomeTv.setText(avgIncomeDecimal.toPlainString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBillViewModel.getSurplusLiveData().removeObserver(mSurplusObserver);
        mSurplusObserver = null;
        mBillViewModel.getIncomeLiveData().removeObserver(mIncomeObserver);
        mIncomeObserver = null;
        mBillViewModel.getIncomeStatLiveData().removeObserver(mIncomeStatObserver);
        mIncomeStatObserver = null;
        mBillViewModel.getDailyIncomeLiveData().removeObserver(mDailyIncomeObserver);
        mDailyIncomeObserver = null;
        isFirstLoad = true;
    }
}
