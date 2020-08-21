package com.gang.economico.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.entities.BillRecord;

import java.util.ArrayList;
import java.util.List;

public class CategoryPrimaryAdapter extends RecyclerView.Adapter<CategoryPrimaryAdapter.ViewHolder> {

    private static final String TAG = "CategoryPrimaryAdapter";
    private List<List<BillRecord>> mLists;
    private CategorySecondaryAdapter mSecondaryAdapter;
    private OnItemClickListener mOnItemClickListener;
    private OnInnerListItemClickListener mOnInnerClickListener;
    private OnInnerListItemLongClickListener mOnInnerLongClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView dateTv;
        RecyclerView secondaryRv;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTv = itemView.findViewById(R.id.category_list_item_date);
            secondaryRv = itemView.findViewById(R.id.category_secondary_list);
            secondaryRv.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }

    public CategoryPrimaryAdapter() {
    }

    public void setPrimaryLists(List<List<BillRecord>> newList) {
        mLists = new ArrayList<>();
        if (mLists.size() != 0) {
            mLists.clear();
        }
        mLists.addAll(newList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_categorized_primary_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String time = mLists.get(position).get(0).getRecordTime();
        holder.dateTv.setText(time);
        mSecondaryAdapter = new CategorySecondaryAdapter();
        holder.secondaryRv.setAdapter(mSecondaryAdapter);
        if (mOnInnerClickListener != null) {
            mSecondaryAdapter.setOnItemClickListener((pos, billRecord) -> {
                mOnInnerClickListener.onInnerItemClick(billRecord);
            });
        }

        if (mOnInnerLongClickListener != null) {
            mSecondaryAdapter.setOnItemLongClickListener((pos, billRecord) -> {
                mOnInnerLongClickListener.onInnerLongClick(billRecord);
            });
        }

        mSecondaryAdapter.setSecondaryList(mLists.get(position));
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                Log.d(TAG, "onBindViewHolder: " + position);
                mOnItemClickListener.onItemClick(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    // 外层Item点击事件的监听
    public void setOutsideItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    // 嵌套的RecyclerView的点击事件监听
    public interface OnInnerListItemClickListener {
        void onInnerItemClick(BillRecord billRecord);
    }

    public interface OnInnerListItemLongClickListener {
        void onInnerLongClick(BillRecord billRecord);
    }

    public void setOnInnerItemClickListener(OnInnerListItemClickListener l) {
        mOnInnerClickListener = l;
    }

    public void setOnInnerLongClickListener(OnInnerListItemLongClickListener l) {
        mOnInnerLongClickListener = l;
    }

    public void clearData() {
        if (mLists != null) {
            mLists.clear();
            mLists = null;
        }
        if (mOnItemClickListener != null) {
            mOnItemClickListener = null;
        }
        if (mOnInnerClickListener != null) {
            mOnInnerClickListener = null;
        }
        if (mOnInnerLongClickListener != null) {
            mOnInnerLongClickListener = null;
        }
        if (mSecondaryAdapter != null) {
            mSecondaryAdapter.clearData();
            mSecondaryAdapter = null;
        }
    }
}
