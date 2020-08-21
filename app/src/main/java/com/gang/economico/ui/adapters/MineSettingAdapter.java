package com.gang.economico.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.model.MineSetting;

import java.util.List;

public class MineSettingAdapter extends RecyclerView.Adapter<MineSettingAdapter.ViewHolder> {

    private List<MineSetting> mSettingModels;
    private OnItemClickListener mOnItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle;
        TextView itemDescription;
        ImageView itemIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.mine_setting_item_title);
            itemDescription = itemView.findViewById(R.id.mine_setting_item_desp);
            itemIcon = itemView.findViewById(R.id.mine_setting_item_icon);
        }
    }

    public void setSettingModels(List<MineSetting> settingModels) {
        mSettingModels = settingModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mine_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemTitle.setText(mSettingModels.get(position).getTitle());
        holder.itemDescription.setText(mSettingModels.get(position).getDescription());
        holder.itemIcon.setImageResource(mSettingModels.get(position).getIconResId());
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                mOnItemClickListener.onItemClick(pos, mSettingModels.get(pos).getTitle());
            });
        }
    }

    @Override
    public int getItemCount() {
        return mSettingModels.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, String name);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

}
