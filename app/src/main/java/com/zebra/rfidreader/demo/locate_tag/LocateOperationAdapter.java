package com.zebra.rfidreader.demo.locate_tag;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.zebra.rfidreader.demo.locate_tag.multitag_locate.MultiTagLocateFragment;

import java.util.HashMap;

/**
 * Class to handle the details about locate operations(No of Tabs, Class acting as UI for the tabs) etc..
 */
public class LocateOperationAdapter extends FragmentStatePagerAdapter {
    private static final int NO_OF_TABS = 2;

    //Map to hold the references for currently active fragments so that we can acess them
    private HashMap<Integer, Fragment> currentlyActiveFragments;

    /**
     * Constructor. Handles the initialization
     *
     * @param fm - FragmentManager instance to be used for handling fragments
     */
    public LocateOperationAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        if (currentlyActiveFragments == null)
            currentlyActiveFragments = new HashMap<>();

        Fragment fragment;

        switch (index) {
            case 0:
                Log.d(getClass().getSimpleName(), "1st Tab Selected");
                fragment = SingleTagLocateFragment.newInstance();
                break;
            case 1:
                Log.d(getClass().getSimpleName(), "2nd Tab Selected");
                fragment = MultiTagLocateFragment.newInstance();
                break;
            default:
                fragment = null;
                break;
        }

        //Store the reference
        currentlyActiveFragments.put(index, fragment);
        return fragment;
    }

    /**
     * Get the active fragment at the given position
     *
     * @param key - Index to be used for fetching the fragment
     * @return - {@link Fragment} at the given index
     */
    public Fragment getFragment(int key) {
        if (currentlyActiveFragments != null)
            return currentlyActiveFragments.get(key);
        else
            return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        //Remove the reference
        currentlyActiveFragments.remove(position);
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return NO_OF_TABS;
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof LocateOperationsFragment.OnRefreshListener) {
            ((LocateOperationsFragment.OnRefreshListener) object).onRefresh();
        }
        return PagerAdapter.POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String[] tabs = {"Single Tag", "Multi Tag"};
        return tabs[position];
    }
}

