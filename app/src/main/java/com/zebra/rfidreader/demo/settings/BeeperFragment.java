package com.zebra.rfidreader.demo.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.CustomProgressDialog;
import com.zebra.rfidreader.demo.rfid.RFIDController;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link BeeperFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to show the UI for Beeper settings.
 */
public class BeeperFragment extends BackPressedFragment {
    private CheckBox sledBeeper;
    private CheckBox hostBeeper;
    private Spinner volumeSpinner;
    private ArrayAdapter<CharSequence> volumeAdapter;

    public BeeperFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BeeperFragment.
     */
    public static BeeperFragment newInstance() {
        return new BeeperFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_beeper, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sledBeeper = (CheckBox) getActivity().findViewById(R.id.sledBeeper);
        hostBeeper = (CheckBox) getActivity().findViewById(R.id.hostBeeper);
        if (RFIDController.mConnectedReader != null) {
            if (!RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
                sledBeeper.setEnabled(false);
            }
        }
        hostBeeper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked() || sledBeeper.isChecked()) {
                    ((Spinner) volumeSpinner).getSelectedView().setEnabled(true);
                    volumeSpinner.setEnabled(true);
                } else if (!sledBeeper.isChecked()) {
                    //   volumeSpinner.setVisibility(View.INVISIBLE);
                    ((Spinner) volumeSpinner).getSelectedView().setEnabled(false);
                    volumeSpinner.setEnabled(false);
                }
            }
        });
        sledBeeper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked() || hostBeeper.isChecked()) {
                    ((Spinner) volumeSpinner).getSelectedView().setEnabled(true);
                    volumeSpinner.setEnabled(true);
                } else if (!hostBeeper.isChecked()) {
                    //   volumeSpinner.setVisibility(View.INVISIBLE);
                    ((Spinner) volumeSpinner).getSelectedView().setEnabled(false);
                    volumeSpinner.setEnabled(false);
                }
            }
        });
        volumeSpinner = (Spinner) getActivity().findViewById(R.id.volumeSpinner);
        volumeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.beeper_volume_array, R.layout.custom_spinner_layout);
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        volumeSpinner.setAdapter(volumeAdapter);
        if (RFIDController.beeperVolume != null) {
            if (RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                hostBeeper.setChecked(false);
                volumeSpinner.setEnabled(false);
                if (RFIDController.beeperspinner_status != 3)
                    volumeSpinner.setSelection(RFIDController.beeperspinner_status);

            } else if (!RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                hostBeeper.setChecked(true);
                if (RFIDController.mConnectedReader != null) {
                    if (!RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
                        volumeSpinner.setSelection(RFIDController.beeperVolume.getValue());
                        volumeSpinner.setEnabled(true);
                        sledBeeper.setEnabled(false);
                    }
                }
            }
        }
        if (RFIDController.mConnectedReader != null) {
            BEEPER_VOLUME beeper_volume = null;
            try {
                beeper_volume = RFIDController.mConnectedReader.Config.getBeeperVolume();
                RFIDController.sledBeeperVolume = beeper_volume;

            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {

                beeper_volume = RFIDController.sledBeeperVolume;
                e.printStackTrace();
            }
            if (beeper_volume != null) {
                if (beeper_volume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                    sledBeeper.setChecked(false);
                    if (!RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                        hostBeeper.setChecked(true);
                        volumeSpinner.setSelection(RFIDController.beeperVolume.getValue());
                        volumeSpinner.setEnabled(true);
                    } else {
                        volumeSpinner.setEnabled(false);
                        if (RFIDController.beeperspinner_status != 3)
                            volumeSpinner.setSelection(RFIDController.beeperspinner_status);
                    }
                } else if (!beeper_volume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                    sledBeeper.setChecked(true);
                    if (RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                        volumeSpinner.setSelection(beeper_volume.getValue());
                        volumeSpinner.setEnabled(true);
                    } else {
                        volumeSpinner.setSelection(RFIDController.beeperVolume.getValue());
                        volumeSpinner.setEnabled(true);
                    }
                }
            }
        }
    }


    public void onBackPressed() {
        if (RFIDController.mConnectedReader != null) {
            if ((hostBeeper.isChecked() && RFIDController.beeperVolume != null && RFIDController.beeperVolume.getValue() != volumeSpinner.getSelectedItemPosition()) || (sledBeeper.isEnabled() && sledBeeper.isChecked() && RFIDController.sledBeeperVolume != null && RFIDController.sledBeeperVolume.getValue() != volumeSpinner.getSelectedItemPosition())) {
                new Task_SetVolume(volumeSpinner.getSelectedItemPosition()).execute();
            } else if (RFIDController.mConnectedReader.getHostName().startsWith("RFD8500") && (hostBeeper.isChecked() && RFIDController.beeperVolume.getValue() == volumeSpinner.getSelectedItemPosition() && !sledBeeper.isChecked()) || (sledBeeper.isChecked() && RFIDController.sledBeeperVolume.getValue() == volumeSpinner.getSelectedItemPosition() && !hostBeeper.isChecked())) {
                if (RFIDController.beeperVolume.getValue() != volumeSpinner.getSelectedItemPosition()) {
                    ((SettingsDetailActivity) getActivity()).callBackPressed();
                } else if (RFIDController.sledBeeperVolume.getValue() != volumeSpinner.getSelectedItemPosition()) {
                    ((SettingsDetailActivity) getActivity()).callBackPressed();
                } else {
                    new Task_SetVolume(volumeSpinner.getSelectedItemPosition()).execute();
                }
            } else if ((!hostBeeper.isChecked() && RFIDController.beeperVolume != null && !RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) || (sledBeeper.isEnabled() && !sledBeeper.isChecked() && RFIDController.sledBeeperVolume != null && !RFIDController.sledBeeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP))) {
                RFIDController.beeperspinner_status = volumeSpinner.getSelectedItemPosition();
                new Task_SetVolume(Constants.QUIET_BEEPER).execute();
            } else {
                ((SettingsDetailActivity) getActivity()).callBackPressed();
            }
        } else {
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        }
    }


    /**
     * method to update battery screen when device got disconnected
     */
    public void deviceDisconnected() {
        hostBeeper.setChecked(false);
    }

    private class Task_SetVolume extends AsyncTask<Void, Void, Boolean> {
        private final int volume;
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;
        private CustomProgressDialog progressDialog;
        private int percantageVolume = 100;
        private boolean bsledBeeper = false;
        private boolean bhostBeeper = false;
        private boolean bsledBeeperEnable = false;

        public Task_SetVolume(int volume) {
            this.volume = volume;
        }

        @Override
        protected void onPreExecute() {
            bsledBeeper = sledBeeper.isChecked();
            bhostBeeper = hostBeeper.isChecked();
            bsledBeeperEnable = sledBeeper.isEnabled();
            progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.beeper_volume_title));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean bResult = true;

            try {

                if (RFIDController.mConnectedReader != null) {
                    if (bsledBeeperEnable) {
                        if (bsledBeeper) {

                            // disable beeper get/set from/to reader
                            if (volume == 0) {
                                RFIDController.mConnectedReader.Config.setBeeperVolume(BEEPER_VOLUME.HIGH_BEEP);
                                RFIDController.sledBeeperVolume = BEEPER_VOLUME.HIGH_BEEP;
                            }
                            if (volume == 1) {
                                RFIDController.mConnectedReader.Config.setBeeperVolume(BEEPER_VOLUME.MEDIUM_BEEP);
                                RFIDController.sledBeeperVolume = BEEPER_VOLUME.MEDIUM_BEEP;
                            }
                            if (volume == 2) {
                                RFIDController.mConnectedReader.Config.setBeeperVolume(BEEPER_VOLUME.LOW_BEEP);
                                RFIDController.sledBeeperVolume = BEEPER_VOLUME.LOW_BEEP;
                            }
                            if (volume == 3) {
                                RFIDController.mConnectedReader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
                                RFIDController.sledBeeperVolume = BEEPER_VOLUME.QUIET_BEEP;
                            }

                        } else {

                            if (RFIDController.mConnectedReader != null) {
                                RFIDController.mConnectedReader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
                            }

                        }
                    }
                    //
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.pref_beeper), getContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    if (bhostBeeper) {
                        if (volume == 0) {
                            RFIDController.beeperVolume = BEEPER_VOLUME.HIGH_BEEP;
                            percantageVolume = 100;
                        }
                        if (volume == 1) {
                            RFIDController.beeperVolume = BEEPER_VOLUME.MEDIUM_BEEP;
                            percantageVolume = 75;
                        }
                        if (volume == 2) {
                            RFIDController.beeperVolume = BEEPER_VOLUME.LOW_BEEP;
                            percantageVolume = 50;
                        }
                        if (volume == 3) {
                            RFIDController.beeperVolume = BEEPER_VOLUME.QUIET_BEEP;
                            percantageVolume = 0;
                        }
                        editor.putInt(getString(R.string.beeper_volume), volume);

                    } else {
                        RFIDController.beeperVolume = BEEPER_VOLUME.QUIET_BEEP;
                        percantageVolume = 0;
                        editor.putInt(getString(R.string.beeper_volume), 3);

                    }
                    editor.commit();
                }

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
            try {
                if (!result) {
                    if (invalidUsageException != null)
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                    if (operationFailureException != null)
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
                } else {
                    int streamType = AudioManager.STREAM_DTMF;
                    RFIDController.toneGenerator = new ToneGenerator(streamType, percantageVolume);
                    Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
                }
            } catch (RuntimeException exception) {
                exception.printStackTrace();
                RFIDController.toneGenerator = null;
            }
            super.onPostExecute(result);
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        }
    }
}
