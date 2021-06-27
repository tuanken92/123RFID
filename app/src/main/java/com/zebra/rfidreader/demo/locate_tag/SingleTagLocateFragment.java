package com.zebra.rfidreader.demo.locate_tag;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces;
import com.zebra.rfidreader.demo.common.asciitohex;
import com.zebra.rfidreader.demo.common.hextoascii;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import static com.zebra.rfidreader.demo.home.MainActivity.filter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link SingleTagLocateFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle locationing
 */
public class SingleTagLocateFragment extends Fragment implements ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler,
        LocateOperationsFragment.OnRefreshListener{
    private RangeGraph locationBar;
    //private TextView distance;
    private FloatingActionButton btn_locate;
    private AutoCompleteTextView et_locateTag;
    private ArrayAdapter<String> adapter;

    public SingleTagLocateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LocationingFragment.
     */
    public static SingleTagLocateFragment newInstance() {
        return new SingleTagLocateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_tag_locate, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        locationBar = (RangeGraph) getActivity().findViewById(R.id.locationBar);
        // distance=(TextView)getActivity().findViewById(R.id.distance);
        btn_locate = (FloatingActionButton) getActivity().findViewById(R.id.btn_locate);
        et_locateTag = (AutoCompleteTextView) getActivity().findViewById(R.id.lt_et_epc);
        if (RFIDController.asciiMode == true)
            et_locateTag.setFilters(new InputFilter[]{filter});
        else
            et_locateTag.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});
        RFIDController.getInstance().updateTagIDs();
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.tagIDs);
        et_locateTag.setAdapter(adapter);

        locationBar.setValue(0);
        if (RFIDController.isLocatingTag) {
            if (et_locateTag != null && Application.locateTag != null) {
                if (RFIDController.asciiMode == true)
                    et_locateTag.setText(hextoascii.convert(RFIDController.currentLocatingTag));
                else
                    et_locateTag.setText(asciitohex.convert(RFIDController.currentLocatingTag));
            }
            et_locateTag.setFocusable(false);
            if (btn_locate != null) {
                btn_locate.setImageResource(R.drawable.ic_play_stop);
            }
            showTagLocationingDetails();
        } else {
            if (et_locateTag != null && Application.locateTag != null) {
                if (RFIDController.asciiMode == true)
                    et_locateTag.setText(hextoascii.convert(Application.locateTag));
                else
                    et_locateTag.setText(asciitohex.convert(Application.locateTag));
            }
            if (btn_locate != null) {
                btn_locate.setImageResource(android.R.drawable.ic_media_play);
            }
        }
        if(RFIDController.asciiMode == true) {
            SpannableStringBuilder print_tag = new SpannableStringBuilder(et_locateTag.getText());
            for(int i =0; i < print_tag.length(); i++) {
                if(print_tag.charAt(i) == ' ') {
                    BackgroundColorSpan bcs = new BackgroundColorSpan(Color.YELLOW);
                    print_tag.setSpan(bcs, i, i+1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            et_locateTag.setText(print_tag);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Application.locateTag = et_locateTag.getText().toString();
    }

    public void showTagLocationingDetails() {
        if (RFIDController.TagProximityPercent != -1) {
           /* if (distance != null)
                distance.setText(RFIDController.tagProximityPercent.Proximitypercent + "%");*/
            if (locationBar != null && RFIDController.TagProximityPercent != -1) {
                locationBar.setValue((short) RFIDController.TagProximityPercent);
                locationBar.invalidate();
                locationBar.requestLayout();
            }
        }
    }

    public void handleLocateTagResponse() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTagLocationingDetails();
            }
        });
    }

    @Override
    public void triggerPressEventRecieved() {
        if (!RFIDController.isLocatingTag)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.locationingButtonClicked(btn_locate);
                    }
                }
            });
    }

    @Override
    public void triggerReleaseEventRecieved() {
        if (RFIDController.isLocatingTag) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.locationingButtonClicked(btn_locate);
                    }
                }
            });
        }
    }

    public void resetLocationingDetails(final boolean isDeviceDisconnected) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (btn_locate != null) {
                    btn_locate.setImageResource(android.R.drawable.ic_media_play);
                }
                if (isDeviceDisconnected && locationBar != null) {
                    locationBar.setValue(0);
                    locationBar.invalidate();
                    locationBar.requestLayout();
                }
                if (et_locateTag != null) {
                    et_locateTag.setFocusableInTouchMode(true);
                    et_locateTag.setFocusable(true);
                }
            }
        });
    }

    public void handleStatusResponse(final RFIDResults results) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!results.equals(RFIDResults.RFID_API_SUCCESS)) {
                    //String command = statusData.command.trim();
                    //if (command.equalsIgnoreCase("lt") || command.equalsIgnoreCase("locatetag"))
                    {
                        RFIDController.isLocatingTag = false;
                        if (btn_locate != null) {
                            btn_locate.setImageResource(android.R.drawable.ic_media_play);
                        }
                        if (et_locateTag != null) {
                            et_locateTag.setFocusableInTouchMode(true);
                            et_locateTag.setFocusable(true);
                        }
                    }

                }
            }
        });
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onRefresh() {
    }
}
