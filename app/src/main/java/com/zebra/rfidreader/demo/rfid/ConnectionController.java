package com.zebra.rfidreader.demo.rfid;

import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.VersionInfo;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.LinkProfileUtil;

import java.util.ArrayList;

import static com.zebra.rfidreader.demo.settings.ProfileContent.UpdateActiveProfile;
import static com.zebra.rfidreader.demo.settings.ProfileContent.UpdateProfilesForRegulatory;

public class ConnectionController {


    protected ConnectionController() {
    }


    public void AutoConnectDevice(final String password, final RfidEventsListener rfidEventsListener, final RfidListeners rfidListeners, final UpdateUIListener updateUIListener) {
        RFIDController.autoConnectDeviceTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onCancelled() {
                super.onCancelled();
                RFIDController.autoConnectDeviceTask = null;
                rfidListeners.onFailure((String) null);
            }

            OperationFailureException exception;
            InvalidUsageException exceptionIN;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    if (RFIDController.readers != null && RFIDController.mConnectedReader == null/* && LAST_CONNECTED_READER.startsWith("RFD8500")*/) {
                        if (RFIDController.readers.GetAvailableRFIDReaderList() != null) {
                            RFIDController.mConnectedDevice = getConnectedDeviceFromRFIDReaderList(RFIDController.LAST_CONNECTED_READER);
                            if (RFIDController.mConnectedDevice != null) {
                                RFIDController.mConnectedReader = RFIDController.mConnectedDevice.getRFIDReader();
                                try {
                                    if (!RFIDController.mConnectedReader.isConnected() && !this.isCancelled()) {
                                        updateUIListener.updateProgressMessage(RFIDController.mConnectedReader.getHostName());
                                        RFIDController.mConnectedReader.setPassword(password);
                                        RFIDController.mConnectedReader.connect();
                                    } else {
                                        this.cancel(true);
                                    }
                                } catch (NullPointerException e) {
                                    Log.d(RFIDController.TAG, "null pointer ");
                                    e.printStackTrace();
                                } catch (InvalidUsageException e) {
                                    e.printStackTrace();
                                } //catch (OperationFailureException e) {
//                                    e.printStackTrace();
//                                    exception = e;
//                                }
                                try {
                                    if (RFIDController.mConnectedReader.Events != null) {
                                        RFIDController.mConnectedReader.Events.addEventsListener(rfidEventsListener);
                                    }
                                } catch (InvalidUsageException e) {
                                    e.printStackTrace();
                                } catch (OperationFailureException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.d(RFIDController.TAG, "null pointer ");
                                    e.printStackTrace();
                                }
                                if (exception == null) {
                                    try {
                                        RFIDController.getInstance().updateReaderConnection(true);
                                    } catch (InvalidUsageException e) {
                                        e.printStackTrace();
                                    } catch (OperationFailureException e) {
                                        e.printStackTrace();
                                    } catch (NullPointerException e) {
                                        Log.d(RFIDController.TAG, "null pointer ");
                                        e.printStackTrace();
                                    }
                                } else {
                                    RFIDController.clearSettings();
                                }
                            }
                        }
                    }
                } catch (InvalidUsageException ex) {
                    exceptionIN = ex;
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                    exception = e;
                }
                return null;
            }


            @Override
            protected void onPostExecute(Boolean result) {

                if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected()) {
                    if (exception != null) {
                        if (exception.getResults() == RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {
                            try {
                                RFIDController.mConnectedReader.Events.addEventsListener(rfidEventsListener);
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();

                            }
                            RFIDController.regionNotSet = true;


                        } else if (exception.getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                            RFIDController.isBatchModeInventoryRunning = true;
                            RFIDController.mIsInventoryRunning = true;
                            try {
                                if (RFIDController.mConnectedReader.Events != null) {
                                    RFIDController.mConnectedReader.Events.addEventsListener(rfidEventsListener);
                                }
                                //MainActivity.updateReaderConnection(false);
                                RFIDController.mConnectedReader.Events.setBatchModeEvent(true);
                                RFIDController.mConnectedReader.Events.setReaderDisconnectEvent(true);
                                RFIDController.mConnectedReader.Events.setBatteryEvent(true);
                                RFIDController.mConnectedReader.Events.setInventoryStopEvent(true);
                                RFIDController.mConnectedReader.Events.setInventoryStartEvent(true);
                                RFIDController.mConnectedReader.Events.setTagReadEvent(true);
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                Log.d(RFIDController.TAG, "null pointer ");
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                RFIDController.mConnectedReader.disconnect();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                            }
                            RFIDController.mConnectedReader = null;
                            RFIDController.mConnectedDevice = null;
                        }
                        rfidListeners.onFailure(exception);
                    } else {
                        rfidListeners.onSuccess(null);
                    }


                } else {
                    rfidListeners.onFailure("Device is not paired");
                }
                RFIDController.autoConnectDeviceTask = null;
                //contextSettingDetails = null;
            }
        }.execute();

    }


    public void updateReaderConnection(Boolean fullUpdate) throws InvalidUsageException, OperationFailureException {
        if (fullUpdate)
            RFIDController.mConnectedReader.PostConnectReaderUpdate();
        RFIDController.mConnectedReader.Events.setBatchModeEvent(true);
        RFIDController.mConnectedReader.Events.setReaderDisconnectEvent(true);
        RFIDController.mConnectedReader.Events.setInventoryStartEvent(true);
        RFIDController.mConnectedReader.Events.setInventoryStopEvent(true);
        RFIDController.mConnectedReader.Events.setTagReadEvent(true);
        RFIDController.mConnectedReader.Events.setHandheldEvent(true);
        RFIDController.mConnectedReader.Events.setBatteryEvent(true);
        RFIDController.mConnectedReader.Events.setPowerEvent(true);
        RFIDController.mConnectedReader.Events.setOperationEndSummaryEvent(true);
        RFIDController.regulatory = RFIDController.mConnectedReader.Config.getRegulatoryConfig();
        RFIDController.regionNotSet = false;
        RFIDController.rfModeTable = RFIDController.mConnectedReader.ReaderCapabilities.RFModes.getRFModeTableInfo(0);
        LinkProfileUtil.getInstance().populateLinkeProfiles();
        //
        LoadProfileToReader();
        //
        RFIDController.antennaRfConfig = RFIDController.mConnectedReader.Config.Antennas.getAntennaRfConfig(1);
        RFIDController.singulationControl = RFIDController.mConnectedReader.Config.Antennas.getSingulationControl(1);
        RFIDController.settings_startTrigger = RFIDController.mConnectedReader.Config.getStartTrigger();
        RFIDController.settings_stopTrigger = RFIDController.mConnectedReader.Config.getStopTrigger();
        RFIDController.tagStorageSettings = RFIDController.mConnectedReader.Config.getTagStorageSettings();
        RFIDController.dynamicPowerSettings = RFIDController.mConnectedReader.Config.getDPOState();
        if (RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
            // beeperVolume = BEEPER_VOLUME.QUIET_BEEP;
            RFIDController.sledBeeperVolume = RFIDController.mConnectedReader.Config.getBeeperVolume();
            RFIDController.batchMode = RFIDController.mConnectedReader.Config.getBatchModeConfig().getValue();
        }
        RFIDController.reportUniquetags = RFIDController.mConnectedReader.Config.getUniqueTagReport();
        RFIDController.mConnectedReader.Config.getDeviceVersionInfo(Application.versionInfo);
        RFIDController.mConnectedReader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, false);
        RFIDController.mConnectedReader.Config.setLedBlinkEnable(RFIDController.ledState);
        RFIDController.mConnectedReader.Config.getDeviceStatus(true, false, false);
        //
        if (RFIDController.ActiveProfile.content.equals("Reader Defined")) {
            UpdateActiveProfile();
        }
        VersionInfo sdkversion = RFIDController.mConnectedReader.versionInfo();
        Log.d("DEMOAPP", "SDK version " + sdkversion.getVersion());
        RFIDController.startTimer();
    }

    void LoadProfileToReader() {
        // update profiles based on reader type
        UpdateProfilesForRegulatory();
        if (!RFIDController.ActiveProfile.content.equals("Reader Defined")) {
            try {
                // Antenna
                Antennas.AntennaRfConfig antennaRfConfigLocal;
                antennaRfConfigLocal = RFIDController.mConnectedReader.Config.Antennas.getAntennaRfConfig(1);
                antennaRfConfigLocal.setTransmitPowerIndex(RFIDController.ActiveProfile.powerLevel);
                antennaRfConfigLocal.setrfModeTableIndex(/*LinkProfileUtil.getInstance().getSimpleProfileModeIndex*/
                        (RFIDController.ActiveProfile.LinkProfileIndex));
                RFIDController.mConnectedReader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfigLocal);
                RFIDController.antennaRfConfig = antennaRfConfigLocal;
                // Singulation
                Antennas.SingulationControl singulationControlLocal;
                singulationControlLocal = RFIDController.mConnectedReader.Config.Antennas.getSingulationControl(1);
                singulationControlLocal.setSession(SESSION.GetSession(RFIDController.ActiveProfile.SessionIndex));
                if (RFIDController.ActiveProfile.id.equals("0"))
                    singulationControlLocal.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_AB_FLIP);
                else if (!RFIDController.ActiveProfile.id.equals("5"))
                    singulationControlLocal.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                // honour the prefilter over profile
                if (RFIDController.mConnectedReader.Actions.PreFilters.length() > 0) {
                    com.zebra.rfid.api3.PreFilters.PreFilter prefilter1 = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                    if (prefilter1 != null) {
                        if (RFIDController.NON_MATCHING)
                            singulationControlLocal.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                        else
                            singulationControlLocal.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_B);
                    }
                }
                RFIDController.mConnectedReader.Config.Antennas.setSingulationControl(1, singulationControlLocal);
                RFIDController.singulationControl = singulationControlLocal;
                // DPO
                if (RFIDController.ActiveProfile.DPO_On)
                    RFIDController.mConnectedReader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);
                else
                    RFIDController.mConnectedReader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
                RFIDController.dynamicPowerSettings = RFIDController.mConnectedReader.Config.getDPOState();
            } catch (InvalidUsageException e) {
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    public static void operationHasAborted(final RfidListeners rfidListeners) {
        if (RFIDController.isBatchModeInventoryRunning != null && RFIDController.isBatchModeInventoryRunning) {
            if (RFIDController.isInventoryAborted) {
                RFIDController.isBatchModeInventoryRunning = false;
                RFIDController.isGettingTags = true;
                if (RFIDController.settings_startTrigger == null) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            try {
                                // execute following code after STOP is finished
                                synchronized (RFIDController.isInventoryAborted) {
                                    RFIDController.isInventoryAborted.wait(50);
                                }
                                if (RFIDController.mConnectedReader.isCapabilitiesReceived()) {
                                    RFIDController.getInstance().updateReaderConnection(false);
                                } else {
                                    RFIDController.getInstance().updateReaderConnection(true);
                                }
                                RFIDController.getTagReportingFields();
                                RFIDController.mConnectedReader.Actions.getBatchedTags();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                                Log.d(RFIDController.TAG, "OpFailEx " + e.getVendorMessage() + " " + e.getResults() + " " + e.getStatusDescription());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);
                            rfidListeners.onSuccess(null);
                        }
                    }.execute();
                } else {
                    RFIDController.mConnectedReader.Actions.getBatchedTags();
                    rfidListeners.onSuccess(null);
                }
            } else
                rfidListeners.onSuccess(null);
        } else
            rfidListeners.onSuccess(null);

    }

    public ReaderDevice getConnectedDeviceFromRFIDReaderList(String deviceName) throws InvalidUsageException {
        ArrayList<ReaderDevice> readersListArray = RFIDController.readers.GetAvailableRFIDReaderList();
        if (readersListArray.size() == 1) {
            return readersListArray.get(0);
        } else {
            for (int prevreader = readersListArray.size() - 1; prevreader >= 0; prevreader--) {
                if (readersListArray.get(prevreader).getName().equals(deviceName)) {
                    return readersListArray.get(prevreader);
                }
            }
        }
        return null;
    }
}
