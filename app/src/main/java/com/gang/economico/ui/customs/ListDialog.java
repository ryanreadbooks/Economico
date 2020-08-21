package com.gang.economico.ui.customs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gang.economico.R;
import com.gang.economico.entities.CategoryModel;

import java.util.List;

/**
 * 一个带有列表的Dialog 列表由RecyclerView来创建
 * 每一项列表的item包含图标信息和文字信息 可以点击
 */
public class ListDialog extends Dialog {
    public ListDialog(@NonNull Context context) {
        this(context, 0);
    }

    public ListDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    /**
     * 列表项监听器接口
     */
    public interface OnListItemClickListener {
        void onListItemClick(View view, int position);
    }

    /**
     * 内部Builder类用来构建该ListDialog
     */
    public static class Builder {

        private Context mContext;
        private ListDialog mDialog;
        private View mDialogView;

        private TextView mTitleTv;
        private RecyclerView mListRv;

        private ItemAdapter mAdapter;

        @SuppressLint("InflateParams")
        public Builder(Context context) {
            mContext = context;
            mDialog = new ListDialog(context);
            // inflate the layout and find all sub-views
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            mDialogView = inflater.inflate(R.layout.layout_accounts_selection, null ,false);
            mDialog.addContentView(mDialogView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            mTitleTv = mDialogView.findViewById(R.id.accounts_selection_title);
            mListRv = mDialogView.findViewById(R.id.accounts_selection_rv);
            mListRv.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new ItemAdapter();
        }

        public Builder setTitle(String title) {
            mTitleTv.setText(title);
            return this;
        }

        public Builder setTitle(@StringRes int stringResId) {
            mTitleTv.setText(stringResId);
            return this;
        }


        public Builder setItems(List<CategoryModel> dataList) {
            mAdapter.setData(dataList);
            mListRv.setAdapter(mAdapter);
            return this;
        }

        public Builder setListItemClickListener(OnListItemClickListener listener) {
            if (listener != null) {
                mAdapter.setOnItemClickListener(listener);
            }
            return this;
        }

        public ListDialog create() {
            Window dialogWindow = mDialog.getWindow();
            WindowManager windowManager = ((Activity)mContext).getWindowManager();
            assert dialogWindow != null;
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            Display display = windowManager.getDefaultDisplay();
            Point outSize = new Point();
            display.getSize(outSize);
            lp.width = (int) (outSize.x * 0.9);
            dialogWindow.setAttributes(lp);
            dialogWindow.setGravity(Gravity.CENTER);
            dialogWindow.setWindowAnimations(R.style.CommentDialogAnim);
            mDialog.setContentView(mDialogView);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(true);

            return mDialog;
        }
    }

    static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        private List<CategoryModel> mData;
        private OnListItemClickListener mListener;

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            ImageView icon;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.edit_payment_icon);
                title = itemView.findViewById(R.id.edit_payment_name);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_payment_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.title.setText(mData.get(position).getCategoryName());
            String iconResId = mData.get(position).getImgRes();
            if ("".equals(iconResId)) {
                holder.icon.setVisibility(View.GONE);
            }
            else {
                holder.icon.setImageResource(Integer.parseInt(iconResId));
            }
            if (mListener != null) {
                holder.itemView.setOnClickListener(v -> mListener.onListItemClick(holder.itemView, holder.getLayoutPosition()));
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        void setData(List<CategoryModel> data) {
            if (data != null) {
                mData = data;
            }
        }

        void setOnItemClickListener(OnListItemClickListener l) {
            mListener = l;
        }
    }
}

