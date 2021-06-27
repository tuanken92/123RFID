package com.zebra.rfidreader.demo.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.util.Timer;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link BatteryFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to show the battery information.
 */
public class BatteryFragment extends Fragment {
    private ImageView batteryLevelImage;
    private TextView batteryStatusText;
    private TextView batteryLevel;
    private Timer t;

    public BatteryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BatteryFragment.
     */
    public static BatteryFragment newInstance() {
        return new BatteryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_battery, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        batteryLevel = (TextView) getActivity().findViewById(R.id.batteryLevelText);
        batteryLevelImage = (ImageView) getActivity().findViewById(R.id.batteryLevelImage);
        batteryStatusText = (TextView) getActivity().findViewById(R.id.batteryStatusText);
        if (RFIDController.BatteryData != null)
            deviceStatusReceived(RFIDController.BatteryData.getLevel(), RFIDController.BatteryData.getCharging(), RFIDController.BatteryData.getCause());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void deviceStatusReceived(final int level, final boolean charging, final String cause) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (level >= Constants.BATTERY_FULL) {
                    batteryLevelImage.setImageLevel(10);
                    batteryLevel.setText(Constants.BATTERY_FULL + "%");
                } else {
                    batteryLevelImage.setImageLevel((int) (level / 10));
                    batteryLevel.setText(level + "%");
                }
                if (cause != null && cause.trim().equalsIgnoreCase(Constants.MESSAGE_BATTERY_CRITICAL)) {
                    batteryStatusText.setText(getString(R.string.battery_critical_message));
                    batteryStatusText.setTextAppearance(getActivity(), R.style.style_red_font);
                } else if (cause != null && cause.trim().equalsIgnoreCase(Constants.MESSAGE_BATTERY_LOW)) {
                    batteryStatusText.setText(getString(R.string.battery_low_message));
                    batteryStatusText.setTextAppearance(getActivity(), R.style.style_red_font);
                } else {
                    if (charging) {
                        if (level >= Constants.BATTERY_FULL)
                            batteryStatusText.setText(R.string.battery_full_message);
                        else
                            batteryStatusText.setText(R.string.battery_charging_message);
                    } else
                        batteryStatusText.setText(R.string.battery_discharging_message);
                    batteryStatusText.setTextAppearance(getActivity(), R.style.style_green_font);
                }
                batteryLevel.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * method to update battery screen when device got disconnected
     */
    public void deviceDisconnected() {
        batteryLevelImage.setImageLevel(0);
        batteryLevel.setText(0 + "%");
        batteryLevel.setVisibility(View.INVISIBLE);
        batteryStatusText.setTextAppearance(getActivity(), R.style.style_grey_font);
        batteryStatusText.setText(R.string.battery_no_active_connection_message);
    }

}
