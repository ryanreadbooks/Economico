package com.gang.economico.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.model.YearlyCategoryStatistic;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 年度数据中分类展示中RV的适配器
 */
public class YearlyCategoryAdapter extends RecyclerView.Adapter<YearlyCategoryAdapter.ViewHolder> {

    private static final String TAG = "YearlyCategoryAdapter";
    private final DecimalFormat mDecimalFormat;
    private List<YearlyCategoryStatistic> mData;    // 数据本身
    private Context mContext;
    private int mMaxProgress;

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mCategoryIconIv;
        TextView mCategoryNameTv;
        TextView mAmountTv;
        ProgressBar mProgressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCategoryIconIv = itemView.findViewById(R.id.mine_annual_cate_icon);
            mCategoryNameTv = itemView.findViewById(R.id.mine_annual_cate_name);
            mProgressBar = itemView.findViewById(R.id.mine_annual_cate_progress);
            mAmountTv = itemView.findViewById(R.id.mine_annual_cate_amount);
        }
    }

    public YearlyCategoryAdapter(Context context) {
        mData = new ArrayList<>();
        mContext = context;
        mDecimalFormat = new DecimalFormat("0.00");
    }

    public void setCategorizedData(List<YearlyCategoryStatistic> l) {
        if (l != null) {
            mData = l;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mine_annual_category_percentage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String cateName = mData.get(position).getCateName();
        int imgRes = mContext.getSharedPreferences("category_img_res", Context.MODE_PRIVATE).getInt(cateName, R.drawable.ic_spending_regular);
        BigDecimal bd = new BigDecimal(mData.get(position).getAmount());
        String amountString = mDecimalFormat.format(bd);
        holder.mCategoryIconIv.setImageResource(imgRes);
        holder.mCategoryNameTv.setText(cateName);
        holder.mAmountTv.setText(amountString);
        holder.mProgressBar.setMax(mMaxProgress);
        int progress = bd.intValue();
        holder.mProgressBar.setProgress(progress);
    }

    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void clearData() {
        if (mData != null) {
            mData.clear();
        }
        mData = null;
        mContext = null;
    }
}
