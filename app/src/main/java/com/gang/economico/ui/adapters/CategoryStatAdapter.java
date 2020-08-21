package com.gang.economico.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.model.StatisticModel;


import java.util.ArrayList;
import java.util.List;

/**
 * Description: 分类展示的RecyclerView的Adapter
 * Time: 4/15/2020
*/
public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.CategoryViewHolder> {

    public static final String TAG = "CategoryStatAdapter";
    private List<StatisticModel> mData;
    private int mMaxProgress;
    private OnCateItemClickListener mListener;

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView mCategoryIconIv;
        TextView mCategoryNameTv;
        TextView mAmountTv;
        ProgressBar mProgressBar;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            mCategoryIconIv = itemView.findViewById(R.id.stat_category_img);
            mCategoryNameTv = itemView.findViewById(R.id.stat_category_name);
            mProgressBar = itemView.findViewById(R.id.stat_category_progress);
            mAmountTv = itemView.findViewById(R.id.stat_amount);
        }
    }

    public CategoryStatAdapter() {}

    public void setCategoryStatList(List<StatisticModel> dataList) {
        if (dataList != null) {
            if (mData == null) {
                mData = new ArrayList<>();
            }
            else {
                if (mData.size() != 0) {
                    mData.clear();
                }
            }
            mData.addAll(dataList);
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_category_percentage, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.mCategoryIconIv.setImageResource(mData.get(position).getImgRes());
        holder.mCategoryNameTv.setText(mData.get(position).getCategoryName());
        String amountText = "￥" + mData.get(position).getAmountDecimal().toPlainString();
        holder.mAmountTv.setText(amountText);
        // 此处的进度值
        holder.mProgressBar.setMax(mMaxProgress);
        holder.mProgressBar.setProgress(mData.get(position).getAmountDecimal().intValue());
        if (mListener != null) {
            holder.itemView.setOnClickListener(view -> {
                mListener.onCateItemClick(mData.get(holder.getLayoutPosition()));
            });
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
    }

    public interface OnCateItemClickListener {
        void onCateItemClick(StatisticModel statisticModel);
    }

    public void setOnCateItemClickListener(OnCateItemClickListener l) {
        mListener = l;
    }
}
