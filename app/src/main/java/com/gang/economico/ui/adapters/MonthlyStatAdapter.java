package com.gang.economico.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.model.MonthlyStatistic;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MonthlyStatAdapter extends RecyclerView.Adapter<MonthlyStatAdapter.ViewHolder> {

    private static final String TAG = "MonthlyStatAdapter";
    private List<MonthlyStatistic> mMonthlyStatisticList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mYearTv;
        TextView mAmountTv;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mYearTv = itemView.findViewById(R.id.mine_annual_year_item);
            mAmountTv = itemView.findViewById(R.id.mine_annual_amount_item);
        }
    }

    public MonthlyStatAdapter() {
        mMonthlyStatisticList = new ArrayList<>();
    }

    public void setMonthlyStatisticList(List<MonthlyStatistic> list) {
        if (list != null) {
            mMonthlyStatisticList = list;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mine_annual_monthly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mMonthlyStatisticList.size() == 0) {
            return;
        }
        String monthStr = mMonthlyStatisticList.get(position).getMonth() + "月";
        holder.mYearTv.setText(monthStr);
        // 格式化
        String amountStr = new DecimalFormat("0.00")
                .format(new BigDecimal(mMonthlyStatisticList.get(position).getMonthlyAmount()));
        holder.mAmountTv.setText(amountStr);
    }

    @Override
    public int getItemCount() {
        return mMonthlyStatisticList.size();
    }

    public void clearData() {
        if (mMonthlyStatisticList != null) {
            mMonthlyStatisticList.clear();
        }
        mMonthlyStatisticList = null;
    }
}
