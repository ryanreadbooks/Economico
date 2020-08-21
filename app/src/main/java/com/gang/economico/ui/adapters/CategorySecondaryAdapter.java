package com.gang.economico.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.entities.BillRecord;

import java.util.List;

public class CategorySecondaryAdapter extends RecyclerView.Adapter<CategorySecondaryAdapter.ViewHolder> {

    private static final String TAG = "CategorySecondary";
    private List<BillRecord> mSecondaryList;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView amountTv;
        TextView commentTv;
        ImageView opeBtn;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTv = itemView.findViewById(R.id.category_secondary_amount);
            commentTv = itemView.findViewById(R.id.category_secondary_comment);
            opeBtn = itemView.findViewById(R.id.category_secondary_btn);
        }
    }

    public CategorySecondaryAdapter() {}

    public void setSecondaryList(List<BillRecord> secondaryList) {
        mSecondaryList = secondaryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_categorized_secondary_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.amountTv.setText(mSecondaryList.get(position).getAmount());
        holder.commentTv.setText(mSecondaryList.get(position).getComment());
        // holder.opeBtn.setImageResource(R.drawable.ic_spending_regular);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(position, mSecondaryList.get(position)));
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                mOnItemLongClickListener.onItemLongClick(position, mSecondaryList.get(position));
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return mSecondaryList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, BillRecord billRecord);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int pos, BillRecord billRecord);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        mOnItemLongClickListener = l;
    }

    public void clearData(){
        if (mSecondaryList != null) {
            mSecondaryList.clear();
            mSecondaryList = null;
        }
        if (mOnItemClickListener != null) {
            mOnItemClickListener = null;
        }
        if (mOnItemLongClickListener != null) {
            mOnItemLongClickListener = null;
        }
    }
}
