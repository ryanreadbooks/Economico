package com.gang.economico.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.entities.BillRecord;

import java.util.ArrayList;
import java.util.List;


/*
* Description: 主页的RecyclerView的Adapter
* Time: 4/11/2020
*/
public class MainInfoListAdapter extends RecyclerView.Adapter<MainInfoListAdapter.InfoViewHolder> {

    private static final String TAG = "MainInfoListAdapter";
    private List<BillRecord> mBillRecordList;
    private Context mContext;
    private OnBillClickListener mOnBillClickListener;
    private OnBillLongClickListener mOnBillLongClickListener;

    // 颜色值
    private final int mSpendingTextColor = Color.parseColor("#ffffff");
    private final int mIncomeTextColor = Color.parseColor("#80cac4");
    public MainInfoListAdapter() {}

    static class InfoViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView mCategory;
        TextView mRemarks;
        TextView mAmount;
        TextView mTime;

        InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.listitem_icon);
            this.mCategory = itemView.findViewById(R.id.listitem_major_category);
            this.mRemarks = itemView.findViewById(R.id.listitem_remarks);
            this.mAmount = itemView.findViewById(R.id.listitem_amount);
            this.mTime = itemView.findViewById(R.id.listitem_time);
        }
    }

    public void setBillRecordList(List<BillRecord> dataList) {
        if (dataList != null) {
            if (mBillRecordList == null) {
                mBillRecordList = new ArrayList<>();
            }
            else {
                if (mBillRecordList.size() != 0) {
                    mBillRecordList.clear();
                }
            }
            mBillRecordList.addAll(dataList);
        }

    }

    public boolean hasDataListSet() {
        return !(mBillRecordList == null);
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_main_listitem, parent, false);
        mContext = parent.getContext();
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        // 测试用
        BillRecord temp = mBillRecordList.get(position);
        String categoryName = temp.getMajorCategory();
        int imgResId = mContext.getSharedPreferences("category_img_res",Context.MODE_PRIVATE).getInt(categoryName, R.drawable.ic_spending_regular);
        holder.icon.setImageResource(imgResId);
        holder.mCategory.setText(categoryName);
        holder.mRemarks.setText(temp.getComment());
        String amountText = temp.getAmount();
        if (!temp.isSpending()) {
            holder.mAmount.setTextColor(mIncomeTextColor);
            amountText = "+" + amountText;
        }
        else {
            holder.mAmount.setTextColor(mSpendingTextColor);
            amountText = "-" + amountText;
        }
        holder.mAmount.setText(amountText);
        holder.mTime.setText(temp.getRecordTime());

        if (mOnBillClickListener != null) {
            holder.itemView.setOnClickListener(view -> {
                mOnBillClickListener.onBillClick(holder.itemView, mBillRecordList.get(holder.getLayoutPosition()));
            });
        }

        if (mOnBillLongClickListener != null) {
            holder.itemView.setOnLongClickListener(view -> {
                  mOnBillLongClickListener.onBillLongClick(mBillRecordList.get(holder.getLayoutPosition()));
                  return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return mBillRecordList.size();
    }

    /**
     * Description: 点击事件设置
    */
    public interface OnBillClickListener {
        void onBillClick(View view, BillRecord billRecord);
    }

    public interface OnBillLongClickListener {
        void onBillLongClick(BillRecord billRecord);
    }

    public void setOnBillClickListener(OnBillClickListener listener) {
        mOnBillClickListener = listener;
    }

    public void setOnBillLongClickListener(OnBillLongClickListener listener) {
        mOnBillLongClickListener = listener;
    }
}
