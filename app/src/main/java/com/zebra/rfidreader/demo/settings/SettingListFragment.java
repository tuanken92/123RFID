package com.zebra.rfidreader.demo.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;


/**
 * A list fragment representing a list of Settings.
 */
public class SettingListFragment extends ListFragment {
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private SettingAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingListFragment() {
    }

    public static SettingListFragment newInstance() {
        SettingListFragment fragment = new SettingListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SettingAdapter(getActivity(), R.layout.setting_list, SettingsContent.ITEMS);
        setListAdapter(adapter);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.action_dpo);
    }

    @Override
    public void onResume() {
        super.onResume();
        // settingsListUpdated();

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Intent detailsIntent = new Intent(getActivity(), SettingsDetailActivity.class);
        detailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        detailsIntent.putExtra(Constants.SETTING_ITEM_ID, Integer.parseInt(SettingsContent.ITEMS.get(position).id));
        startActivity(detailsIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

//    public void settingsListUpdated() {
//        //
//        if (RFIDController.dynamicPowerSettings != null && RFIDController.dynamicPowerSettings.getValue() == 1)
//            AdvancedOptionsContent.ITEMS.get(DPO_ITEM_INDEX).icon = R.drawable.title_dpo_enabled;
//        else {
//            AdvancedOptionsContent.ITEMS.get(DPO_ITEM_INDEX).icon = R.drawable.title_dpo_disabled;
//        }
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (adapter != null)
//                    adapter.notifyDataSetChanged();
//            }
//        });
//    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onSettingsItemSelected(String id);
    }
}
