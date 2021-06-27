package com.zebra.rfidreader.demo.access_operations;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zebra.rfid.api3.TagData;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import static com.zebra.rfidreader.demo.home.MainActivity.setAccessProfile;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link AccessOperationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to act as a Holder for Access Tabs(Read/Write, Lock and Kill)
 */
public class AccessOperationsFragment extends Fragment {
    private ViewPager viewPager;
    private AccessOperationsAdapter mAdapter;
    private android.support.v7.app.ActionBar actionBar;
    private int accessOperationCount = -1;
    private boolean showAdvancedOptions = false;

    public AccessOperationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccessOperationsFragment.
     */
    public static AccessOperationsFragment newInstance() {
        return new AccessOperationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_advanced_option, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void UpdateViews() {
        LinearLayout advancedOptions = (LinearLayout) getActivity().findViewById(R.id.accessRWAdvanceOption);
        if (advancedOptions != null) {
            if (showAdvancedOptions) {
                advancedOptions.setVisibility(View.VISIBLE);
                //getActivity().findViewById(R.id.seperaterData).setVisibility(View.GONE);
            } else {
                advancedOptions.setVisibility(View.INVISIBLE);
                //getActivity().findViewById(R.id.seperaterData).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_adv_op:
                showAdvancedOptions = !showAdvancedOptions;
                UpdateViews();
                SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.ACCESS_ADV_OPTIONS, showAdvancedOptions);
                editor.commit();
                break;
            case R.id.action_inventory :
                ((MainActivity) getActivity()).selectItem(2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_access_operations, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialization
        viewPager = (ViewPager) getActivity().findViewById(R.id.accessOperationsPager);
        mAdapter = new AccessOperationsAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        //
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        showAdvancedOptions = settings.getBoolean(Constants.ACCESS_ADV_OPTIONS, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        accessOperationCount = -1;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        RFIDController.isAccessCriteriaRead = false;
        setAccessProfile(false);
    }

    public void handleTagResponse(TagData tagData) {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null && fragment instanceof AccessOperationsReadWriteFragment) {
                ((AccessOperationsReadWriteFragment) fragment).handleTagResponse(tagData);
            }
        }
    }

    /**
     * Method to fetch one of (Read/Write, Lock or Kill) fragments currently being displayed
     *
     * @return - {@link android.support.v4.app.Fragment} instance
     */
    public Fragment getCurrentlyViewingFragment() {
        if (mAdapter != null && viewPager != null) {
            return mAdapter.getFragment(viewPager.getCurrentItem());
        } else {
            return null;
        }
    }


    /**
     * interface to maintain last entered access tag id in access control fragments
     */
    public interface OnRefreshListener {
        /**
         * method to update accessControlTag value
         */
        void onUpdate();

        /**
         * method to refresh the fragment details with updated tag id
         */
        void onRefresh();
    }
}
