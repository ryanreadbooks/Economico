package com.gang.economico.ui.customs;

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

import com.gang.economico.R;

public class CustomaryDialog extends Dialog {

    public CustomaryDialog(@NonNull Context context) {
        this(context, R.style.CommentDialogTheme);
    }

    public CustomaryDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {

        private Context mContext;
        private View mView;
        private CustomaryDialog mDialog;
        private TextView mTitleTv;
        private TextView mContentTv;
        private TextView mCancelTv;
        private TextView mConfirmTv;
        private ImageView mIconIv;
        private OnCancelClickListener mCancelListener;
        private OnConfirmClickListener mConfirmListener;

        public Builder(Context context) {
            // 初始化
            mContext = context;
            mDialog = new CustomaryDialog(mContext);
            // 加载布局
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = inflater.inflate(R.layout.layout_customary_dialog, null, false);
            mDialog.addContentView(mView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            mTitleTv = mView.findViewById(R.id.dialog_title);
            mContentTv = mView.findViewById(R.id.dialog_content);
            mCancelTv = mView.findViewById(R.id.dialog_cancel);
            mConfirmTv = mView.findViewById(R.id.dialog_confirm);
            mIconIv = mView.findViewById(R.id.dialog_icon);

        }

        public Builder setIcon(int iconId) {
            if (iconId == 0) {
                mIconIv.setVisibility(View.GONE);
            }
            else {
                mIconIv.setImageResource(iconId);
            }
            return this;
        }

        public Builder setTitle(String title) {
            mTitleTv.setText(title);
            return this;
        }

        public Builder setTitleColor(int color) {
            mTitleTv.setTextColor(color);
            return this;
        }

        public Builder setContent(String content) {
            mContentTv.setText(content);
            return this;
        }

        public Builder setContentColor(int color) {
            mContentTv.setTextColor(color);
            return this;
        }

        public Builder setCancel(String cancelStr, OnCancelClickListener listener) {
            mCancelTv.setText(cancelStr);
            mCancelListener = listener;
            if (mCancelListener != null) {
                mCancelTv.setOnClickListener(v -> {
                    mCancelListener.onCancel(mDialog);
                    mDialog.dismiss();
                });
            }
            return this;
        }

        public Builder setConfirm(String confirmStr, OnConfirmClickListener listener) {
            mConfirmTv.setText(confirmStr);
            mConfirmListener = listener;
            if (mConfirmListener != null) {
                mConfirmTv.setOnClickListener(v -> {
                    mConfirmListener.onConfirm(mDialog);
                    mDialog.dismiss();
                });
            }
            return this;
        }

        public Builder setCancelColor(int color) {
            mCancelTv.setTextColor(color);
            return this;
        }

        public Builder setConfirmColor(int color) {
            mConfirmTv.setTextColor(color);
            return this;
        }

        public interface OnCancelClickListener {
            void onCancel(Dialog dialog);
        }

        public interface OnConfirmClickListener {
            void onConfirm(Dialog dialog);
        }

        public CustomaryDialog create() {
            // 进一步初始化Dialog
            Window dialogWindow = mDialog.getWindow();
            // 整个屏幕的WindowManager
            WindowManager windowManager = ((Activity)mContext).getWindowManager();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            // 令Dialog的宽度为屏幕宽度的90%
            lp.width = (int) (size.x * 0.9);
            // 设置Dialog放置位置
            dialogWindow.setGravity(Gravity.CENTER);
            dialogWindow.setAttributes(lp);
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialogWindow.setWindowAnimations(R.style.CommentDialogAnim);
            // 设置能够取消这个Dialog
            mDialog.setContentView(mView);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(true);

            return mDialog;
        }
    }
}
