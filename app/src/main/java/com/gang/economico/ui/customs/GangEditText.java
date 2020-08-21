package com.gang.economico.ui.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.gang.economico.R;

public class GangEditText extends androidx.appcompat.widget.AppCompatEditText {

    private static final String TAG = "Gang edit text";
    private static final int OFFSET_VERTICAL = 10;
    private static final int OFFSET_VERTICAL_TEXT = 10;
    private static final float RATIO = 0.70711f;
    // 设置的最大的输入长度
    private int mMaxLength = 999;
    // 是否需要下划线
    private boolean mUnderLineNeeded;
    // 未获得焦点的下划线颜色
    private int mUnderlineNormalColor;
    // 获得焦点的下划线颜色
    private int mUnderlineFocusedColor;
    private int mUnderlineWidth;
    private int mNumTextSize;
    // 文字颜色
    private int mNumTextNormalColor;
    private int mNumTextFilledColor;
    // 控件的宽高
    private int mWidth, mHeight;
    // 删除小按钮的半径
    private float mCircleRadius;
    // 画笔
    private Paint mUnderlinePaint;
    private Paint mTextPaint;
    private final Paint mCirclePaint;
    private Rect mTextBounds;
    private Rect mDeleteBound;

    public GangEditText(Context context) {
        this(context, null);
    }

    public GangEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GangEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusableInTouchMode(true);
        // 获取属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GangEditText);
        mUnderLineNeeded = typedArray.getBoolean(R.styleable.GangEditText_underlineNeeded, true);
        mUnderlineNormalColor = typedArray.getColor(R.styleable.GangEditText_underlineColorNormal, Color.GRAY);
        mUnderlineFocusedColor = typedArray.getColor(R.styleable.GangEditText_underlineColorFocused, Color.GREEN);
        mNumTextNormalColor = typedArray.getColor(R.styleable.GangEditText_numTextColorNormal, Color.GREEN);
        mNumTextFilledColor = typedArray.getColor(R.styleable.GangEditText_numTextColorFilled, Color.RED);
        mUnderlineWidth = (int) typedArray.getDimension(R.styleable.GangEditText_underlineWidth, 5);
        mNumTextSize = (int) typedArray.getDimension(R.styleable.GangEditText_numTextSize, 15);

        // 获取设置的最大长度
        for (InputFilter filter: getFilters()) {
            if (filter instanceof InputFilter.LengthFilter){
                mMaxLength = ((InputFilter.LengthFilter) filter).getMax();
                break;
            }
        }
        typedArray.recycle();

        // 画笔初始化
        mUnderlinePaint = new Paint();
        mUnderlinePaint.setAntiAlias(true);
        mUnderlinePaint.setColor(mUnderlineNormalColor);
        mUnderlinePaint.setStrokeWidth(mUnderlineWidth);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setTextSize(mNumTextSize);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.GRAY);
        mCirclePaint.setStrokeWidth(3);
        mCirclePaint.setStyle(Paint.Style.STROKE);

        mTextBounds = new Rect();
        mDeleteBound = new Rect();
    }

    public void setEditMaxLength(int maxLength) {
        mMaxLength = maxLength;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mWidth = resolveSize(mWidth, widthMeasureSpec);
        mHeight = resolveSize(mHeight, heightMeasureSpec);
        // Log.d(TAG, "onMeasure: width -->" + mWidth + " height --> " + mHeight);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int lineStartX = getPaddingStart();
        int lineStartY = getHeight() - getPaddingBottom();
        int lineEndX = getWidth() - getPaddingEnd();
        // 需要下划线
        if (mUnderLineNeeded) {
            if (hasFocus()){
                if (length() >= mMaxLength) {
                    mUnderlinePaint.setColor(mNumTextFilledColor);
                }
                else {
                    mUnderlinePaint.setColor(mUnderlineFocusedColor);
                }
            }
            else {
                mUnderlinePaint.setColor(mUnderlineNormalColor);
            }
            canvas.drawLine(lineStartX, lineStartY, lineEndX, lineStartY, mUnderlinePaint);
        }
        // 绘制文字,已经输入了多少字的提示
        String text = length() + "/" + mMaxLength;
        mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        float textWidth = mTextBounds.width();
        float textHeight = mTextPaint.descent() - mTextPaint.ascent();
        float textStartX = lineEndX - textWidth;
        float baseline = (mTextBounds.height() - textHeight) / 2 - mTextPaint.ascent();
        float textStartY = lineStartY + baseline + OFFSET_VERTICAL_TEXT;
        if (length() >= mMaxLength) {
            mTextPaint.setColor(mNumTextFilledColor);
        }
        else {
            if (hasFocus()) {
                mCircleRadius = (float) (textHeight * 0.65);
                mTextPaint.setColor(mNumTextNormalColor);
            }
            else {
                mTextPaint.setColor(mUnderlineNormalColor);
            }
        }
        canvas.drawText(text, textStartX, textStartY, mTextPaint);

        // 如果当前文字大于0，则绘制能够一键删除全部的小图标
        if (length() > 0) {
            float cx = mWidth - getPaddingEnd() - mCircleRadius;
            float cy = lineStartY - mUnderlineWidth - mCircleRadius - OFFSET_VERTICAL;
            // 保存绘制的小删除按钮的边界
            int deleteRectLeft = (int) (cx - mCircleRadius - OFFSET_VERTICAL * 1.5);
            int deleteRectTop = (int) (cy - mCircleRadius - OFFSET_VERTICAL * 1.5);
            int deleteRectRight = (int) (cx + mCircleRadius + OFFSET_VERTICAL * 1.5);
            int deleteRectBottom = (int) (cy + mCircleRadius + OFFSET_VERTICAL * 1.5);
            mDeleteBound.set(deleteRectLeft, deleteRectTop, deleteRectRight, deleteRectBottom);
            // canvas.drawRect(mDeleteBound, mCirclePaint);
            canvas.drawCircle(cx, cy, mCircleRadius, mCirclePaint);
            float temp = mCircleRadius * RATIO;
            float lx1start = cx - temp;
            float ly1start = cy - temp;
            float lx1end = cx + temp;
            float ly1end = cy + temp;
            canvas.drawLine(lx1start, ly1start, lx1end, ly1end, mCirclePaint);
            lx1start = cx + temp;
            ly1start = cy - temp;
            lx1end = cx - temp;
            ly1end = cy + temp;
            canvas.drawLine(lx1start, ly1start, lx1end, ly1end, mCirclePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 上面这个警告是与事件分发有关
        Log.d(TAG, "onTouchEvent: ");
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        Log.d(TAG, "action: " + action);
        Log.d(TAG, "x: " + x);
        Log.d(TAG, "y: " + y);
        Log.d(TAG, "rawX: " + rawX);
        Log.d(TAG, "rawY: " + rawY);
        Log.d(TAG, "onTouchEvent: delete Bound left : " + mDeleteBound.left);
        Log.d(TAG, "onTouchEvent: delete Bound top : " + mDeleteBound.top);
        Log.d(TAG, "onTouchEvent: delete Bound right : " + mDeleteBound.right);
        Log.d(TAG, "onTouchEvent: delete Bound bottom : " + mDeleteBound.bottom);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "onTouchEvent: up");
            if (x > mDeleteBound.left && x < mDeleteBound.right) {
                if (y > mDeleteBound.top && y < mDeleteBound.bottom) {
                    // 点击选中目标区域,清空已经输入的文字
                    Log.d(TAG, "onTouchEvent: click specified region");
                    setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }
}
