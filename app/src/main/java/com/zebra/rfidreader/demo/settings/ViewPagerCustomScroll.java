package com.zebra.rfidreader.demo.settings;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ViewPagerCustomScroll extends ViewPager {
    private boolean mAdvancedOptionEnabled = false;

    public ViewPagerCustomScroll(Context context) {
        super(context);
    }

    public ViewPagerCustomScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdvancedOptionEnabled(boolean AdvancedOptionEnabled) {
        this.mAdvancedOptionEnabled = AdvancedOptionEnabled;
    }

    private boolean canScroll() {
        if (!mAdvancedOptionEnabled) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return canScroll() && super.canScrollHorizontally(direction);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return canScroll() && super.canScroll(v, checkV, dx, x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return canScroll() && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return canScroll() && super.onInterceptTouchEvent(event);
    }

}
