package com.zebra.rfidreader.demo.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.zebra.rfid.api3.BATCH_MODE;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFModeTableEntry;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.util.ArrayList;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link SaveConfigurationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle save configuration operation and UI
 */
public class SaveConfigurationsFragment extends BackPressedFragment {
    ArrayList<String> linkedProfiles = new ArrayList<>();
    private TextView antennaPower;
    private TextView linkProfile;
    private TextView session;
    private TextView startTrigger;
    private TextView stopTrigger;
    private TextView tagPopulation;
    private TextView invState;
    private TextView slFlag;
    private TextView saveIncPC;
    private TextView saveIncRSSI;
    private TextView saveIncPhase;
    private TextView saveIncChannel;
    private LinearLayout saveStartPeriodicLayout;
    private LinearLayout saveStartHandheldLayout;
    private TableRow saveStopTimeOutLayout;
    private LinearLayout saveStopNObserveAttemptsLayout;
    private LinearLayout saveStopTagObserveLayout;
    private LinearLayout saveStopHandheldLayout;
    private TextView saveStopTriggerReleased;
    private TextView saveStopTriggerPressed;
    private LinearLayout saveStopDurationLayout;
    private TextView saveStartTriggerReleased;
    private TextView saveStartTriggerPressed;
    private TextView saveSledBeeper;
    private TextView saveHostBeeper;
    private TextView saveSledBeeperVolume;
    //private TextView saveRegion;
    private TextView saveIncTagSeenCount;
    private TextView savebatchMode;
    private TextView saveDPO;
    private TextView reportUniqueTags;

    public SaveConfigurationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SaveConfigurationsFragment.
     */
    public static SaveConfigurationsFragment newInstance() {
        return new SaveConfigurationsFragment();
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
        return inflater.inflate(R.layout.fragment_save_configurations, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    /**
     * Method to initialize the data to be shown
     */
    private void loadData() {
        antennaPower = (TextView) getActivity().findViewById(R.id.antennaPower);
        linkProfile = (TextView) getActivity().findViewById(R.id.saveLinkProfile);
        session = (TextView) getActivity().findViewById(R.id.saveSession);
        tagPopulation = (TextView) getActivity().findViewById(R.id.tagPopulation);
        invState = (TextView) getActivity().findViewById(R.id.invState);
        slFlag = (TextView) getActivity().findViewById(R.id.saveSlFlag);
        saveIncPC = (TextView) getActivity().findViewById(R.id.saveIncPC);
        saveIncRSSI = (TextView) getActivity().findViewById(R.id.saveIncRSSI);
        saveIncPhase = (TextView) getActivity().findViewById(R.id.saveIncPhase);
        saveIncChannel = (TextView) getActivity().findViewById(R.id.saveIncChannel);
        saveIncTagSeenCount = (TextView) getActivity().findViewById(R.id.saveIncTagSeenCount);
        savebatchMode = (TextView) getActivity().findViewById(R.id.savebatchMode);
        saveDPO = (TextView) getActivity().findViewById(R.id.saveDPO);
        reportUniqueTags = (TextView) getActivity().findViewById(R.id.reportUniqueTags);
        startTrigger = (TextView) getActivity().findViewById(R.id.saveStartTrigger);
        saveStartPeriodicLayout = (LinearLayout) getActivity().findViewById(R.id.saveStartPeriodicLayout);
        saveStartHandheldLayout = (LinearLayout) getActivity().findViewById(R.id.saveStartHandheldLayout);
        stopTrigger = (TextView) getActivity().findViewById(R.id.saveStopTrigger);
        saveStopDurationLayout = (LinearLayout) getActivity().findViewById(R.id.saveStopDurationLayout);
        saveStopHandheldLayout = (LinearLayout) getActivity().findViewById(R.id.saveStopHandheldLayout);
        saveStopTagObserveLayout = (LinearLayout) getActivity().findViewById(R.id.saveStopTagObserveLayout);
        saveStopNObserveAttemptsLayout = (LinearLayout) getActivity().findViewById(R.id.saveStopNObserveAttemptsLayout);
        saveStopTimeOutLayout = (TableRow) getActivity().findViewById(R.id.saveStopTimeOutLayout);
        saveSledBeeper = (TextView) getActivity().findViewById(R.id.saveSledBeeper);
        saveHostBeeper = (TextView) getActivity().findViewById(R.id.saveHostBeeper);
        saveSledBeeperVolume = (TextView) getActivity().findViewById(R.id.saveSledBeeperVolume);
        //Set Anntenna settings detals
        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected() && RFIDController.mConnectedReader.isCapabilitiesReceived()) {
            try {

                if (!RFIDController.mIsInventoryRunning)
                    RFIDController.antennaRfConfig = RFIDController.mConnectedReader.Config.Antennas.getAntennaRfConfig(1);

                RFIDController.rfModeTable = RFIDController.mConnectedReader.ReaderCapabilities.RFModes.getRFModeTableInfo(0);
                RFIDController.antennaPowerLevel = RFIDController.mConnectedReader.ReaderCapabilities.getTransmitPowerLevelValues();


                getLinkedProfiles(linkedProfiles);
                if (RFIDController.antennaRfConfig != null) {
                    antennaPower.setText(String.valueOf(RFIDController.antennaPowerLevel[RFIDController.antennaRfConfig.getTransmitPowerIndex()]));
                    linkProfile.setText(linkedProfiles.get(getSelectedLinkedProfilePosition(RFIDController.antennaRfConfig.getrfModeTableIndex())));
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
        //Singulation settings detail
        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected() && RFIDController.mConnectedReader.isCapabilitiesReceived()) {
            try {

                if (!RFIDController.mIsInventoryRunning)
                    RFIDController.singulationControl = RFIDController.mConnectedReader.Config.Antennas.getSingulationControl(1);
                session.setText(getResources().getStringArray(R.array.session_array)[RFIDController.singulationControl.getSession().getValue()]);
                if (RFIDController.singulationControl.getTagPopulation() == 30)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[0]);
                if (RFIDController.singulationControl.getTagPopulation() == 100)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[1]);
                if (RFIDController.singulationControl.getTagPopulation() == 200)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[2]);
                if (RFIDController.singulationControl.getTagPopulation() == 300)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[3]);
                if (RFIDController.singulationControl.getTagPopulation() == 400)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[4]);
                if (RFIDController.singulationControl.getTagPopulation() == 500)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[5]);
                if (RFIDController.singulationControl.getTagPopulation() == 600)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[6]);
                invState.setText(getResources().getStringArray(R.array.inventory_state_array)[RFIDController.singulationControl.Action.getInventoryState().getValue()]);
                switch (RFIDController.singulationControl.Action.getSLFlag().getValue()) {
                    case 0:
                        slFlag.setText(getResources().getStringArray(R.array.sl_flags_array)[2]);
                        break;
                    case 1:
                        slFlag.setText(getResources().getStringArray(R.array.sl_flags_array)[1]);
                        break;
                    case 2:
                        slFlag.setText(getResources().getStringArray(R.array.sl_flags_array)[0]);
                        break;
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
        //Tag Report settings Details
        if (RFIDController.tagStorageSettings != null) {
            TAG_FIELD[] tag_field = RFIDController.tagStorageSettings.getTagFields();
            for (int idx = 0; idx < tag_field.length; idx++) {
                if (tag_field[idx] == TAG_FIELD.PEAK_RSSI)
                    saveIncRSSI.setText(Constants.ON);
                if (tag_field[idx] == TAG_FIELD.PHASE_INFO)
                    saveIncPhase.setText(Constants.ON);
                if (tag_field[idx] == TAG_FIELD.PC)
                    saveIncPC.setText(Constants.ON);
                if (tag_field[idx] == TAG_FIELD.CHANNEL_INDEX)
                    saveIncChannel.setText(Constants.ON);
                if (tag_field[idx] == TAG_FIELD.TAG_SEEN_COUNT)
                    saveIncTagSeenCount.setText(Constants.ON);
            }
        }
        if (RFIDController.batchMode != -1) {
            savebatchMode.setText(getResources().getStringArray(R.array.batch_modes_array)[RFIDController.batchMode]);
        }
        //Power management detail
        if (RFIDController.dynamicPowerSettings != null) {
            if (RFIDController.dynamicPowerSettings.getValue() == 1) {
                saveDPO.setText(Constants.ON);
            } else
                saveDPO.setText(Constants.OFF);
        }
        //Unique Tag Report Settings
        if (RFIDController.reportUniquetags != null) {
            if (RFIDController.reportUniquetags.getValue() == 1) {
                reportUniqueTags.setText(Constants.ON);
            } else {
                reportUniqueTags.setText(Constants.OFF);
            }
        }
        //Start / Stop Trgger details if (RFIDController.setStartTriggerSettings != null) {
        if (RFIDController.settings_startTrigger != null) {
            saveStartHandheldLayout.setVisibility(View.GONE);
            saveStartPeriodicLayout.setVisibility(View.GONE);
            if (RFIDController.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE) {
                startTrigger.setText(Constants.IMMEDIATE);
            } else if (RFIDController.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC) {
                startTrigger.setText(Constants.PERIODIC);
                saveStartPeriodicLayout.setVisibility(View.VISIBLE);
                ((TextView) getActivity().findViewById(R.id.saveStartPeriodic)).setText(String.valueOf(RFIDController.settings_startTrigger.Periodic.getPeriod()));
            } else if (RFIDController.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD) {
                startTrigger.setText(Constants.HANDHELD);
                saveStartHandheldLayout.setVisibility(View.VISIBLE);
                saveStartTriggerPressed = (TextView) getActivity().findViewById(R.id.saveStartTriggerPressed);
                saveStartTriggerReleased = (TextView) getActivity().findViewById(R.id.saveStartTriggerReleased);
                ((TableRow) getActivity().findViewById(R.id.saveStartTriggerPressedRow)).setVisibility(View.GONE);
                ((TableRow) getActivity().findViewById(R.id.saveStartTriggerReleasedRow)).setVisibility(View.GONE);
                if (RFIDController.settings_startTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    ((TableRow) getActivity().findViewById(R.id.saveStartTriggerPressedRow)).setVisibility(View.VISIBLE);
                    saveStartTriggerPressed.setText(Constants.ON);
                } else {
                    ((TableRow) getActivity().findViewById(R.id.saveStartTriggerReleasedRow)).setVisibility(View.VISIBLE);
                    saveStartTriggerReleased.setText(Constants.ON);
                }
            }
        }
        if (RFIDController.settings_stopTrigger != null) {
            saveStopDurationLayout.setVisibility(View.GONE);
            saveStopHandheldLayout.setVisibility(View.GONE);
            saveStopTagObserveLayout.setVisibility(View.GONE);
            saveStopNObserveAttemptsLayout.setVisibility(View.GONE);
            saveStopTimeOutLayout.setVisibility(View.GONE);
            if (RFIDController.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE) {
                stopTrigger.setText(Constants.IMMEDIATE);
            } else if (RFIDController.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT) {
                stopTrigger.setText(Constants.HANDHELD);
                saveStopHandheldLayout.setVisibility(View.VISIBLE);
                saveStopTimeOutLayout.setVisibility(View.VISIBLE);
                saveStopTriggerPressed = (TextView) getActivity().findViewById(R.id.saveStopTriggerPressed);
                saveStopTriggerReleased = (TextView) getActivity().findViewById(R.id.saveStopTriggerReleased);
                ((TableRow) getActivity().findViewById(R.id.saveStopTriggerPressedRow)).setVisibility(View.GONE);
                ((TableRow) getActivity().findViewById(R.id.saveStopTriggerReleasedRow)).setVisibility(View.GONE);
                if (RFIDController.settings_stopTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    ((TableRow) getActivity().findViewById(R.id.saveStopTriggerPressedRow)).setVisibility(View.VISIBLE);
                    saveStopTriggerPressed.setText(Constants.ON);
                } else {
                    ((TableRow) getActivity().findViewById(R.id.saveStopTriggerReleasedRow)).setVisibility(View.VISIBLE);
                    saveStopTriggerReleased.setText(Constants.ON);
                }
                ((TextView) getActivity().findViewById(R.id.saveStopTimeOut)).setText(String.valueOf(RFIDController.settings_stopTrigger.Handheld.getHandheldTriggerTimeout()));

            } else if (RFIDController.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_DURATION) {
                stopTrigger.setText(Constants.DURATION);
                saveStopDurationLayout.setVisibility(View.VISIBLE);
                ((TextView) getActivity().findViewById(R.id.saveStopDuration)).setText(String.valueOf(RFIDController.settings_stopTrigger.getDurationMilliSeconds()));
            } else if (RFIDController.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT) {
                stopTrigger.setText(Constants.TAG_OBSERVATION);
                saveStopTagObserveLayout.setVisibility(View.VISIBLE);
                saveStopTimeOutLayout.setVisibility(View.VISIBLE);
                ((TextView) getActivity().findViewById(R.id.saveStopTagObserve)).setText(String.valueOf(RFIDController.settings_stopTrigger.TagObservation.getN()));
                ((TextView) getActivity().findViewById(R.id.saveStopTimeOut)).setText(String.valueOf(RFIDController.settings_stopTrigger.TagObservation.getTimeout()));
            } else if (RFIDController.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_N_ATTEMPTS_WITH_TIMEOUT) {
                stopTrigger.setText(Constants.N_ATTEMPTS);
                saveStopNObserveAttemptsLayout.setVisibility(View.VISIBLE);
                saveStopTimeOutLayout.setVisibility(View.VISIBLE);
                ((TextView) getActivity().findViewById(R.id.saveStopNObserveAttempts)).setText(String.valueOf(RFIDController.settings_stopTrigger.NumAttempts.getN()));
                ((TextView) getActivity().findViewById(R.id.saveStopTimeOut)).setText(String.valueOf(RFIDController.settings_stopTrigger.NumAttempts.getTimeout()));
            }
        }
        //Beeper settings Detail
        try {
            if (RFIDController.mConnectedReader != null) {
                if (!RFIDController.mIsInventoryRunning)
                    RFIDController.sledBeeperVolume = RFIDController.mConnectedReader.Config.getBeeperVolume();
                if (RFIDController.mConnectedReader.Config.getBeeperVolume() != null) {
                    if (RFIDController.mConnectedReader.Config.getBeeperVolume().equals(BEEPER_VOLUME.QUIET_BEEP)) {
                        saveSledBeeper.setText(Constants.OFF);
                        if (!RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                            saveHostBeeper.setText(Constants.ON);

                        }
                    } else if (!RFIDController.mConnectedReader.Config.getBeeperVolume().equals(BEEPER_VOLUME.QUIET_BEEP)) {
                        saveSledBeeper.setText(Constants.ON);


                    }
                }
            }


        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
        if (RFIDController.beeperVolume != null) {
            if (RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                if (RFIDController.beeperspinner_status != 3)
                    saveSledBeeperVolume.setText(
                            getResources().getStringArray(R.array.beeper_volume_array)
                                    [RFIDController.beeperspinner_status]);

            } else if (!RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                if (RFIDController.mConnectedReader != null) {
                    if (!RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
                        saveSledBeeper.setText(Constants.OFF);
                        saveSledBeeperVolume.setText(
                                getResources().getStringArray(R.array.beeper_volume_array)
                                        [RFIDController.beeperVolume.getValue()]);
                    }
                }
            }
        }
        try {
            if (RFIDController.mConnectedReader != null) {
                if (!RFIDController.mIsInventoryRunning)
                    RFIDController.sledBeeperVolume = RFIDController.mConnectedReader.Config.getBeeperVolume();
                if (RFIDController.mConnectedReader.Config.getBeeperVolume() != null) {
                    if (RFIDController.mConnectedReader.Config.getBeeperVolume().equals(BEEPER_VOLUME.QUIET_BEEP)) {
                        if (!RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                            saveSledBeeperVolume.setText(
                                    getResources().getStringArray(R.array.beeper_volume_array)
                                            [RFIDController.beeperVolume.getValue()]);


                        } else {
                            if (RFIDController.beeperspinner_status != 3)
                                saveSledBeeperVolume.setText(
                                        getResources().getStringArray(R.array.beeper_volume_array)
                                                [RFIDController.beeperspinner_status]);
                        }
                    } else if (!RFIDController.mConnectedReader.Config.getBeeperVolume().equals(BEEPER_VOLUME.QUIET_BEEP)) {
                        if (RFIDController.beeperVolume.equals(BEEPER_VOLUME.QUIET_BEEP)) {
                            saveSledBeeperVolume.setText(
                                    getResources().getStringArray(R.array.beeper_volume_array)
                                            [RFIDController.mConnectedReader.Config.getBeeperVolume().getValue()]);

                        } else {
                            saveSledBeeperVolume.setText(
                                    getResources().getStringArray(R.array.beeper_volume_array)
                                            [RFIDController.beeperVolume.getValue()]);

                        }
                    }
                }
            }


        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }


    }

    private int getSelectedLinkedProfilePosition(long rfModeTableIndex) {
        RFModeTableEntry rfModeTableEntry = null;
        for (int ix = 0; ix < RFIDController.rfModeTable.length(); ix++) {
            rfModeTableEntry = RFIDController.rfModeTable.getRFModeTableEntryInfo(ix);
            if (rfModeTableEntry.getModeIdentifer() == rfModeTableIndex)
                return ix;//linkAdapter.getPosition(rfModeTableEntry.getBdrValue()+" "+rfModeTableEntry.getModulation()+" "+rfModeTableEntry.getPieValue()+" "+rfModeTableEntry.getMaxTariValue()+" "+rfModeTableEntry.getMaxTariValue()+" "+rfModeTableEntry.getStepTariValue());
        }
        return 0;
    }

    private void getLinkedProfiles(ArrayList<String> linkedProfiles) {
        RFModeTableEntry rfModeTableEntry = null;
        for (int i = 0; i < RFIDController.rfModeTable.length(); i++) {
            rfModeTableEntry = RFIDController.rfModeTable.getRFModeTableEntryInfo(i);
            linkedProfiles.add(rfModeTableEntry.getBdrValue() + " " + rfModeTableEntry.getModulation() + " " + rfModeTableEntry.getPieValue() + " " + rfModeTableEntry.getMinTariValue() + " " + rfModeTableEntry.getMaxTariValue() + " " + rfModeTableEntry.getStepTariValue());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void deviceConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
    }

    public void deviceDisconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Set Anntenna settings detals
                antennaPower.setText("");
                linkProfile.setText("");
                //Singulation settings detail
                if (RFIDController.singulationControl != null) {
                    session.setText("");
                    tagPopulation.setText("");
                    invState.setText("");
                    slFlag.setText("");
                    slFlag.setText("");
                }
                saveIncRSSI.setText(Constants.OFF);
                saveIncPhase.setText(Constants.OFF);
                saveIncPC.setText(Constants.OFF);
                saveIncChannel.setText(Constants.OFF);
                saveIncTagSeenCount.setText(Constants.OFF);
                reportUniqueTags.setText(Constants.OFF);
                savebatchMode.setText(getResources().getStringArray(R.array.batch_modes_array)[BATCH_MODE.AUTO.getValue()]);
                saveDPO.setText("");
                startTrigger.setText("");
                stopTrigger.setText("");
                saveSledBeeper.setText("");
                saveSledBeeperVolume.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
        replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
    }

}
