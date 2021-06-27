package com.zebra.rfidreader.demo.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zebra.rfidreader.demo.BuildConfig;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.rfid.RFIDController;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle the display of application info.
 */
public class AboutFragment extends Fragment {
    TextView tv_radioVersion;
    TextView tv_moduleVersion;
    TextView tv_appVersion;

    public AboutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AboutFragment.
     */
    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tv_radioVersion = (TextView) getActivity().findViewById(R.id.radioVersion);
        tv_moduleVersion = (TextView) getActivity().findViewById(R.id.moduleVersion);
        tv_appVersion = (TextView) getActivity().findViewById(R.id.appVersion);
        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected()) {
            if (Application.versionInfo.containsKey("NGE"))
                tv_radioVersion.setText(Application.versionInfo.get(Constants.NGE) + "");
            if (RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
                if (Application.versionInfo.containsKey("GENX_DEVICE"))
                    tv_moduleVersion.setText(Application.versionInfo.get(Constants.GENX_DEVICE) + "");
            } else {
                if (Application.versionInfo.containsKey("RFID_DEVICE"))
                    tv_moduleVersion.setText(Application.versionInfo.get(Constants.RFID_DEVICE) + "");
            }
        }
        tv_appVersion.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * method to clear version details on disconnection of the reader
     */
    public void resetVersionDetail() {
        if (tv_radioVersion != null)
            tv_radioVersion.setText("");
        if (tv_moduleVersion != null)
            tv_moduleVersion.setText("");
    }

    /**
     * method to set version details retrieved from reader after connected with reader
     */
    public void deviceConnected() {
        if (tv_radioVersion != null && Application.versionInfo.containsKey("NGE"))
            tv_radioVersion.setText(Application.versionInfo.get(Constants.NGE) + "");
        if (RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
            if (tv_moduleVersion != null && Application.versionInfo.containsKey("GENX_DEVICE"))
                tv_moduleVersion.setText(Application.versionInfo.get(Constants.GENX_DEVICE) + "");

        } else {
            if (tv_moduleVersion != null && Application.versionInfo.containsKey("RFID_DEVICE"))
                tv_moduleVersion.setText(Application.versionInfo.get(Constants.RFID_DEVICE) + "");
        }
    }
}
