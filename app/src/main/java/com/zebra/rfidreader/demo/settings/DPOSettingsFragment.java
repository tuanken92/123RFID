package com.zebra.rfidreader.demo.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.CustomProgressDialog;
import com.zebra.rfidreader.demo.rfid.RFIDController;


/**
 * Created by qvfr34 on 6/26/2015.
 */
public class DPOSettingsFragment extends BackPressedFragment {
    private CheckBox dynamicPower;

    public DPOSettingsFragment() {

    }

    public static DPOSettingsFragment newInstance() {
        return new DPOSettingsFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dpo, container, false);

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dynamicPower = (CheckBox) getActivity().findViewById(R.id.dynamicPower);


        if (RFIDController.dynamicPowerSettings != null) {
            if (RFIDController.dynamicPowerSettings.getValue() == 0)
                dynamicPower.setChecked(false);
            else if (RFIDController.dynamicPowerSettings.getValue() == 1)
                dynamicPower.setChecked(true);
        }

//        if (dynamicPower.isChecked())
//            actionBar.setIcon(R.drawable.dl_dpo_enabled);
//        else
//            actionBar.setIcon(R.drawable.dl_dpo_disabled);
    }

    @Override
    public void onBackPressed() {
        if (isSettingsChanged())
            new Task_SaveDynamicPowerSetting(dynamicPower.isChecked()).execute();
        else{
            AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
            replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
        }


    }

    public boolean isSettingsChanged() {
        if (RFIDController.dynamicPowerSettings != null && !((dynamicPower.isChecked() && RFIDController.dynamicPowerSettings.getValue() == 1) || (!dynamicPower.isChecked() && RFIDController.dynamicPowerSettings.getValue() != 1))) {
            return true;
        }
        return false;
    }

    private class Task_SaveDynamicPowerSetting extends AsyncTask<Void, Void, Boolean> {
        private final boolean enabled;
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;
        private CustomProgressDialog progressDialog;

        public Task_SaveDynamicPowerSetting(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.dynamic_power_title));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean bResult = true;
            try {
                if (enabled)
                    RFIDController.mConnectedReader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);
                else
                    RFIDController.mConnectedReader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
                RFIDController.dynamicPowerSettings = RFIDController.mConnectedReader.Config.getDPOState();
                ProfileContent.UpdateActiveProfile();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
                invalidUsageException = e;
                bResult = false;
            } catch (OperationFailureException e) {
                e.printStackTrace();
                operationFailureException = e;
                bResult = false;
            }

            return bResult;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.cancel();
            if (!result) {
                if (invalidUsageException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                if (operationFailureException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
            } else
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
            AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
            replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
            //((SettingsDetailActivity) getActivity()).callBackPressed();
        }
    }

    public void deviceConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (RFIDController.dynamicPowerSettings != null) {
                    if (RFIDController.dynamicPowerSettings.getValue() == 1)
                        dynamicPower.setChecked((true));
                    else if (RFIDController.dynamicPowerSettings.getValue() == 0)
                        dynamicPower.setChecked((false));
                }
            }
        });
    }

    public void deviceDisconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dynamicPower.setChecked(false);
            }
        });
    }
    public static void replaceFragment (@NonNull FragmentManager fragmentManager,
                                        @NonNull Fragment fragment, int frameId){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.disallowAddToBackStack();
        transaction.commit();
    }
   /* @Override
    public void handleStatusResponse(final Response_Status statusData) {
        String command = statusData.command.trim();
        if (command.equalsIgnoreCase(Constants.COMMAND_DYNAMICPOWER))
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (statusData.Status.trim().equalsIgnoreCase("OK")) {
                        ((BaseReceiverActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_success_message));
                    } else
                        ((BaseReceiverActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + statusData.Status);

                    ((SettingsDetailActivity) getActivity()).callBackPressed();
                }
            });
    }*/
}
