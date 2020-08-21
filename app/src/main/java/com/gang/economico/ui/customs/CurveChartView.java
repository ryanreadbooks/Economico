package com.gang.economico.ui.customs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.gang.economico.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 曲线图
 */
public class CurveChartView extends View {

    private static final String TAG = "CurveChartView";
    private static final int VERTICAL_OFFSET = 20;
    private static final int HORIZONTAL_OFFSET = 15;

    private List<Float> mData;
    private float mMaxData;

    private float mLineWidth;
    private int mLineColor;
    private boolean lineSmooth;
    private String mTitle;
    private int mTitleColor;
    private float mTitleSize;
    private float mTitleVerticalSpace;

    private Paint mLinePaint;
    private Paint mTextPaint;
    private CornerPathEffect mPathEffect;
    // 线的路径
    private Path mPath;
    private Path mDstPath;
    private PathMeasure mPathMeasure;
    private float mAnimatorValue;
    private float mPathLength;

    // 动画
    private ValueAnimator mAnimator;

    private float mAvailableWidth, mAvailableHeight;    // 可以用作画图的宽高
    private float mOriginX, mOriginY;                   // 画布原点转换

    public CurveChartView(Context context) {
        this(context, null);
    }

    public CurveChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 属性获取
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CurveChartView);
        mLineWidth = typedArray.getDimension(R.styleable.CurveChartView_lineWidth, 20);
        mLineColor = typedArray.getColor(R.styleable.CurveChartView_lineColor, Color.BLUE);
        lineSmooth = typedArray.getBoolean(R.styleable.CurveChartView_smooth, true);
        mTitle = typedArray.getString(R.styleable.CurveChartView_curveChartTitle);
        mTitleColor = typedArray.getColor(R.styleable.CurveChartView_curveTitleColor, Color.RED);
        mTitleSize = typedArray.getDimension(R.styleable.CurveChartView_curveTitleSize, 20);
        typedArray.recycle();
        // 初始化
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStyle(Paint.Style.STROKE);
        if (lineSmooth) {
            mPathEffect = new CornerPathEffect(35);
        }
        mLinePaint.setPathEffect(mPathEffect);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTitleColor);
        mTextPaint.setTextSize(mTitleSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mPath = new Path();
        mDstPath = new Path();
        mPathMeasure = new PathMeasure();

        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(2000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();

        mData = new ArrayList<>();
    }

    public void setData(List<Float> l) {
        if (l != null) {
            if (mData == null) {
                mData = new ArrayList<>();
            }
            else {
                if (mData.size() != 0) {
                    mData.clear();
                }
                mData.addAll(l);
                mMaxData = Collections.max(mData);
            }
            // 先清楚原先已经画了的path 否则则不能覆盖
            mPath.reset();
            invalidate();
        }
    }

    public void setTitle(String newTitle) {
        mTitle = newTitle;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float width = MeasureSpec.getSize(widthMeasureSpec);
        float height = MeasureSpec.getSize(heightMeasureSpec);
        mTitleVerticalSpace = mTextPaint.descent() - mTextPaint.ascent() + VERTICAL_OFFSET;
        mAvailableHeight = height - mTitleVerticalSpace * 2;
        mAvailableWidth = width - mTitleVerticalSpace * 2;
        mOriginX = mTitleVerticalSpace;
        mOriginY = height - mTitleVerticalSpace;
        Log.d(TAG, "onMeasure: width " + width);
        Log.d(TAG, "onMeasure: height " + height);
        Log.d(TAG, "onMeasure: mAvailableHeight " + mAvailableHeight);
        Log.d(TAG, "onMeasure: mAvailableWidth " + mAvailableWidth);
        Log.d(TAG, "onMeasure: max - >" + mMaxData);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Log.d(TAG, "onDraw: width ->" + getWidth() + " height -> " + getHeight());
        // 最大值如果都是0 那么肯定列表里的所有数据都是零 也就是没有数据
        if (mData == null || mData.size() == 0 || mMaxData == 0) {
            mTextPaint.setTextSize((float) (mTitleSize * 1.5));
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            float textX = (float) (getWidth() * 0.5);
            float textY = (float) (getHeight() * 0.5 + VERTICAL_OFFSET * 3);
            canvas.drawText("暂无数据...", textX, textY - VERTICAL_OFFSET, mTextPaint);
            Log.d(TAG, "onDraw: null");
            return;
        }
        super.onDraw(canvas);
        // 画标题
        drawTitle(canvas);
        // 画坐标轴
        drawAxes(canvas);
        int dataSize = mData.size();
        // 计算每个数据之间的间隔
        float spacing = mAvailableWidth / (dataSize + 1);
        float rulerTextY = (float) (mOriginY + mTitleVerticalSpace * 0.5);
        // 设置path路径
        // 起点为第一个数据点
        float startX = mOriginX + spacing;
        float tempCoefficient = (float) (mAvailableHeight / mMaxData * 0.95);
        float startHeight = tempCoefficient * mData.get(0);
        float startY = mOriginY - startHeight;
        // 第一个x轴刻度
        drawRulerX(canvas, "1月", startX, rulerTextY);
        mPath.moveTo(startX, startY); // 起点移动到第一个点
        // 这种用的是相对画法
//        for (int i = 1; i < dataSize; i++) {
//            // 下一个点的高度
//            float nextHeight = tempCoef * mData.get(i);
//            // 与上一个点的高度差
//            float nextY = startHeight - nextHeight;
//            // 移动到下一个点
//            Log.d(TAG, "onDraw: nextX -> " + spacing + " nextY -> " + nextY);
//            mPath.rLineTo(spacing, nextY);
//            startHeight = nextHeight;
//        }
        float nextY = 0;
        float nextX = startX;
        for (int i = 1; i < dataSize; i++) {
            nextY = mOriginY - tempCoefficient * mData.get(i);
            nextX += spacing;
            mPath.lineTo(nextX, nextY);
            // 画x轴刻度
            drawRulerX(canvas, (i+1)+"月", nextX, rulerTextY);
        }
        mPathMeasure.setPath(mPath, false);
        mPathLength = mPathMeasure.getLength();
        // Log.d(TAG, "onDraw: length of path " + mPathLength);

        // 实现第一次加载时的动画效果
        float stop = mPathLength * mAnimatorValue;
        mDstPath.reset();
        mDstPath.rLineTo(0, 0);
        mPathMeasure.getSegment(0, stop, mDstPath, true);
        canvas.drawPath(mDstPath, mLinePaint);

        // canvas.drawPath(mPath, mLinePaint);
    }

    private void drawAxes(Canvas canvas) {
        // x轴
        canvas.drawLine(mOriginX, mOriginY, getWidth() - mTitleVerticalSpace, mOriginY, mTextPaint);
        // y轴
        canvas.drawLine(mOriginX, mOriginY, mOriginX, mOriginY - mAvailableHeight, mTextPaint);
        // canvas.drawLine(mOriginX, mOriginY - mAvailableHeight, getWidth() - mTitleVerticalSpace, mOriginY - mAvailableHeight, mTextPaint);
    }

    private void drawTitle(Canvas canvas) {
        mTextPaint.setTextSize(mTitleSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        float textX = (float) getWidth() / 2;
        canvas.drawText(mTitle, textX, mTitleVerticalSpace, mTextPaint);
    }

    /**
     * 画横坐标刻度
     * @param canvas 画布
     * @param text 文字
     * @param x 文字位置 x
     * @param y 文字位置 y
     */
    private void drawRulerX(Canvas canvas, String text, float x, float y) {
        mTextPaint.setTextSize((float) (mTitleSize * 0.55));
        canvas.drawText(text, x, y, mTextPaint);

    }
}
