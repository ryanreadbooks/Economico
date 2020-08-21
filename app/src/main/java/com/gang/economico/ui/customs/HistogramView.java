package com.gang.economico.ui.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.gang.economico.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
* Description: 一个较为通用的简单数值直方图View
* Time: 4/14/2020
*/
public class HistogramView extends View {

    private static final String TAG = "HistogramView";
    private static final int COEFFICIENT = 2;
    private static final int OFFSET = 10;
    private int mWidth, mHeight;
    // 标题
    private String mTitle;
    // 是否展示x轴和y轴
    private boolean mShowAxisX = true, mShowAxisY = true;
    // 填充颜色，选中的颜色
    private int mFilledColor, mSelectedColor, mTitleColor;
    // 文字大小
    private int mTextSize;
    // 顶部显示数值
    private boolean mShowNumTop;
    // y轴能显示的最大的数值范围(0, mRangeY), x轴范围不可设置，而是自动计算出来的, 默认是展示数据的最大值
    private float mRangeY;
    // 展示的方向 false -> 水平(一条条打横画) , true -> 垂直(一条条打竖画)(默认)
    private boolean mOrientation;
    // 每一条的宽度和每一条Bar之间的间隙
    private int mBarWidth, mBarsSpace, mMaxRangeHeight;
    // 坐标原点位置
    private float mOriginX, mOriginY;
    // 那一条Bar被点击了, 一开始等于-1表示没有任何Bar被点击
    private int mSelectedBarNum = -1;
    // 待显示的数据
    private List<Float> mDataList;
    // 画笔
    private Paint mAxesPaint, mBarPaint, mTextPaint;
    // 每条Bar的点击接口
    private OnBarClickListener mBarClickListener;
    // 点击Bar在右上角显示的文字
    private String mSelectedBarTextPrefix;
    private String mSelectedBarTextMiddle;
    private String mSelectedBarTextSuffix;

    public HistogramView(Context context) {
        this(context, null);
    }

    public HistogramView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HistogramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HistogramView);
        mTextSize = (int) typedArray.getDimension(R.styleable.HistogramView_gramTextSize, 30);
        mRangeY = typedArray.getFloat(R.styleable.HistogramView_maxRangeY, 100);
        mFilledColor = typedArray.getColor(R.styleable.HistogramView_filledColor, Color.BLUE);
        mSelectedColor = typedArray.getColor(R.styleable.HistogramView_selectedColor, Color.RED);
        mTitleColor = typedArray.getColor(R.styleable.HistogramView_titleColor, Color.GRAY);
        mOrientation = typedArray.getBoolean(R.styleable.HistogramView_barOrientation, true);
        mShowNumTop = typedArray.getBoolean(R.styleable.HistogramView_showNumOnTop, true);
        mTitle = typedArray.getString(R.styleable.HistogramView_chartTitle);
        typedArray.recycle();

        mAxesPaint = new Paint();
        mAxesPaint.setAntiAlias(true);
        mAxesPaint.setStrokeWidth(2);
        mAxesPaint.setColor(Color.GRAY);

        mBarPaint = new Paint();
        mBarPaint.setAntiAlias(true);
        mBarPaint.setStyle(Paint.Style.FILL);
        mBarPaint.setColor(mFilledColor);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mFilledColor);

        mDataList = new ArrayList<>();
    }

    public void setDisplayData(List<Float> dataList) {
        mDataList = dataList;
        // 默认情况下，展示数据的最大值就是y轴范围的最大值
        mRangeY = Collections.max(dataList);
    }

    public void refreshDisplayData(List<Float> dataList) {
        if (mDataList.size() != 0) {
            mDataList.clear();
        }
        mDataList.addAll(dataList);
        mRangeY = Collections.max(dataList);
        invalidate();
    }

    public void setShowNumTop(boolean flag) {
        mShowNumTop = flag;
    }

    public void setSelectedBarText(String prefix, String middle, String suffix) {
        mSelectedBarTextPrefix = prefix;
        mSelectedBarTextMiddle = middle;
        mSelectedBarTextSuffix = suffix;

    }

    // 初始化一些尺寸参数
    private void initMeasurement() {
        int dataSize = mDataList.size();
        int availableWidth = mWidth - 2 * COEFFICIENT * mTextSize;
        int availableHeight = mHeight - 3 * COEFFICIENT * mTextSize;
        if (mOrientation) {
            mBarWidth = (int) (availableWidth / ((1.5 * dataSize) + (0.5f)));
            mBarsSpace = mBarWidth / 2;
            mMaxRangeHeight = availableHeight - mTextSize * COEFFICIENT;
        }
        else {
            mBarWidth = (int) (availableHeight / ((1.25 * dataSize) + (0.25f)));
            mBarsSpace = mBarWidth / 4;
            mMaxRangeHeight = availableWidth - mTextSize;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: " + mDataList.size());
        if (mDataList == null || mDataList.size() == 0) {
            // 如果数据缺失，则drawText显示暂无数据
            float x = (float) (getWidth() * 0.5);
            float y = (float) (getHeight() * 0.5);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("数据丢失...", x, y, mTextPaint);
            return;
        }
        // 初始化尺寸
        initMeasurement();
        // 绘制坐标轴
        drawAxes(canvas);
        // 绘制标题
        drawTitle(canvas);
        // 绘制Bar
        drawBars(canvas);
    }

    // 绘制Bar
    private void drawBars(Canvas canvas) {
        int dataSize = mDataList.size();
        int left, top, right, bottom;
        boolean isBarSelected = false;
        // 记录有多少天的数据是0
        int nZeros = 0;
        // Log.d(TAG, "drawBars: selected bar num -> " + mSelectedBarNum);
        for (int i = 0; i < dataSize; i++) {
            if (i == mSelectedBarNum) {
                // 如果被点击则变换颜色来画
                mBarPaint.setColor(mSelectedColor);
                isBarSelected = true;
                // 如果被点击，同时还要在右上角显示该条目的详细信息(该条目是那一条，该条目的具体是是多少)
                Paint selectedBarPaint = new Paint();
                selectedBarPaint.setAntiAlias(true);
                selectedBarPaint.setColor(mTitleColor);
                selectedBarPaint.setTextSize((float) (mTextSize));
                selectedBarPaint.setTextAlign(Paint.Align.RIGHT);
                String selectedBarInfo = mSelectedBarTextPrefix + (i + 1) + mSelectedBarTextMiddle + mDataList.get(i) + mSelectedBarTextSuffix;
                float detailX = mWidth - OFFSET - getPaddingRight();
                float detailY =  mTextSize * COEFFICIENT + OFFSET;
                canvas.drawText(selectedBarInfo, detailX, detailY, selectedBarPaint);
            }
            else {
                mBarPaint.setColor(mFilledColor);
                isBarSelected = false;
            }
            if (mOrientation) {
                // 步骤1 偏移mBarsSpace
                // 步骤2 计算Bar所需高度barHeight
                // 步骤2 绘制 mBarWidth宽 和 barHeight高的Rect
                left = (int) (mOriginX + mBarsSpace * (i + 1) + mBarWidth * i);
                right = left + mBarWidth;
                bottom = (int) mOriginY;
                top = bottom - (int) (mDataList.get(i) / mRangeY * mMaxRangeHeight);
                // 绘制顶部文字
                if (mShowNumTop) {
                    if (mDataList.get(i) != 0) {
                        String text = "" + mDataList.get(i);
                        drawTextOnTop(canvas, text, left + mBarWidth / 2, top - OFFSET, true, isBarSelected);
                    }
                }
                // 绘制坐标轴下方的刻度，刻度在这里画方便一点
                if (!(i != 0 && i != dataSize - 1 && i != (dataSize/2))) {
                    mAxesPaint.setTextAlign(Paint.Align.CENTER);
                    mAxesPaint.setTextSize(mTextSize);
                    canvas.drawText((i + 1) + "",left + mBarWidth / 2 , mOriginY + mTextSize + OFFSET, mAxesPaint);
                }
            }
            else {
                left = (int) mOriginX;
                right = left + (int) (mDataList.get(i) / mRangeY * mMaxRangeHeight);
                top = mTextSize * COEFFICIENT * 2 + mBarsSpace * (i + 1) + mBarWidth * i;
                bottom = top + mBarWidth;
                if (mShowNumTop) {
                    String text = "" + mDataList.get(i);
                    drawTextOnTop(canvas, text, right + OFFSET, top + mBarWidth - OFFSET / 2, false, isBarSelected);
                }
            }
            // 如果当天的值是0，则跳过不画这条数据
            if (mDataList.get(i) == 0) {
                nZeros++;
                continue;
            }
            // canvas.drawRect(left, top, right, bottom, mBarPaint);
            canvas.drawRoundRect(left, top, right, bottom, 4, 4, mBarPaint);
        }
        // 所有数据都为0
        if (nZeros == dataSize) {
            float x = (float) (getWidth() * 0.5);
            float y = (float) (getHeight() * 0.5);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("暂无数据...", x, y, mTextPaint);
        }
    }

    // 绘制Bar上方的文字
    private void drawTextOnTop(Canvas canvas, String text, int dx, int dy, boolean flag, boolean isBarSelected) {
        if (flag) {
            mTextPaint.setTextAlign(Paint.Align.CENTER);
        }
        else {
            mTextPaint.setTextAlign(Paint.Align.LEFT);
        }
        if (isBarSelected) {
            mTextPaint.setColor(mSelectedColor);
        }
        else {
            mTextPaint.setColor(mFilledColor);
        }
        canvas.drawText(text, dx, dy, mTextPaint);
    }

    // 绘制坐标轴
    private void drawAxes(Canvas canvas) {
        mOriginX = mTextSize * COEFFICIENT;
        mOriginY = mHeight - mTextSize * COEFFICIENT;
        float endXx = mWidth - mTextSize * COEFFICIENT;
        float endXy = mHeight - mTextSize * COEFFICIENT;
        float endYx = mTextSize * COEFFICIENT;
        float endYy = mTextSize * COEFFICIENT;
        if (mShowAxisX) {
            // 坐标轴本身
            canvas.drawLine(mOriginX, mOriginY, endXx, endXy, mAxesPaint);
        }
        if (mShowAxisY) {
            canvas.drawLine(mOriginX, mOriginY, endYx, endYy, mAxesPaint);
        }
    }

    private void drawTitle(Canvas canvas) {
        if (mTitle.equals("")) {
            return;
        }
        Paint titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setTextSize((float) (mTextSize * 1.5));
        titlePaint.setColor(mTitleColor);
        float titleX = mWidth / 2;
        float titleY = mTextSize * COEFFICIENT + OFFSET;
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mTitle, titleX, titleY, titlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.d(TAG, "onTouchEvent: x -> " + x + " y -> " + y);
        Log.d(TAG, "onTouchEvent: action : " + event.getAction());
        // 发生了点击事件，并且抬起
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int left, top, right, bottom;
            for (int i = 0; i < mDataList.size(); i++) {
                // 分开横向竖向绘制的处理
                if (mOrientation) {
                    left = (int) (mOriginX + mBarsSpace * (i + 1) + mBarWidth * i);
                    right = left + mBarWidth;
                    bottom = (int) mOriginY;
                    top = bottom - (int) (mDataList.get(i) / mRangeY * mMaxRangeHeight);
                }
                else {
                    left = (int) mOriginX;
                    right = left + (int) (mDataList.get(i) / mRangeY * mMaxRangeHeight);
                    top = mTextSize * COEFFICIENT * 2 + mBarsSpace * (i + 1) + mBarWidth * i;
                    bottom = top + mBarWidth;
                }
                if (x >= left && x <= right && y >= top && y <= bottom) {
                    mSelectedBarNum = i;
                    invalidate();
                    // 如果找到了被点击的Bar，直接退出循环，无需再往后寻找被点击的Bar，因为这里规定只能每次选中一条Bar
                    if (mBarClickListener != null) {
                        mBarClickListener.onBarClick(i, mDataList.get(i));
                    }
                    break;
                }
                else {
                    // 而要确定没有Bar被点击，则需要查遍所有Bar看是否被点击，故此处不用break
                    mSelectedBarNum = -1;
                    invalidate();
                }
            }
        }

        return true;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    // 暴露点击接口给外界
    public interface OnBarClickListener {
        void onBarClick(int pos, float value);
    }

    public void setOnBarClickListener(OnBarClickListener listener) {
        mBarClickListener = listener;
    }
}
