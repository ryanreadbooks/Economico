package com.gang.economico.ui.customs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.gang.economico.R;
import com.gang.economico.model.StatisticModel;
import com.gang.economico.model.YearlyCategoryStatistic;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Description: 一个较为通用的饼图
 * 可以设置为饼形图或者设置为环形图
 * 饼图的点击事件会使相应部分分离
 * Time: 4/15/2020
*/
public class PieChartView extends View {

    private static final String TAG = "PieChartView";
    private static final float OFFSET = 20;
    // 属性
    private int mSize;
    private float mPieWidth, mExpandedPieWidth;
    private String mTitle;
    private int mTitleColor, mTextColorInside;
    private float mTitleSize;
    private boolean mColorSchema;
    // 数据
    private List<StatisticModel> mData;

    private BigDecimal mTotal;
    private BigDecimal mFullAngle = new BigDecimal("360");

    // 画图相关资源
    private Paint mPiePaint, mTextPaint;
    private RectF mRectF;
    // 监听器
    private OnPieItemClickListener mOnPieItemClickListener;
    // 判断监听用的辅助列表
    private List<Float> mSweepingAngles;
    // 选中的那一项
    private int mSelectedItem = -1;

    public PieChartView(Context context) {
        this(context, null);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView);
        mPieWidth = typedArray.getDimension(R.styleable.PieChartView_pieWidth, 20);
        mExpandedPieWidth = (float) (mPieWidth * 1.3);
        mTitle = typedArray.getString(R.styleable.PieChartView_pieChartTitle);
        mTitleColor = typedArray.getColor(R.styleable.PieChartView_pieChartTitleColor, Color.GREEN);
        mTextColorInside = typedArray.getColor(R.styleable.PieChartView_textColorInside, Color.RED);
        mTitleSize = typedArray.getDimension(R.styleable.PieChartView_pieChartTitleSize, 20);
        // 两种不同的颜色方案
        mColorSchema = typedArray.getBoolean(R.styleable.PieChartView_colorSchema, false);
        typedArray.recycle();

        // 数据初始化
        mTotal = new BigDecimal("0.00");
        // 初始化绘制资源
        mRectF = new RectF();
        mPiePaint = new Paint();
        mPiePaint.setAntiAlias(true);
        mPiePaint.setStyle(Paint.Style.STROKE);
        mPiePaint.setStrokeWidth(mPieWidth);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTitleColor);

        mData = new ArrayList<>();
        mSweepingAngles = new ArrayList<>();
    }

    public void refreshDisplayData(List<StatisticModel> data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData = data;
        Log.d(TAG, "refreshDisplayData: " + mData.size());
        mTotal = new BigDecimal("0.00");
        for (StatisticModel datum : mData) {
            mTotal = mTotal.add(datum.getAmountDecimal());
        }
        mSelectedItem = 0;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mSize = Math.min(width, height);
        setMeasuredDimension(mSize, mSize);
        // 默认选中首个分类
        mSelectedItem = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int mDataSize = mData.size();
        if (mDataSize == 0 || mTotal.floatValue() == 0) {
            float textX = (float) (getWidth() * 0.5);
            float textY = (float) (getWidth() * 0.5 + OFFSET * 2);
            mTextPaint.setTextSize((float) (mTitleSize * 0.75));
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("暂无数据...", textX, textY, mTextPaint);
        }
        else {
            // 每个分类分别绘制
            float leftAndTop = mPieWidth + OFFSET;
            float rightAndBottom = mSize - mPieWidth - OFFSET;
            mRectF.set(leftAndTop, leftAndTop, rightAndBottom, rightAndBottom);
            BigDecimal percentage;
            float sweepAngle = 0;
            float startAngle = -90;
            if (mSweepingAngles.size() != 0) {
                mSweepingAngles.clear();
            }
            for (int i = 0; i < mDataSize; i++) {
                percentage = mData.get(i).getAmountDecimal().divide(mTotal, 5, RoundingMode.HALF_UP);
                sweepAngle = percentage.multiply(mFullAngle, MathContext.DECIMAL128).floatValue();
                if (mSelectedItem == i) {
                    // 某项被选中
                    mPiePaint.setStrokeWidth(mExpandedPieWidth);
                    // 画圆内的中间文字
                    float textX = (float) (getWidth() * 0.5);
                    float textY = (float) (getWidth() * 0.5 + OFFSET * 2);
                    mTextPaint.setTextAlign(Paint.Align.CENTER);
                    DecimalFormat tempFormat = (DecimalFormat) DecimalFormat.getPercentInstance();
                    tempFormat.setMaximumFractionDigits(2);
                    String text = tempFormat.format(percentage.floatValue());
                    // 分类名称
                    mTextPaint.setTextSize(mTitleSize);
                    Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                    float textHeight = fontMetrics.descent - fontMetrics.ascent;
                    float textY2 = textY - textHeight - OFFSET;
                    canvas.drawText(text, textX, textY, mTextPaint);

                    String categoryName = mData.get(i).getCategoryName();
                    mTextPaint.setTextSize((float) (mTitleSize * 0.5));
                    canvas.drawText(categoryName, textX, textY2, mTextPaint);
                }
                else {
                    mPiePaint.setStrokeWidth(mPieWidth);
                }
                // 绘制的颜色
                int changing = 128 / mDataSize;
                int changing2 = 30 / mDataSize;
                int changing3 = 200 / mDataSize;
                if (!mColorSchema) {
                    mPiePaint.setARGB(changing3 * (mDataSize - i),changing * (mDataSize - i), 185, changing2 * (i + 1));
                }
                else {
                    mPiePaint.setARGB(changing3 * (mDataSize - i),230, changing * (mDataSize - i), changing2 * (i + 1));
                }
                mSweepingAngles.add(sweepAngle);
                // 画圆
                canvas.drawArc(mRectF, startAngle, sweepAngle, false, mPiePaint);
                startAngle += sweepAngle;
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float dx = event.getX();
        float dy = event.getY();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // 找到是哪一个部分被点击了
            // 先判断是否在位置被点击(圆外(无效) 圆上 圆内)
            if (isClickInOrOnCircle(dx, dy)) {
                // 再找出被点击的位置的角度
                float clickAngle = calculateClickAngle(dx, dy);
                // 遍历每个判断角度位置
                float startAngle = 0;
                float regionAngle = 0;
                for (int i = 0; i < mSweepingAngles.size(); i++) {
                    regionAngle += mSweepingAngles.get(i);
                    if (clickAngle >= startAngle && clickAngle <= regionAngle) {
                        mSelectedItem = i;
                        Log.d(TAG, "onTouchEvent: item " + i + " is clicked");
                        invalidate();
                        if (mOnPieItemClickListener != null) {
                            mOnPieItemClickListener.onPieItemClick(i);
                        }
                        break;
                    }
                    else {
                        startAngle += mSweepingAngles.get(i);
                        mSelectedItem = -1;
                    }
                }
            }
            // 没有item被选中 且当前已经有item被选中 点击圆外区域取消选中状态
            else {
                if (mSelectedItem != -1) {
                    mSelectedItem = -1;
                    invalidate();
                }
            }
        }
        return true;

    }

    /**
     * Description: 判断点击的位置是否在圆上或者圆内
    */
    private boolean isClickInOrOnCircle(float dx, float dy) {
        float origin = (float) (getWidth() * 0.5);
        float deltaX = Math.abs(origin - dx);
        float deltaY = Math.abs(origin - dy);
        float distance = (float) (Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
        distance = (float) Math.sqrt(distance);
        float radius = origin - mPieWidth + OFFSET;  // 此处并非真正半径，为了适应性更强，允许些许偏差
        Log.d(TAG, "click distance: " + distance + " radius: " + radius);
        return distance <= radius;
    }

    /**
     * Description: 计算点击位置的角度(原点位置: 系统的-90°位置)
    */
    private float calculateClickAngle(float dx, float dy) {
        // 分开四个象限
        float origin = (float) (getWidth() * 0.5);
        float aTan = 0;
        // 第一象限
        if (dx > origin && dy < origin) {
            float deltaX = dx - origin;
            float deltaY = origin - dy;
            aTan = (float) (Math.atan(deltaX / deltaY) * 180 / Math.PI);
        }
        // 第二象限
        else if (dx < origin && dy < origin) {
            float deltaX = origin - dx;
            float deltaY = origin - dy;
            float aTanTemp = (float) (Math.atan(deltaY / deltaX) * 180 / Math.PI);
            aTan = aTanTemp + 270;
        }
        // 第三象限
        else if (dx < origin && dy > origin) {
            float deltaX = origin - dx;
            float deltaY = dy - origin;
            float aTanTemp = (float) (Math.atan(deltaX / deltaY) * 180 / Math.PI);
            aTan = aTanTemp + 180;
        }
        // 第四象限
        else if (dx > origin && dy > origin) {
            float deltaX = dx - origin;
            float deltaY = dy - origin;
            float aTanTemp = (float) (Math.atan(deltaY / deltaX) * 180 / Math.PI);
            aTan = aTanTemp + 90;
        }
        Log.d(TAG, "final aTan: " + aTan);
        return aTan;
    }

    public interface OnPieItemClickListener{
        void onPieItemClick(int pos);
    }

    public void setOnPieItemClickListener(OnPieItemClickListener listener) {
        mOnPieItemClickListener = listener;
    }

    public void setColorSchema(boolean colorSchema) {
        mColorSchema = colorSchema;
    }
}
