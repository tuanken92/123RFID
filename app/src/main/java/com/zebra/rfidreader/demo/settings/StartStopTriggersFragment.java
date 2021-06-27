package com.zebra.rfidreader.demo.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.StartTrigger;
import com.zebra.rfid.api3.StopTrigger;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.CustomProgressDialog;
import com.zebra.rfidreader.demo.common.InputFilterMax;
import com.zebra.rfidreader.demo.rfid.RFIDController;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link StartStopTriggersFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle trigger operations and UI changes.
 */
public class StartStopTriggersFragment extends BackPressedFragment implements Spinner.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private static final String IMMEDIATE = "Immediate";
    private static final String HANDHELD = "Handheld";
    private static final String PERIODIC = "Periodic";
    private static final String DURATION = "Duration";
    private static final String TAG_OBSERVATION = "Tag Observation";
    private static final String N_ATTEMPTS = "N attempts";

    private Spinner startTriggerSpinner;
    private ArrayAdapter<CharSequence> startTriggerAdapter;
    private Spinner stopTriggerSpinner;
    private ArrayAdapter<CharSequence> stopTriggerAdapter;
    private ActionBar actionBar;
    private CheckBox startHandHeldTriggerReleased;
    private CheckBox startHandHeldTriggerPressed;
    private EditText startPeriodic;
    private CheckBox stopHandHeldTriggerReleased;
    private CheckBox stopHandHeldTriggerPressed;
    private EditText stopHandheldDuration;
    private EditText stopDuration;
    private EditText stopTagObserve;
    private EditText stopTagObserveTimeout;
    private EditText stopNObserveAttempts;
    private EditText stopNObserveTimeout;
    private START_TRIGGER_TYPE start_trigger_type;
    private STOP_TRIGGER_TYPE stop_trigger_type;

    public StartStopTriggersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StartStopTriggersFragment.
     */
    public static StartStopTriggersFragment newInstance() {
        return new StartStopTriggersFragment();
    }

    public static void replaceFragment(@NonNull FragmentManager fragmentManager,
                                       @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.disallowAddToBackStack();
        transaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_stop_triggers, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //getActivity().getWindow().setSoftInputMode(
        //        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        startTriggerSpinner = (Spinner) getActivity().findViewById(R.id.startTriggerSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        startTriggerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.start_trigger_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        startTriggerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        startTriggerSpinner.setAdapter(startTriggerAdapter);

        stopTriggerSpinner = (Spinner) getActivity().findViewById(R.id.stopTriggerSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        stopTriggerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.stop_trigger_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        stopTriggerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        stopTriggerSpinner.setAdapter(stopTriggerAdapter);

        if (RFIDController.settings_startTrigger != null) {
            start_trigger_type = RFIDController.settings_startTrigger.getTriggerType();
            stop_trigger_type = RFIDController.settings_stopTrigger.getTriggerType();
        } else {
            start_trigger_type = START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE;
            stop_trigger_type = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE;
        }

        if (start_trigger_type == START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE)
            startTriggerSpinner.setSelection(0, false);
        else if (start_trigger_type == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD)
            startTriggerSpinner.setSelection(1, false);
        else if (start_trigger_type == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC)
            startTriggerSpinner.setSelection(2, false);

        if (stop_trigger_type == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT)
            stopTriggerSpinner.setSelection(1, false);
        else if (stop_trigger_type == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_DURATION)
            stopTriggerSpinner.setSelection(2, false);
        else if (stop_trigger_type == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT)
            stopTriggerSpinner.setSelection(3, false);
        else if (stop_trigger_type == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_N_ATTEMPTS_WITH_TIMEOUT)
            stopTriggerSpinner.setSelection(4, false);
        else
            stopTriggerSpinner.setSelection(0, false);

        startTriggerSpinner.setOnItemSelectedListener(this);
        stopTriggerSpinner.setOnItemSelectedListener(this);

        startPeriodic = ((EditText) getActivity().findViewById(R.id.startPeriodic));
        startPeriodic.setFilters(new InputFilter[]{new InputFilterMax()});

        startHandHeldTriggerReleased = ((CheckBox) getActivity().findViewById(R.id.startHandHeldTriggerReleased));
        startHandHeldTriggerPressed = ((CheckBox) getActivity().findViewById(R.id.startHandHeldTriggerPressed));
        startHandHeldTriggerReleased.setOnCheckedChangeListener(this);
        startHandHeldTriggerPressed.setOnCheckedChangeListener(this);

        stopHandHeldTriggerReleased = ((CheckBox) getActivity().findViewById(R.id.stopHandHeldTriggerReleased));
        stopHandHeldTriggerPressed = ((CheckBox) getActivity().findViewById(R.id.stopHandHeldTriggerPressed));
        stopHandHeldTriggerReleased.setOnCheckedChangeListener(this);
        stopHandHeldTriggerPressed.setOnCheckedChangeListener(this);

        stopHandheldDuration = ((EditText) getActivity().findViewById(R.id.stopHandheldDuration));
        stopDuration = ((EditText) getActivity().findViewById(R.id.stopDuration));
        stopTagObserve = ((EditText) getActivity().findViewById(R.id.stopTagObserve));
        stopTagObserveTimeout = ((EditText) getActivity().findViewById(R.id.stopTagObserveTimeout));
        stopNObserveAttempts = ((EditText) getActivity().findViewById(R.id.stopNObserveAttempts));
        stopNObserveTimeout = ((EditText) getActivity().findViewById(R.id.stopNObserveTimeout));

        stopHandheldDuration.setFilters(new InputFilter[]{new InputFilterMax()});
        stopDuration.setFilters(new InputFilter[]{new InputFilterMax()});
        stopTagObserve.setFilters(new InputFilter[]{new InputFilterMax()});
        stopTagObserveTimeout.setFilters(new InputFilter[]{new InputFilterMax()});
        stopNObserveAttempts.setFilters(new InputFilter[]{new InputFilterMax()});
        stopNObserveTimeout.setFilters(new InputFilter[]{new InputFilterMax()});

        loadTriggerStates();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Method to load the trigger states
     */
    private void loadTriggerStates() {

        if (startTriggerSpinner.getSelectedItem().toString().equalsIgnoreCase(HANDHELD)) {
            getActivity().findViewById(R.id.startHandheldLayout).setVisibility(View.VISIBLE);
            if (RFIDController.settings_startTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                startHandHeldTriggerPressed.setChecked(true);
                startHandHeldTriggerReleased.setChecked(false);
            } else if (RFIDController.settings_startTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                startHandHeldTriggerPressed.setChecked(false);
                startHandHeldTriggerReleased.setChecked(true);
            }
        } else if (startTriggerSpinner.getSelectedItem().toString().equalsIgnoreCase(PERIODIC)) {
            startTriggerSpinner.setSelection(2);
            getActivity().findViewById(R.id.startPeriodicLayout).setVisibility(View.VISIBLE);
            startPeriodic.setText(RFIDController.settings_startTrigger.Periodic.getPeriod() + "");
        }

        if (stopTriggerSpinner.getSelectedItem().toString().equalsIgnoreCase(HANDHELD)) {
            getActivity().findViewById(R.id.stopHandheldLayout).setVisibility(View.VISIBLE);
            if (RFIDController.settings_stopTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                stopHandHeldTriggerReleased.setChecked(false);
                stopHandHeldTriggerPressed.setChecked(true);
            } else if (RFIDController.settings_stopTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                stopHandHeldTriggerReleased.setChecked(true);
                stopHandHeldTriggerPressed.setChecked(false);
            }
            stopHandheldDuration.setText(RFIDController.settings_stopTrigger.Handheld.getHandheldTriggerTimeout() + "");
        } else if (stopTriggerSpinner.getSelectedItem().toString().equalsIgnoreCase(DURATION)) {
            getActivity().findViewById(R.id.stopDurationLayout).setVisibility(View.VISIBLE);
            stopDuration.setText(RFIDController.settings_stopTrigger.getDurationMilliSeconds() + "");
        } else if (stopTriggerSpinner.getSelectedItem().toString().equalsIgnoreCase(TAG_OBSERVATION)) {
            getActivity().findViewById(R.id.stopTagObserveLayout).setVisibility(View.VISIBLE);
            stopTagObserve.setText(RFIDController.settings_stopTrigger.TagObservation.getN() + "");
            stopTagObserveTimeout.setText(RFIDController.settings_stopTrigger.TagObservation.getTimeout() + "");
        } else if (stopTriggerSpinner.getSelectedItem().toString().equalsIgnoreCase(N_ATTEMPTS)) {
            getActivity().findViewById(R.id.stopNObserveLayout).setVisibility(View.VISIBLE);
            stopNObserveAttempts.setText(RFIDController.settings_stopTrigger.NumAttempts.getN() + "");
            stopNObserveTimeout.setText(RFIDController.settings_stopTrigger.NumAttempts.getTimeout() + "");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        if (adapterView == getActivity().findViewById(R.id.startTriggerSpinner)) {
            switch (pos) {
                case 1:
                    getActivity().findViewById(R.id.startHandheldLayout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.startPeriodicLayout).setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    getActivity().findViewById(R.id.startPeriodicLayout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.startHandheldLayout).setVisibility(View.INVISIBLE);
                    break;
                default:
                    getActivity().findViewById(R.id.startHandheldLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.startPeriodicLayout).setVisibility(View.INVISIBLE);
                    break;
            }
            /*((SettingsDetailActivity)getActivity()).sendStartTriggerCommand();*/
        } else if (adapterView == getActivity().findViewById(R.id.stopTriggerSpinner)) {
            switch (pos) {
                case 1:
                    getActivity().findViewById(R.id.stopHandheldLayout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.stopDurationLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopNObserveLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopTagObserveLayout).setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    getActivity().findViewById(R.id.stopDurationLayout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.stopHandheldLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopNObserveLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopTagObserveLayout).setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    getActivity().findViewById(R.id.stopTagObserveLayout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.stopDurationLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopHandheldLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopNObserveLayout).setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    getActivity().findViewById(R.id.stopNObserveLayout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.stopTagObserveLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopDurationLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopHandheldLayout).setVisibility(View.INVISIBLE);
                    break;
                default:
                    getActivity().findViewById(R.id.stopNObserveLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopTagObserveLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopDurationLayout).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.stopHandheldLayout).setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

  /*  @Override
    public void handleStatusResponse(final Response_Status statusData) {
        String command = statusData.command.trim();
        if (command.equalsIgnoreCase(Constants.COMMAND_STARTTRIGGER) || command.equalsIgnoreCase(Constants.COMMAND_STOPTRIGGER))
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
    }

    */

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /**
     * Method to be called when back button is pressed by the user
     */

    @Override
    public void onBackPressed() {
        if (!isSettingsChanged()) {
            //((SettingsDetailActivity) getActivity()).callBackPressed();
            AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
            replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
        }
    }

    private StartTrigger getDefaultStartTrigger() {
        StartTrigger tempStartTrigger = null;
        try {
            tempStartTrigger = RFIDController.mConnectedReader.Config.getStartTrigger();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
            ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());
        }
        return tempStartTrigger;
    }

    private StopTrigger getDefaultStopTrigger() {
        StopTrigger tempStopTrigger = null;
        try {
            tempStopTrigger = RFIDController.mConnectedReader.Config.getStopTrigger();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
            ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());
        }
        return tempStopTrigger;
    }

    /**
     * method to know whether start/stop trigger settings has changed
     *
     * @return true if settings has changed or false if settings has not changed
     */
    private boolean isSettingsChanged() {
        boolean isSettingsChanged = false;
        if (RFIDController.mConnectedReader == null || RFIDController.settings_startTrigger == null)
            return false;
        String startTriggerst = startTriggerSpinner.getSelectedItem().toString();
        String stopTriggerst = stopTriggerSpinner.getSelectedItem().toString();
        StartTrigger tempStartTrigger = null;
        StopTrigger tempStopTrigger = null;

        if (((!startTriggerst.isEmpty()) || (!stopTriggerst.isEmpty())) && startTriggerSpinner.getSelectedItem() != null && stopTriggerSpinner.getSelectedItem() != null) {
            switch (startTriggerSpinner.getSelectedItemPosition()) {
                case 1:
                    if (startHandHeldTriggerPressed.isChecked() || startHandHeldTriggerReleased.isChecked()) {
                        if (((RFIDController.settings_startTrigger.getTriggerType() != START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD) && (startHandHeldTriggerPressed.isChecked() || startHandHeldTriggerReleased.isChecked())) ||
                                ((startHandHeldTriggerPressed.isChecked() && RFIDController.settings_startTrigger.Handheld.getHandheldTriggerEvent() != HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) ||
                                        (startHandHeldTriggerReleased.isChecked() && RFIDController.settings_startTrigger.Handheld.getHandheldTriggerEvent() != HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED))) {
                            isSettingsChanged = true;
                            tempStartTrigger = getDefaultStartTrigger();
                            if (tempStartTrigger != null) {
                                tempStartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD);
                                tempStartTrigger.Handheld.setHandheldTriggerEvent(startHandHeldTriggerPressed.isChecked() ?
                                        HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED : HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED);
                            } else
                                return false;
                        }
                    } else {
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_empty_fields_start_trigger));
                    }
                    break;
                case 2:
                    if (!startPeriodic.getText().toString().isEmpty()) {
                        Long periodic = Long.parseLong(startPeriodic.getText().toString());
                        if ((RFIDController.settings_startTrigger.getTriggerType() != START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC) || RFIDController.settings_startTrigger.Periodic.getPeriod() != periodic) {
                            isSettingsChanged = true;
                            tempStartTrigger = getDefaultStartTrigger();
                            if (tempStartTrigger != null) {
                                tempStartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC);
                                tempStartTrigger.Periodic.setPeriod(periodic.intValue());
                            } else
                                return false;
                        }
                    } else {
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_empty_fields_start_trigger));
                    }
                    break;
                default:
                    if (RFIDController.settings_startTrigger.getTriggerType() != START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE) {
                        isSettingsChanged = true;
                        tempStartTrigger = getDefaultStartTrigger();
                        if (tempStartTrigger != null) {
                            tempStartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
                        } else
                            return false;
                    }

            }

            switch (stopTriggerSpinner.getSelectedItemPosition()) {
                case 1:
                    if (!stopHandheldDuration.getText().toString().isEmpty() && (stopHandHeldTriggerReleased.isChecked() || stopHandHeldTriggerPressed.isChecked())) {
                        Long timeout = Long.parseLong(stopHandheldDuration.getText().toString());
                        if ((RFIDController.settings_stopTrigger.getTriggerType() != STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT) ||
                                (stopHandHeldTriggerReleased.isChecked() && RFIDController.settings_stopTrigger.Handheld.getHandheldTriggerEvent() != HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) ||
                                (stopHandHeldTriggerPressed.isChecked() && RFIDController.settings_stopTrigger.Handheld.getHandheldTriggerEvent() != HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) ||
                                (RFIDController.settings_stopTrigger.Handheld.getHandheldTriggerTimeout() != timeout)) {
                            isSettingsChanged = true;
                            tempStopTrigger = getDefaultStopTrigger();
                            if (tempStopTrigger != null) {
                                tempStopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT);
                                tempStopTrigger.Handheld.setHandheldTriggerEvent(stopHandHeldTriggerPressed.isChecked() ? HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED : HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED);
                                tempStopTrigger.Handheld.setHandheldTriggerTimeout(timeout.intValue());
                            } else
                                return false;
                        }
                    } else {
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_empty_fields_stop_trigger));
                    }
                    break;
                case 2:
                    if (!stopDuration.getText().toString().isEmpty()) {
                        Long duration = Long.parseLong(stopDuration.getText().toString());
                        if ((RFIDController.settings_stopTrigger.getTriggerType() != STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_DURATION) || RFIDController.settings_stopTrigger.getDurationMilliSeconds() != duration) {
                            isSettingsChanged = true;
                            tempStopTrigger = getDefaultStopTrigger();
                            if (tempStopTrigger != null) {
                                tempStopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_DURATION);
                                tempStopTrigger.setDurationMilliSeconds(duration.intValue());
                            } else
                                return false;
                        }
                    } else {
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_empty_fields_stop_trigger));
                    }
                    break;
                case 3:
                    if (!stopTagObserve.getText().toString().isEmpty() && !stopTagObserveTimeout.getText().toString().isEmpty()) {
                        Long tagObservation = Long.parseLong(stopTagObserve.getText().toString());
                        Long tagTimeOut = Long.parseLong(stopTagObserveTimeout.getText().toString());
                        if ((RFIDController.settings_stopTrigger.getTriggerType() != STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT) || (RFIDController.settings_stopTrigger.TagObservation.getN() != tagObservation) || (RFIDController.settings_stopTrigger.TagObservation.getTimeout() != tagTimeOut)) {
                            isSettingsChanged = true;
                            tempStopTrigger = getDefaultStopTrigger();
                            if (tempStopTrigger != null) {
                                tempStopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT);
                                tempStopTrigger.TagObservation.setN(tagObservation.shortValue());
                                tempStopTrigger.TagObservation.setTimeout(tagTimeOut.intValue());
                            } else
                                return false;
                        }
                    } else {
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_empty_fields_stop_trigger));
                    }
                    break;
                case 4:
                    if (!stopNObserveAttempts.getText().toString().isEmpty() && !stopNObserveTimeout.getText().toString().isEmpty()) {
                        Long nAttempts = Long.parseLong(stopNObserveAttempts.getText().toString());
                        Long nTimeOut = Long.parseLong(stopNObserveTimeout.getText().toString());
                        if ((RFIDController.settings_stopTrigger.getTriggerType() != STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_N_ATTEMPTS_WITH_TIMEOUT) || (RFIDController.settings_stopTrigger.NumAttempts.getN() != nAttempts) || (RFIDController.settings_stopTrigger.NumAttempts.getTimeout() != nTimeOut)) {
                            isSettingsChanged = true;
                            tempStopTrigger = getDefaultStopTrigger();
                            if (tempStopTrigger != null) {
                                tempStopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_N_ATTEMPTS_WITH_TIMEOUT);
                                tempStopTrigger.NumAttempts.setN(nAttempts.shortValue());
                                tempStopTrigger.NumAttempts.setTimeout(nTimeOut.intValue());
                            } else
                                return false;
                        }
                    } else {
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_empty_fields_stop_trigger));
                    }
                    break;

                default:
                    if (RFIDController.settings_stopTrigger.getTriggerType() != STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE) {
                        isSettingsChanged = true;
                        tempStopTrigger = getDefaultStopTrigger();
                        if (tempStopTrigger != null) {
                            tempStopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
                        } else
                            return false;
                    }
            }
        }
        Log.d("TRIGGER", isSettingsChanged + "");

        if (isSettingsChanged)
            new Task_SaveTriggerConfiguration(tempStartTrigger, tempStopTrigger).execute();
        return isSettingsChanged;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == startHandHeldTriggerReleased && compoundButton.isChecked())
            startHandHeldTriggerPressed.setChecked(false);
        else if (compoundButton == startHandHeldTriggerPressed && startHandHeldTriggerPressed.isChecked())
            startHandHeldTriggerReleased.setChecked(false);
        else if (compoundButton == stopHandHeldTriggerReleased && stopHandHeldTriggerReleased.isChecked())
            stopHandHeldTriggerPressed.setChecked(false);
        else if (compoundButton == stopHandHeldTriggerPressed && stopHandHeldTriggerPressed.isChecked())
            stopHandHeldTriggerReleased.setChecked(false);
    }

    private class Task_SaveTriggerConfiguration extends AsyncTask<Void, Void, Boolean> {
        private final StartTrigger fnStartTrigger;
        private final StopTrigger fnStopTrigger;
        private CustomProgressDialog progressDialog;
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;

        public Task_SaveTriggerConfiguration(StartTrigger startTrigger, StopTrigger stopTrigger) {
            this.fnStartTrigger = startTrigger;
            this.fnStopTrigger = stopTrigger;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.start_stop_progress_title));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            try {
                if (fnStartTrigger != null) {
                    RFIDController.mConnectedReader.Config.setStartTrigger(fnStartTrigger);
                    RFIDController.settings_startTrigger = fnStartTrigger;
                }
                if (fnStopTrigger != null) {
                    RFIDController.mConnectedReader.Config.setStopTrigger(fnStopTrigger);
                    RFIDController.settings_stopTrigger = fnStopTrigger;
                }
                result = true;
            } catch (InvalidUsageException e) {
                e.printStackTrace();
                invalidUsageException = e;
            } catch (OperationFailureException e) {
                e.printStackTrace();
                operationFailureException = e;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.cancel();
            super.onPostExecute(result);
            if (!result) {
                if (invalidUsageException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                if (operationFailureException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
            } else
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            //((SettingsDetailActivity) getActivity()).callBackPressed();
            AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
            replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
        }
    }

}
