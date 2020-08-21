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
import android.widget.ImageView;

import com.gang.economico.R;
import com.gang.economico.model.MonthlyStatistic;
import com.gang.economico.ui.adapters.MonthlyStatAdapter;
import com.gang.economico.ui.customs.CurveChartView;
import com.gang.economico.viewmodels.YearlyViewModel;

import java.time.Year;
import java.util.List;

/**
 * 年度数据总览
 */
public class MineAnnualOverviewFragment extends Fragment {

    private static final String TAG = "MineAnnualOverview";
    private RecyclerView mOverviewRv;
    private CurveChartView mChartView;
    private boolean isFirstLoad = true;

    private MonthlyStatAdapter mAdapter;
    private YearlyViewModel mViewModel;
    private Observer<List<MonthlyStatistic>> mObserver;
    private Observer<List<Float>> mChartObserver;

    public MineAnnualOverviewFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mAdapter = new MonthlyStatAdapter();
        mViewModel = new ViewModelProvider(requireActivity()).get(YearlyViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_mine_annual_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        mOverviewRv = view.findViewById(R.id.mine_annual_overview_rv);
        mOverviewRv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mChartView = view.findViewById(R.id.mine_annual_curve_view);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        // 此处加载数据
        if (isFirstLoad) {
            // 第一次加载数据
            mViewModel.loadOverviewData();
            isFirstLoad = false;
            Log.d(TAG, "onResume: first load");
        }
        if (mObserver == null) {
            mObserver = monthlyStatistics -> {
                Log.d(TAG, "onResume: changed " + monthlyStatistics);
                mAdapter.setMonthlyStatisticList(monthlyStatistics);
                mAdapter.notifyDataSetChanged();
            };
            mViewModel.getYearlyLiveData().observe(requireActivity(), mObserver);
        }
        mOverviewRv.setAdapter(mAdapter);

        if (mChartObserver == null) {
            mChartObserver = floats -> {
                String curveChartTitle = mViewModel.getQueryCondition() == 1 ? "本年度总支出趋势" : "本年度总收入趋势";
                mChartView.setTitle(curveChartTitle);
                mChartView.setData(floats);
            };
            mViewModel.getMonthBillsLiveData().observe(requireActivity(), mChartObserver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.getYearlyLiveData().removeObserver(mObserver);
        mViewModel.getMonthBillsLiveData().removeObserver(mChartObserver);
        mObserver = null;
        mChartObserver = null;
        isFirstLoad = true;
        mAdapter.clearData();
        mAdapter = null;
        mOverviewRv.setAdapter(null);
        mOverviewRv.setLayoutManager(null);
        Log.d(TAG, "onDestroy: ");
    }
}
