package com.zebra.rfidreader.demo.rapidread;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.zebra.rfidreader.demo.R;

/**
 * TODO: document your custom view class.
 */
public class MatchModeProgressView extends View {
    protected int colorInRange = getResources().getColor(R.color.colorAccent);
    protected int colorNeutral = Color.parseColor("#f2f2f2"); // light gray
    float mSweepAngle = 0;
    float mStartAngle = 0;
    float strokeWidth = 20;
    boolean bCompleted = false;
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;
    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private RectF mRectF;
    private Paint mPaintBackground;
    private Paint mPaintProgress;

    public MatchModeProgressView(Context context) {
        super(context);
        init(null, 0);
    }

    public MatchModeProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MatchModeProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MatchModeProgressView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.MatchModeProgressView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.MatchModeProgressView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.MatchModeProgressView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.MatchModeProgressView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.MatchModeProgressView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        setup();

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    private void setup() {
        mPaintBackground = new Paint();
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setStrokeWidth(strokeWidth);
        //mPaintBackground.setStrokeCap(Paint.Cap.ROUND);
        mPaintBackground.setColor(colorNeutral);

        mPaintProgress = new Paint();
        mPaintProgress.setAntiAlias(true);
        mPaintProgress.setStyle(Paint.Style.STROKE);
        mPaintProgress.setStrokeWidth(strokeWidth);
        //mPaintProgress.setStrokeCap(Paint.Cap.ROUND);
        mPaintProgress.setColor(colorInRange);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }

        // Draw the bar outline
        float width = contentHeight - strokeWidth;

        mRectF = new RectF();
        mRectF.top = getPaddingTop() + strokeWidth / 2;
        mRectF.bottom = contentHeight - strokeWidth / 2;
        mRectF.left = getPaddingLeft() + contentWidth / 2 - width / 2;
        mRectF.right = contentWidth / 2 + width / 2;

        // draw background line
        canvas.drawArc(mRectF, 0, 360, false, mPaintBackground);
        // draw progress line
        if (!bCompleted) {
            for (float i = mStartAngle + 270; i < mSweepAngle + 270; ) {
                canvas.drawArc(mRectF, i, 1, false, mPaintProgress);
                i += 2;
            }
        } else {
            canvas.drawArc(mRectF, 0, 360, false, mPaintProgress);
        }
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
