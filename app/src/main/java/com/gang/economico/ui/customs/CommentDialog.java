package com.gang.economico.ui.customs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gang.economico.R;

/*
* Description: 备注填写的一个Dialog
* Time: 4/12/2020
*/
public class CommentDialog extends Dialog {
    public CommentDialog(@NonNull Context context) {
        this(context, R.style.CommentDialogTheme);
    }

    public CommentDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    /*
    * Description: 创建Dialog的内部Builder类
    */
    public static class Builder {

        private Context mContext;
        private View mDialogView;
        private CommentDialog mDialog;
        private int mIconRes;
        private String mTitle;
        private String mCancelStr;
        private String mConfirmStr;
        private ImageView mIvIcon;
        private TextView mTvTitle, mTvCancel, mTvConfirm;
        private GangEditText mEditInput;
        private OnCancelClickListener mCancelClickListener;
        private OnConfirmClickListener mConfirmClickListener;

        public Builder(Context context) {
            // 初始化
            mContext = context;
            mDialog = new CommentDialog(mContext);
            // 加载Dialog的布局
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDialogView = inflater.inflate(R.layout.layout_comment_dialog, null ,false);
            mDialog.addContentView(mDialogView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            mIvIcon = mDialogView.findViewById(R.id.comment_dialog_icon);
            mTvTitle = mDialogView.findViewById(R.id.comment_dialog_title);
            mTvCancel = mDialogView.findViewById(R.id.dialog_cancel);
            mTvConfirm = mDialogView.findViewById(R.id.dialog_confirm);
            mEditInput = mDialogView.findViewById(R.id.comment_dialog_input);

        }

        public Builder setIcon(int iconRes) {
            mIconRes = iconRes;
            mIvIcon.setImageResource(iconRes);
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            mTvTitle.setText(mTitle);
            return this;
        }

        public Builder setCancel(String cancelStr, OnCancelClickListener listener) {
            mCancelStr = cancelStr;
            mTvCancel.setText(mCancelStr);
            mCancelClickListener = listener;
            if (mCancelClickListener != null) {
                mTvCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCancelClickListener.onCancelClick(mCancelStr);
                        mDialog.dismiss();
                    }
                });
            }
            return this;
        }

        public Builder setDefaultText(String defaultText) {
            mEditInput.setText(defaultText);
            return this;
        }

        public Builder setEditHint(String hint) {
            mEditInput.setHint(hint);
            return this;
        }

        public Builder setEditInputType(int type) {
            mEditInput.setInputType(type);
            return this;
        }

        public Builder setEditMaxLength(int maxLength) {
            InputFilter[] filters = {new InputFilter.LengthFilter(maxLength)};
            mEditInput.setFilters(filters);
            mEditInput.setEditMaxLength(maxLength);
            return this;
        }

        public Builder setConfirm(String confirmStr, OnConfirmClickListener listener) {
            mConfirmStr = confirmStr;
            mTvConfirm.setText(mConfirmStr);
            mConfirmClickListener = listener;
            if (mConfirmClickListener != null) {
                mTvConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mConfirmClickListener.onConfirmClick(mEditInput, mConfirmStr);
                        mDialog.dismiss();
                    }
                });
            }
            return this;
        }

        public interface OnCancelClickListener {
            void onCancelClick(String cancelStr);
        }

        public interface OnConfirmClickListener {
            void onConfirmClick(EditText editText, String confirmStr);
        }

        public CommentDialog create() {
            // 进一步初始化Dialog
            Window dialogWindow = mDialog.getWindow();
            // 整个屏幕的WindowManager
            WindowManager windowManager = ((Activity)mContext).getWindowManager();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            // 令Dialog的宽度为屏幕宽度的85%
            lp.width = (int) (size.x * 0.9);
            // 设置Dialog放置位置
            dialogWindow.setGravity(Gravity.CENTER);
            dialogWindow.setAttributes(lp);
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialogWindow.setWindowAnimations(R.style.CommentDialogAnim);
            // 设置能够取消这个Dialog
            mDialog.setContentView(mDialogView);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(true);
            mEditInput.requestFocus();
            return mDialog;
        }
    }
}
