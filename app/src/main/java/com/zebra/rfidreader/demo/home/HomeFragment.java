package com.zebra.rfidreader.demo.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.settings.SettingsDetailActivity;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to show the HomeScreen
 */
public class HomeFragment extends Fragment {
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_layout_new, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        menu.removeItem(R.id.action_dpo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                ((MainActivity) getActivity()).aboutClicked();
                return true;

            case R.id.action_reader_list:

                Intent detailsIntent = new Intent(getContext(), SettingsDetailActivity.class);
                detailsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                detailsIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                detailsIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                detailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                detailsIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.readers_list);
                startActivity(detailsIntent);
               return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
