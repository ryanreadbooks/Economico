package com.gang.economico.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.model.AccountModel;
import com.gang.economico.viewmodels.AccountsViewModel;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> {

    private static final String TAG = "AccountsAdapter";
    private List<AccountModel> mModelList;
    private Context mContext;
    private int mFlag = AccountsViewModel.QUERY_SPENDING;
    // 金额显示的格式
    private final DecimalFormat mDecimalFormat;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mAccountIcon;
        TextView mAccountName;
        TextView mAccountAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAccountIcon = itemView.findViewById(R.id.mine_accounts_rv_item_icon);
            mAccountName = itemView.findViewById(R.id.mine_accounts_rv_name);
            mAccountAmount = itemView.findViewById(R.id.mine_accounts_rv_amount);
        }
    }

    public AccountsAdapter(Context context) {
        if (mModelList == null) {
            mModelList = new ArrayList<>();
        }
        mContext = context;
        mDecimalFormat = new DecimalFormat("0.00");
    }

    public void setFlag(int flag) {
        mFlag = flag;
    }

    public void setAccountList(List<AccountModel> modelList) {
        if (modelList == null) {
            Log.d(TAG, "setAccountList: null" + modelList);
            mModelList = new ArrayList<>();
        }
        else {
            mModelList = modelList;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mine_accounts_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String accountName = mModelList.get(position).getAccountName();
        String amount = mDecimalFormat.format(new BigDecimal(mModelList.get(position).getAccountAmount()));
        int iconIconRes = mContext.getSharedPreferences("category_img_res",Context.MODE_PRIVATE).getInt(accountName, R.drawable.ic_spending_regular );
        holder.mAccountIcon.setImageResource(iconIconRes);
        holder.mAccountName.setText(accountName);
        if (mFlag == AccountsViewModel.QUERY_INCOME) {
            holder.mAccountAmount.setTextColor(mContext.getColor(R.color.colorSecondaryAccent));
        }
        else {
            holder.mAccountAmount.setTextColor(mContext.getColor(R.color.color_text_amount_white));
        }
        holder.mAccountAmount.setText(amount);
    }

    @Override
    public int getItemCount() {
        return mModelList.size();
    }

}
