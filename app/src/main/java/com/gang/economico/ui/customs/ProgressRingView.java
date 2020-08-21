package com.gang.economico.ui.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.gang.economico.R;

/*
* Description: 该View是一个环状的进度条
*               此处的用途是用来展现预算的使用情况
* Time: 4/13/2020
*/
public class ProgressRingView extends View {

    private static final int OFFSET = 10;
    private static final String TAG = "RING";
    // 控件宽高
    private int mWidth, mHeight;
    // 圆环宽度
    private int mRingWidth;
    // 圆环内字体大小
    private int mTextSize;
    // 颜色
    private int mRingBackgroundColor, mRingNormalColor, mRingWarningColor, mRingErrorColor;
    // 颜色变换的比例
    private float mWarningPercentage, mErrorPercentage;
    // 最大值和当前值
    private float mMaxValue = 0.0f, currentValue;

    private RectF mRectF;
    private Rect mTextBounds;
    private Paint mTextPaint, mRingBackgroundPaint, mRingForegroundPaint;


    public ProgressRingView(Context context) {
        this(context, null);
    }

    public ProgressRingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取设置属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressRingView);
        mRingWidth = (int) typedArray.getDimension(R.styleable.ProgressRingView_ringWidth, 10);
        mTextSize = (int) typedArray.getDimension(R.styleable.ProgressRingView_textSizeInRing, 15);
        mRingBackgroundColor = typedArray.getColor(R.styleable.ProgressRingView_ringBackgroundColor, Color.GRAY);
        mRingNormalColor = typedArray.getColor(R.styleable.ProgressRingView_ringNormalColor, Color.GREEN);
        mRingWarningColor = typedArray.getColor(R.styleable.ProgressRingView_ringWarningColor, Color.YELLOW);
        mRingErrorColor = typedArray.getColor(R.styleable.ProgressRingView_ringErrorColor, Color.RED);
        mWarningPercentage = typedArray.getFraction(R.styleable.ProgressRingView_warningPercentage, 1, 1, 0.4f);
        mErrorPercentage = typedArray.getFraction(R.styleable.ProgressRingView_errorPercentage, 1, 1, 0.2f);
        typedArray.recycle();

        mRectF = new RectF();
        mTextBounds = new Rect();
        // 画笔初始化
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mRingNormalColor);
        mTextPaint.setTextSize(mTextSize);

        mRingBackgroundPaint = new Paint();
        mRingBackgroundPaint.setAntiAlias(true);
        mRingBackgroundPaint.setColor(mRingBackgroundColor);
        mRingBackgroundPaint.setStyle(Paint.Style.STROKE);
        mRingBackgroundPaint.setStrokeWidth(mRingWidth);
        mRingBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        mRingForegroundPaint = new Paint();
        mRingForegroundPaint.setAntiAlias(true);
        mRingForegroundPaint.setColor(mRingNormalColor);
        mRingForegroundPaint.setStyle(Paint.Style.STROKE);
        mRingForegroundPaint.setStrokeWidth(mRingWidth);
        mRingForegroundPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public float getMaxValue() {
        return this.mMaxValue;
    }

    public void setMaxValue(float maxValue) {
        this.mMaxValue = maxValue;
        invalidate();
    }

    public float getCurrentValue() {
        return this.currentValue;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mWidth = Math.min(mWidth, mHeight);
        mHeight = mWidth;
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMaxValue == 0) {
            return;
        }
        // 画底部的圆环
        float rectFRightAndBottom = mWidth - mRingWidth;
        int rectFLeftAndTop = (int) (mRingWidth * 1.1);
        mRectF.set(rectFLeftAndTop, rectFLeftAndTop, rectFRightAndBottom, rectFRightAndBottom);
        canvas.drawArc(mRectF, 120, 300, false, mRingBackgroundPaint);
        // 画覆盖在上方的圆环, 根据当前的百分比来确定颜色
        float percentage = currentValue / mMaxValue;
        if (percentage > mWarningPercentage) {
            mRingForegroundPaint.setColor(mRingNormalColor);
            mTextPaint.setColor(mRingNormalColor);
        }
        else if (percentage > mErrorPercentage && percentage <= mWarningPercentage) {
            mRingForegroundPaint.setColor(mRingWarningColor);
            mTextPaint.setColor(mRingWarningColor);
        }
        else if (percentage >= 0 && percentage <= mErrorPercentage){
            mRingForegroundPaint.setColor(mRingErrorColor);
            mTextPaint.setColor(mRingErrorColor);
        }
        canvas.drawArc(mRectF, 120, percentage * 300, false, mRingForegroundPaint);
        // 画文字
        String text = (int) (percentage * 100) + "%";
        mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        int textX = mWidth / 2 - (mTextBounds.width() / 2) + OFFSET / 3;
        int textHeight = (int) (mTextPaint.descent() - mTextPaint.ascent());
        int baseline = (int) ((mTextBounds.height() - textHeight) / 2 - mTextPaint.ascent());
        int textY = mHeight / 2 + baseline / 2;
        canvas.drawText(text, textX,  textY, mTextPaint);
    }

}
