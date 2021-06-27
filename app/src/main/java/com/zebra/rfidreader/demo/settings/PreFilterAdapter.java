package com.zebra.rfidreader.demo.settings;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.zebra.rfidreader.demo.common.Constants;


/**
 * Adapter for showing prefilters(2 tabs)
 */
public class PreFilterAdapter extends FragmentStatePagerAdapter {
    public final static int NO_OF_TABS = 2;

    /**
     * Constructor. Handles the initialization.
     *
     * @param fm - Fragment Manager to be used for handling fragments.
     */
    public PreFilterAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Constants.logAsMessage(Constants.TYPE_DEBUG, getClass().getSimpleName(), "1st Tab Selected");
                return PreFilter1ContentFragment.newInstance();
            case 1:
                Constants.logAsMessage(Constants.TYPE_DEBUG, getClass().getSimpleName(), "2nd Tab Selected");
                return PreFilter2ContentFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NO_OF_TABS;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        // Tab titles
        String[] tabs = {"Filter 1", "Filter 2"};
        return tabs[position];
    }
}
