package com.gang.economico.ui.customs;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gang.economico.R;
import com.gang.economico.entities.CategoryModel;

import java.util.ArrayList;
import java.util.List;

/*
* Description: 类别显示的自定义view，在编辑的界面中使用
* Time: 4/8/2020
*/
public class CategoryDisplayView extends ViewGroup {

    private static final int DEFAULT_SPACING = 15;
    private int mItemSpacingVer;                            // 每一项的垂直间隔
    private int mItemSpacingHor;                            // 每一项的水平间隔
    private int mItemWidth;
    private int mItemHeight;
    private int mWidth;
    private int mHeight;
    private List<CategoryModel> mCategoryModels;            // 资源集合
    private List<List<View>> lines;                         // 每一行的集合
    private OnItemClickListener mItemClickListener = null;

    public CategoryDisplayView(Context context) {
        this(context, null);
    }

    public CategoryDisplayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CategoryDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        lines = new ArrayList<>();
        // 获取自定义属性中的垂直间隔
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CategoryDisplayView);
        mItemSpacingVer = (int) typedArray.getDimension(R.styleable.CategoryDisplayView_itemSpacingVer, DEFAULT_SPACING);
        typedArray.recycle();
    }

    /**
     * 方法功能: 给外部提供显示数据传入的接口
     *
     * 功能实现原理:
     **/
    public void setCategoryResource(List<CategoryModel> categoryResource) {
        mCategoryModels = categoryResource;
        for (final CategoryModel model: mCategoryModels){
            // 获取布局
            final View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_category_item, this, false);
            // 找到这个子view中的两个控件，为ImageView和TextView
            final ImageView categoryImg = itemView.findViewById(R.id.category_icon);
            TextView categoryText = itemView.findViewById(R.id.category_name);
            categoryImg.setImageResource(model.getImgResInt());
            categoryText.setText(model.getCategoryName());
            // 设置监听事件
            if (mItemClickListener != null) {
                itemView.setOnClickListener(v -> {
                    mItemClickListener.onItemClick(model.getImgRes(), model.getCategoryName());
                    // 设置点击动画 --> 旋转35°后转回来
                    ObjectAnimator animator = ObjectAnimator.ofFloat(categoryImg,
                            "rotation", 0, 35, 0)
                            .setDuration(500);
                    animator.start();
                });
            }
            addView(itemView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        List<View> line = null;
        lines.clear();
        mWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        mHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
       mItemWidth = getChildAt(0).getMeasuredWidth();
        mItemHeight = getChildAt(0).getMeasuredHeight();
        mItemSpacingHor = (mWidth - (mItemWidth * 6)) / 7;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            // 如果为空行，则先创建一行
            if (line == null) {
                line = createNewLine(view);
            }
            else {
                // 一行只摆6个子view
                if (i % 6 != 0) {
                    // 这一行为满，添加子view
                    line.add(view);
                }
                else {
                    // 这一行已满，创建新的一行
                    line = createNewLine(view);
                }
            }
        }
        // 测量自己
        // 高度 = 每一个子view的高度 * 行数 + (行数 + 1) * 每行的间隔
        mHeight = mItemHeight * lines.size() + (lines.size() + 1) * mItemSpacingVer;
        setMeasuredDimension(mWidth, mHeight);
    }

    private List<View> createNewLine(View view) {
        List<View> line = new ArrayList<>();
        line.add(view);
        lines.add(line);
        return line;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = mItemSpacingVer;
        for (List<View> line: lines) {
            int left = mItemSpacingHor;
            for (View view: line) {
                int right = mItemWidth + left;
                int bottom = mItemHeight + top;
                view.layout(left, top, right, bottom);
                left += mItemWidth + mItemSpacingHor;
            }
            top += mItemHeight + mItemSpacingVer;
        }
    }

    // 外部设置监听事件
    public interface OnItemClickListener {
        void onItemClick(String categoryImgRes, String categoryName);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void clearData() {
        if (mCategoryModels != null) {
            mCategoryModels.clear();
            mCategoryModels = null;
        }
        if (lines != null) {
            lines = null;
        }
    }
}
