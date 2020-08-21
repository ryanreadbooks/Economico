package com.gang.economico.ui.customs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.gang.economico.R;
import com.google.android.material.tabs.TabLayout;

import java.util.List;


/**
 * Description: 一个相当于DatePicker的Dialog, 但只是能够选择年和月的DatePicker的Dialog
 * Time: 4/16/2020
*/
public class YearMonthPickerDialog extends Dialog {

    public YearMonthPickerDialog(@NonNull Context context) {
        this(context, R.style.CustomDatePickerTheme);
    }

    public YearMonthPickerDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

    }

    /**
     * Description: 创建这个Dialog的Builder
    */
    public static class Builder {

        public static final String TAG = "YearMonthPickerDialog builder";
        private TabLayout mYearSelectionTab;
        private List<String> mYearList;
        private ViewGroup mMonthContent;
        private View mView;
        private Context mContext;
        private YearMonthPickerDialog mDialog;
        private OnYearMonthSetListener mOnYearMonthSetListener;

        public Builder(Context context) {
            mContext = context;
            mDialog = new YearMonthPickerDialog(context);

            mView = LayoutInflater.from(context).inflate(R.layout.layout_datepicker_dialog, null, false);
            mYearSelectionTab = mView.findViewById(R.id.year_selection_tab);
            mMonthContent = mView.findViewById(R.id.month_content);
            mDialog.addContentView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        public Builder setYearTab(List<String> yearList) {
            mYearList = yearList;
            // 设置TabLayout的标题
            for (String yearTitle: mYearList) {
                mYearSelectionTab.addTab(mYearSelectionTab.newTab().setText(yearTitle));
            }
            return this;
        }



        public YearMonthPickerDialog create() {
            // 设置Dialog的属性
            Window dialogWindow = mDialog.getWindow();
            WindowManager windowManager = ((Activity)mContext).getWindowManager();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            // dialog的宽度，占据屏幕的90%
            lp.width = (int) (size.x * 0.9);
            // dialogWindow.setGravity(Gravity.CENTER);
            dialogWindow.setAttributes(lp);
            dialogWindow.setWindowAnimations(R.style.YearMonthPickerAnim);

            mDialog.setContentView(mView);
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.setCancelable(true);

            return mDialog;
        }

        public interface OnYearMonthSetListener {
            void onYearMonthSet(String year, int month);
        }

        public Builder setOnYearMonthSetListener(OnYearMonthSetListener listener) {
            mOnYearMonthSetListener = listener;

            if (mOnYearMonthSetListener != null) {
                for (int i = 0; i < mMonthContent.getChildCount(); i++) {
                    final int finalMonth = i + 1;
                    mMonthContent.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnYearMonthSetListener.onYearMonthSet(mYearList.get(mYearSelectionTab.getSelectedTabPosition()), finalMonth);
                            // 在MainActivity里面来dismiss，如果选择了不正确的月份，该dialog不会消失，而是等待用户选择正确的月份或者取笑
                            // mDialog.dismiss();
                        }
                    });
                }
            }

            return this;
        }
    }
}
