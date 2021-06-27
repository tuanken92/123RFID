package com.zebra.rfidreader.demo.reader_connection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfidreader.demo.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//###################################################################
//###################################################################
public class ScanPair {

    public ArrayList<String> readers = new ArrayList<>();
    public ProgressDialog mProgressDlg;
    public ProgressDialog pairTaskDailog;
    private ProgressDialog connectProgressDialog;
    private BluetoothHandler btConnection = null;
    private ArrayList<BluetoothDevice> mRFD8500AvailDeviceList = null;
    private ArrayList<BluetoothDevice> mRFD8500PairedDeviceList = null;
    private BluetoothDevice mLastToPairedDevice = null;

    private String recvdMacAddress = null;
    private String recvdBarcodeName = null;

    private Activity activityObject = null;
    private ScanAndPairFragment mainActivityObject = null;

    private AsyncTask<Object, Void, String> mUnpairTask = null;
    private AsyncTask<Object, Void, String> mPairTask = null;

    private boolean pairingConnectIdleFlag = true;

    private boolean isDeviceConnectionConfirmationRequested = false;
    private boolean deviceConnectionConfirmationReceived = false;

    public static ArrayList<ReaderBTDevice> availableReaders = new ArrayList<ReaderBTDevice>();

    public boolean isDevicePairing = false;
    public static String TAG = "123RFID-PAIR";
    public ScanAndPairFragment fragement;

    //#####################################################
    public void Init(Activity activity, ScanAndPairFragment fragement) {
        activityObject = activity;
        this.fragement = fragement;

        btConnection = new BluetoothHandler();
        btConnection.init(activityObject, this);

        mRFD8500AvailDeviceList = new ArrayList<BluetoothDevice>();
        mRFD8500PairedDeviceList = new ArrayList<BluetoothDevice>();

        loadAvailableReaders();
    }


    public void setActivityObject(Activity activityObject) {
        this.activityObject = activityObject;
    }

    //###########################################
    public void onResume() {
        if (btConnection != null) {
            btConnection.onResume();
        }
    }

    //###########################################
    public void onPause() {
        if (btConnection != null) {
            btConnection.onPause();
        }
    }

    //###########################################
    public void onDestroy() {
        if (btConnection != null) {
            btConnection.onDestroy();
            btConnection = null;
        }
        //super.onDestroy();
    }

    //#####################################################
    public void barcodeDeviceNameConnect(String barcodeData) {
        boolean connecting_pairingFlag = false;

        try {
            if (barcodeData == null) {
                fragement.processCompleted("Error while pairing");
            } else {
                if ((pairingConnectIdleFlag)) {
                    recvdMacAddress = null;
                    recvdBarcodeName = null;
                    if (barcodeData != null) {
                        barcodeData = barcodeData.toUpperCase();
                        if (barcodeData.length() == Defines.BT_ADDRESS_LENGTH) {
                            recvdMacAddress = barcodeData.replaceAll("(.{2})(?!$)", "$1:");
                            if (btConnection.isValidMacAddress(recvdMacAddress))
                                connecting_pairingFlag = pairConnect(recvdMacAddress, true);
                            else {
                                showToast(recvdMacAddress + " is not valid BT address");
                            }
                        } else if (barcodeData.length() > Defines.BT_ADDRESS_LENGTH) {
                            recvdBarcodeName = Defines.NameStartString + barcodeData;
                            connecting_pairingFlag = pairConnect(recvdBarcodeName, false);
                        } else {

                            fragement.processCompleted(barcodeData + " is not valid");
                        }
                    }
                    if (!connecting_pairingFlag) {

                    }
                }
            }
        } catch (Exception ex) {
            fragement.processCompleted("Error while pairing");
            //showToast("EXCEPTION(ScanPair) - 'barcodeDeviceNameConnect'");
        }
    }

    //#####################################################
    //#####################################################
    private boolean pairConnect(String data, boolean isMacAddress) {
        boolean connecting_pairingFlag = false;
        isDevicePairing = false;
        Log.d(TAG, "pairConnect");
        showToast(activityObject.getResources().getString(R.string.warning_bt_enable_on_sled));
        try {
            // check if device is already paired and connected
            ArrayList<ReaderBTDevice> readersList = new ArrayList<>();
            readersList.addAll(getAvailableReaders());
            BluetoothDevice tmpDev = null;
            boolean isFound = false;

            for (ReaderBTDevice rdDevice : readersList) {
                if (isMacAddress) {
                    isFound = rdDevice.getBluetoothDevice().getAddress().equals(data);
                    recvdMacAddress = data;
                } else {
                    isFound = rdDevice.getBluetoothDevice().getName().equals(data);
                    recvdBarcodeName = data;
                }

                if (isFound) {
                    tmpDev = rdDevice.getBluetoothDevice();
                    recvdMacAddress = rdDevice.getBluetoothDevice().getAddress();
                    /// check if connected
                    if (rdDevice.isConnected() == true) {

                    } else {
                        /// connect
                        connecting_pairingFlag = true;
                        showToast("Device is already paired");
                        //showToast(Defines.INFO_ALREADY_PAIRED_CONNECTING_STR);
                        //ConnectDevice(rdDevice, true);
                    }
                    break;
                }
            }

            if (tmpDev == null) {
                Log.d(TAG, "pairConnect nothing found");
                /// nothing found check for device availability
                if (isMacAddress) {
                    showToast(Defines.INFO_PAIRING + recvdMacAddress);
                    mPairTask = new PairTask(activityObject, recvdMacAddress).execute();
                    /*btConnection.pair(recvdMacAddress);
                    isDevicePairing = true;*/
                } else {
                    mProgressDlg = new ProgressDialog(activityObject);
                    mProgressDlg.setMessage("Scanning... (" + recvdBarcodeName + ")");
                    mProgressDlg.show();
                   /* mProgressDlg.setCancelable(false);
                    mProgressDlg.setCanceledOnTouchOutside(false);*/
                    btConnection.scanningDevices(recvdBarcodeName, false);
                }

                connecting_pairingFlag = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showToast(ex.getMessage());
        }

        return (connecting_pairingFlag);
    }

    private void ConnectDevice(ReaderDevice rdDevice, boolean b) {
        //   showToast("Device reday to connect:" + rdDevice.getName());
        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            mProgressDlg.dismiss();
        }
        if (pairTaskDailog != null && pairTaskDailog.isShowing()) {
            pairTaskDailog.dismiss();
        }
        fragement.connectDevice(rdDevice.getName(), b);
    }


    private void loadAvailableReaders() {
        Log.d(TAG, "loadAvailableReaders");
        availableReaders.clear();
        readers.clear();
        HashSet<BluetoothDevice> btAvailableReaders = new HashSet();
        btConnection.getAvailableDevices(btAvailableReaders);
        for (BluetoothDevice device : btAvailableReaders) {
            availableReaders.add(new ReaderBTDevice(device, device.getName(), device.getAddress(), null, null, false));
            readers.add(device.getName());
            Log.d(TAG, device.getName());
        }
        ((ScanAndPairFragment) fragement).refreshList();
    }

    public Collection<? extends ReaderBTDevice> getAvailableReaders() {
        loadAvailableReaders();
        return availableReaders;
    }

    //#####################################################
    //#####################################################
    private void showToast(String message) {
        Log.d(TAG, message);

        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            mProgressDlg.dismiss();
        }

        if ((pairTaskDailog != null && pairTaskDailog.isShowing())) {
            pairTaskDailog.dismiss();
        }
        fragement.processCompleted(message);

    }

    //###########################################
    public void btScanningDone(ArrayList<BluetoothDevice> btDeviceList, boolean isMacAddress) {
        BluetoothDevice tmpBTDevice = null;

        if (mProgressDlg != null && mProgressDlg.isShowing())
            mProgressDlg.dismiss();
        if ((pairTaskDailog != null && pairTaskDailog.isShowing())) {
            pairTaskDailog.dismiss();
        }
        Log.d(TAG, "btScanningDone");
        try {
            if (btDeviceList != null) {

                if (!isMacAddress) {

                    for (BluetoothDevice device : btDeviceList) {
                        if (device.getName() != null && device.getName().equals(recvdBarcodeName)) {
                            recvdMacAddress = device.getAddress();
                            tmpBTDevice = device;
                            break;
                        }
                    }
                } else {
                    for (BluetoothDevice device : btDeviceList) {
                        if (device.getAddress().equals(recvdMacAddress)) {
                            tmpBTDevice = device;
                            break;
                        }
                    }
                }

                if (tmpBTDevice != null) {
                    String tmpStr = "Done scanning. Device found.\n\r" +
                            "Name: " + tmpBTDevice.getName() + "\n\r" +
                            "MAC Address: " + tmpBTDevice.getAddress();
                    //showToast(tmpStr);

                    mPairTask = new PairTask(activityObject, tmpBTDevice).execute();
                } else {
                    showToast("Done scanning. Device NOT found.");
                }
            } else {
                showToast("Done scanning. Device NOT found(ex).");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showToast("EXCEPTION -Scanning failed!" + ex.getMessage());
        }
    }

    //###########################################
    public void btPairingDone(boolean successFlag, BluetoothDevice btDevice) {
        try {
            if ((pairTaskDailog != null && pairTaskDailog.isShowing())) {
                pairTaskDailog.dismiss();
            }
            mPairTask = null;

            //updatePairedDevList();
            mLastToPairedDevice = btDevice;

            if (successFlag) {
                //showToast(Defines.INFO_DONE_PAIRING_CONNECTING_STR);

                if (mLastToPairedDevice != null) {
                    /*ArrayList<ReaderBTDevice> readersList = new ArrayList<>();*/
                    loadAvailableReaders();
                    /*readersList.addAll(getAvailableReaders());*/
                    boolean tmpFlag = false;
                    for (ReaderBTDevice rdDevice : getAvailableReaders()) {
                        if (rdDevice.getBluetoothDevice().getAddress().equals(recvdMacAddress)) {
                            tmpFlag = true;
                            ConnectDevice(rdDevice, false);
                            break;
                        }
                    }
                    if (!tmpFlag) {

                    }
                } else {
                    //showToast("ERROR - Nothing to connect!");
                }
            } else {
                showToast("ERROR - Pairing failed!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //showToast("EXCEPTION(ScanPair) - 'btPairingDone'");

        }
    }

    //###########################################
    public void btUnpairingDone(boolean successFlag) {
        try {
            if ((mUnpairTask != null) && (((UnpairTask) mUnpairTask).dialog != null)) {
                ((UnpairTask) mUnpairTask).dialog.dismiss();
            }
            mUnpairTask = null;
            updatePairedDevList();
            loadAvailableReaders();

            if (successFlag) {
                showToast("Unpairing done");
                fragement.removeDevice(unPairDevice);

            } else {

            }
            //showToast("ERROR - Unpairing failed!");


        } catch (Exception ex) {
            ex.printStackTrace();
            //showToast("EXCEPTION(ScanPair) - 'btUnpairingDone'");

        }
    }

    private String unPairDevice;

    public void unpair(String readerDevice) {
        unPairDevice = readerDevice;
        btConnection.unpairReader(readerDevice);
    }


    //###########################################
    private class UnpairTask extends AsyncTask<Object, Void, String> {
        public ProgressDialog dialog;

        public UnpairTask(Context act) {
            dialog = new ProgressDialog(act);
        }

        protected void onPreExecute() {
            dialog.setMessage("Unpairing All RFD8500...");
            dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            String errorCode = null;
            try {
                int retValue = btConnection.unpair();
                switch (retValue) {
                    case Defines.NO_ERROR:
                        break;
                    case Defines.INFO_UNPAIRING_NO_PAIRED:
                        errorCode = "INFO - No paired RFD8500!";
                        break;
                    case Defines.ERROR_UNPAIRING_TIMEOUT:
                        errorCode = "ERROR - Unpairing failed (timeout)!";
                        break;
                    case Defines.ERROR_UNPAIRING_FAILED:
                    default:
                        errorCode = "ERROR - Unpairing failed!";
                        break;
                }
            } catch (Exception ex) {
                //showToast("EXCEPTION(ScanPair) - 'UnpairTask.doInBackground'");

            }

            return (errorCode);
        }

        @Override
        protected void onPostExecute(String errorCode) {
            try {
                if (errorCode != null) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    //showToast(errorCode);
                    updatePairedDevList();


                }
            } catch (Exception ex) {
                //showToast("EXCEPTION(ScanPair) - 'UnpairTask.onPostExecute'");

            }
        }
    }

    //###########################################
    private class PairTask extends AsyncTask<Object, Void, String> {

        private String mMacAddress = null;
        private BluetoothDevice mBtDevice = null;
        private String devName = null;

        public PairTask(Context act, String macAddress) {
            mMacAddress = macAddress;
            devName = mMacAddress;
            pairTaskDailog = new ProgressDialog(act);
        }

        public PairTask(Context act, BluetoothDevice btDevice) {
            mBtDevice = btDevice;
            devName = "'" + mBtDevice.getName() + "'";

            if ((pairTaskDailog != null && pairTaskDailog.isShowing())) {
                pairTaskDailog.dismiss();
            }
            pairTaskDailog = new ProgressDialog(act);
        }

        protected void onPreExecute() {


            pairTaskDailog.setMessage("Pairing " + devName + "...\nPress RFD8500 Yellow Trigger " +
                    "button when Bluetooth button LED blinks fast to finalize pairing!");
           /* pairTaskDailog.setCancelable(false);
            pairTaskDailog.setCanceledOnTouchOutside(false);*/
            pairTaskDailog.show();

        }

        @Override
        protected String doInBackground(Object... params) {
            String errorCode = null;
            int retValue = 0;

            try {
                if (mMacAddress == null) {
                    mLastToPairedDevice = mBtDevice;
                    retValue = btConnection.pair(mBtDevice, true);
                } else
                    retValue = btConnection.pair(mMacAddress);
                switch (retValue) {
                    case Defines.NO_ERROR:
                        break;
                    case Defines.INFO_ALREADY_PAIRED:
                        errorCode = Defines.INFO_ALREADY_PAIRED_CONNECTING_STR;
                        ArrayList<ReaderBTDevice> readersList = new ArrayList<>();
                        readersList.addAll(getAvailableReaders());
                        boolean tmpFlag = false;
                        for (ReaderBTDevice rdDevice : readersList) {
                            if (rdDevice.getBluetoothDevice().getAddress().equals(recvdMacAddress)) {
                                tmpFlag = true;
                                ConnectDevice(rdDevice, true);
                                break;
                            }
                        }
                        break;
                    case Defines.ERROR_PAIRING_TIMEOUT:
                        errorCode = Defines.ERROR_PAIRING_FAILED_TIMEOUT_STR;
                        break;
                    case Defines.ERROR_PAIRING_FAILED:
                    default:
                        errorCode = Defines.ERROR_PAIRING_FAILED_STR;
                        break;
                }
            } catch (Exception ex) {
                //showToast("EXCEPTION(ScanPair) - 'PairTask.doInBackground'");
            }
            return (errorCode);
        }

        @Override
        protected void onPostExecute(String errorCode) {
            try {
                if (errorCode != null) {
                    if (pairTaskDailog.isShowing()) {
                        pairTaskDailog.dismiss();
                        pairTaskDailog = null;
                    }
                    //showToast(errorCode);
                    updatePairedDevList();
                }
            } catch (Exception ex) {
                //showToast("EXCEPTION(ScanPair) - 'PairTask.onPostExecute'");
            }
        }
    }

    //###########################################
    private void updatePairedDevList() {
        try {
            mRFD8500PairedDeviceList.clear();
            Set<BluetoothDevice> mPairedDevices = btConnection.GetBluetoothAdapter().getBondedDevices();
            mRFD8500PairedDeviceList = new ArrayList<BluetoothDevice>();
            if (mPairedDevices != null) {
                for (BluetoothDevice device : mPairedDevices) {
                    if (device.getName().contains(Defines.NameStartString)) {
                        mRFD8500PairedDeviceList.add(device);
                    }
                }
            }
        } catch (Exception ex) {
            //showToast("EXCEPTION(ScanPair) - 'updatePairedDevList'");
        }
    }


    //###########################################
    private void DeviceConnectionConfirmationRequest() {
        isDeviceConnectionConfirmationRequested = true;
        deviceConnectionConfirmationReceived = false;
    }

    //###########################################
    private void DeviceConnectionConfirmationReset() {
        isDeviceConnectionConfirmationRequested = false;
        deviceConnectionConfirmationReceived = false;
    }

    //###########################################
    public boolean DeviceConnectionConfirmationRequested() {
        return isDeviceConnectionConfirmationRequested;
    }

    //###########################################
    public void DeviceConnectionConfirmed() {
        isDeviceConnectionConfirmationRequested = false;
        deviceConnectionConfirmationReceived = true;
    }


}
