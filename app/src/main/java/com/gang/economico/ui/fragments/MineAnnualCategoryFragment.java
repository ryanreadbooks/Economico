package com.gang.economico.ui.fragments;

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

import com.gang.economico.R;
import com.gang.economico.model.StatisticModel;
import com.gang.economico.model.YearlyCategoryStatistic;
import com.gang.economico.ui.adapters.YearlyCategoryAdapter;
import com.gang.economico.ui.customs.PieChartView;
import com.gang.economico.viewmodels.AccountsViewModel;
import com.gang.economico.viewmodels.YearlyViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MineAnnualCategoryFragment extends Fragment {

    private static final String TAG = "MineAnnualCategory";
    private RecyclerView mCateRv;
    private PieChartView mPieChartView;

    private YearlyViewModel mViewModel;
    private YearlyCategoryAdapter mAdapter;

    private Observer<List<YearlyCategoryStatistic>> mObserver;
    private boolean isFirstLoad = true;

    public MineAnnualCategoryFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(YearlyViewModel.class);
        mAdapter = new YearlyCategoryAdapter(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_mine_annual_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        mCateRv = view.findViewById(R.id.mine_annual_category_rv);
        mCateRv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mCateRv.setAdapter(mAdapter);

        mPieChartView = view.findViewById(R.id.mine_annual_cate_pie);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        if (isFirstLoad) {
            // 加载数据
            mViewModel.loadCategorizedData();
            isFirstLoad = false;
            Log.d(TAG, "onResume: is first load in category");
        }
        if (mObserver == null) {
            mObserver = yearlyCategoryStatistics -> {
                Log.d(TAG, "onResume: " + yearlyCategoryStatistics);
                mAdapter.setCategorizedData(yearlyCategoryStatistics);
                mAdapter.notifyDataSetChanged();
                mAdapter.setMaxProgress((int)Float.parseFloat(Objects.requireNonNull(mViewModel.getTotalAmountLiveData().getValue())));
                // 同时更新PieChart
                List<StatisticModel> pieData = new ArrayList<>();
                for (YearlyCategoryStatistic statistic : yearlyCategoryStatistics) {
                    pieData.add(new StatisticModel(0, statistic.getCateName(), statistic.getAmountDecimal().toPlainString()));
                }
                if (mViewModel.getQueryCondition() == AccountsViewModel.QUERY_SPENDING) {
                    mPieChartView.setColorSchema(false);
                }
                else {
                    mPieChartView.setColorSchema(true);
                }
                mPieChartView.refreshDisplayData(pieData);
            };
            mViewModel.getCategorizedLiveData().observe(requireActivity(), mObserver);
        }
        mCateRv.setAdapter(mAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mViewModel.getCategorizedLiveData().removeObserver(mObserver);
        mObserver = null;
        isFirstLoad = false;
        mAdapter.clearData();
        mAdapter = null;
        mCateRv.setAdapter(null);
        mCateRv.setLayoutManager(null);
        Log.d(TAG, "onDetach: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
