package com.zebra.rfidreader.demo.settings;

/**
 * Created by XJR746 on 09-10-2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.rfid.RFIDController;


public class LedFragment extends BackPressedFragment {
    public static final String SHARED_PREF_NAME = "Switch";
    Context context;
    SharedPreferences mSharedPreferences;
    private CheckBox checkboxled;

    //public static final String LEDSTATE = "LED_STATE";
    //Boolean Ledstate;

    public LedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BeeperFragment.
     */
    public static LedFragment newInstance() {
        return new LedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootview = inflater.inflate(R.layout.fragment_led, container, false);
        mSharedPreferences = getActivity().getSharedPreferences("LEDPreferences", getContext().MODE_PRIVATE);
        RFIDController.ledState = mSharedPreferences.getBoolean("LED_STATE1", true);

        context = rootview.getContext();
        //RFIDController.AUTO_DETECT_READERS = Ledstate.ge(Constants.AUTO_DETECT_READERS, true);
        checkboxled = (CheckBox) rootview.findViewById(R.id.checkboxled);

        checkboxled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkboxled.isChecked()) {
                    if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected())
                        try {
                            RFIDController.mConnectedReader.Config.setLedBlinkEnable(true);
                        } catch (InvalidUsageException e) {
                            e.printStackTrace();
                        } catch (OperationFailureException e) {
                            e.printStackTrace();
                        }
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean("LED_STATE1", true);
                    editor.apply();
                } else {
                    try {
                        if (RFIDController.mConnectedReader != null) {
                            RFIDController.mConnectedReader.Config.setLedBlinkEnable(false);
                        }
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean("LED_STATE1", false);
                    editor.apply();
                }
            }
        });
        if (RFIDController.ledState) {
            checkboxled.setChecked(true);

        } else {
            checkboxled.setChecked(false);
        }
        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        ((SettingsDetailActivity) getActivity()).callBackPressed();
    }

    /**
     * method to update battery screen when device got disconnected
     */
    public void deviceDisconnected() {
        checkboxled.setChecked(false);
    }
}
