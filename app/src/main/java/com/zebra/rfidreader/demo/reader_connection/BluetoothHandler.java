package com.zebra.rfidreader.demo.reader_connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;


//###################################################################
//###################################################################
public class BluetoothHandler {
    private static final String REQUEST_ENABLE_BT = "";
    final int SCANNING_TIMEOUT = 10000; //ms

    private String pairMacAddress = null;
    private String pairName = null;

    private ScanPair parentObj = null;
    private Context grandParentObj = null;
    private static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ;
    private ArrayList<BluetoothDevice> mDeviceList = null;
    private Set<BluetoothDevice> mPairedDevices = null;

    private static int operationType = 0;
    private IntentFilter filter = null;

    private static final String TAG = BluetoothHandler.class.getName();

    //#####################################################
    //#####################################################
    //#############################
    public int init(Context grandParent, ScanPair parent) {
        int ret = Defines.NO_ERROR;

        grandParentObj = grandParent;
        parentObj = parent;

        mDeviceList = new ArrayList<BluetoothDevice>();

        filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        grandParentObj.registerReceiver(mReceiver, filter);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            grandParentObj.startActivity(enableBtIntent);
        }

        return (ret);
    }

    //#############################
    public void onResume() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            grandParentObj.startActivity(enableBtIntent);
        }
        if (filter != null) {
            grandParentObj.registerReceiver(mReceiver, filter);
        }
    }

    //#############################
    public void onPause() {
        try {
            grandParentObj.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    //#############################
    public void onDestroy() {
        try {
            grandParentObj.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
        }
    }


    //#############################
    public int abortOperation() {
        if (operationType == 0) {
            mBluetoothAdapter.cancelDiscovery();
        }

        return (0);
    }

    //#############################
    public int scanningDevices() {
        operationType = 0;
        pairName = null;
        pairMacAddress = null;
        mBluetoothAdapter.startDiscovery();
        new TimeoutTask().execute();
        return (0);
    }

    //#############################
    public int scanningDevices(String data, boolean isMacAddress) {
        operationType = 0;
        pairMacAddress = null;
        pairName = null;

        if (isMacAddress)
            pairMacAddress = data;
        else
            pairName = data;
        boolean ret = mBluetoothAdapter.startDiscovery();
        return (0);
    }

    //#############################
    public ArrayList<BluetoothDevice> GetScannedDeviceList() {
        return (mDeviceList);
    }

    //#############################
    public BluetoothAdapter GetBluetoothAdapter() {
        return (mBluetoothAdapter);
    }

    //#############################
    public int pair(BluetoothDevice foundDevice, boolean pairFlag) {
        int ret = Defines.NO_ERROR;

        operationType = 1;
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices != null) {
            for (BluetoothDevice device : mPairedDevices) {
                if (device.getAddress().equals(foundDevice.getAddress())) {
                    ret = Defines.INFO_ALREADY_PAIRED;
                    break;
                }
            }
        }

        if ((ret == Defines.NO_ERROR) && (pairFlag)) {
            ret = pairFunc(foundDevice.getAddress());
        }

        return (ret);
    }

    //#############################
    public static int pair(String macAddress) {
        int ret = Defines.NO_ERROR;
        operationType = 1;
        ret = pairDevice(mBluetoothAdapter.getRemoteDevice(macAddress));

	/*	operationType = 1;
		pairMacAddress = macAddress;
		mPairedDevices = mBluetoothAdapter.getBondedDevices();
		if(mPairedDevices != null) {
			for(BluetoothDevice device : mPairedDevices) {
				if(device.getAddress().equals(macAddress)) {
					ret = Defines.INFO_ALREADY_PAIRED;
					break;
   				}
			}
		}   
		
		if(ret == Defines.NO_ERROR) {
			ret = pairFunc(macAddress);
		}
		*/
        return (ret);
    }

    //#############################
    private int pairFunc(String macAddress) {
        int ret = Defines.NO_ERROR;

        ret = Defines.ERROR_PAIRING_FAILED;
        for (BluetoothDevice device : mDeviceList) {
            if (device.getAddress().equals(macAddress)) {
                pairDevice(device);
                ret = Defines.NO_ERROR;
                break;
            }
        }

        return (ret);
    }

    //#############################
    public int unpair(String macAddres) {
        int ret = Defines.ERROR_UNPAIRING_FAILED;

        operationType = 2;
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices != null) {
            for (BluetoothDevice device : mPairedDevices) {
                if (device.getAddress().equals(macAddres)) {
                    unpairDevice(device);
                    ret = Defines.NO_ERROR;
                    break;
                }
            }
        }

        return (ret);
    }

    public int unpairReader(String name) {
        int ret = Defines.ERROR_UNPAIRING_FAILED;

        operationType = 2;
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices != null) {
            for (BluetoothDevice device : mPairedDevices) {
                if (device.getName() != null && device.getName().equals(name)) {
                    unpairDevice(device);
                    ret = Defines.NO_ERROR;
                    break;
                }
            }
        }

        return (ret);
    }

    //#############################
    public int unpair() {
        int ret = Defines.ERROR_UNPAIRING_FAILED;

        operationType = 2;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }

        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if ((mPairedDevices != null) && (mPairedDevices.size() > 0)) {
            for (BluetoothDevice device : mPairedDevices) {
                String devName = device.getName();
                if (devName.contains(Defines.NameStartString) == true) {
                    unpairDevice(device);
                }
            }
            ret = Defines.NO_ERROR;
        } else {
            ret = Defines.INFO_UNPAIRING_NO_PAIRED;
        }

        return (ret);
    }

    //###########################################
    public static int pairDevice(BluetoothDevice device) {
        int ret = Defines.NO_ERROR;
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
            ret = Defines.ERROR_PAIRING_FAILED;
        }

        return (ret);
    }

    //###########################################
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //###########################################
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            try {
                String action = intent.getAction();
                Log.d(TAG, "BT  " + action);
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    if (state == BluetoothAdapter.STATE_ON) {
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    mDeviceList.clear();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (operationType == 0) {
                        parentObj.btScanningDone(mDeviceList, pairMacAddress != null);
                    }
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDeviceList.add(device);
                    if (pairMacAddress != null) {
                        if (device.getAddress().equals(pairMacAddress)) {
                            mBluetoothAdapter.cancelDiscovery();
                        }
                    }
                    if (pairName != null) {
                        if (device.getName() != null && device.getName().equals(pairName)) {
                            mBluetoothAdapter.cancelDiscovery();
                        }
                    }
                } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                    if (prevState == BluetoothDevice.BOND_BONDING) {
                        parentObj.btPairingDone(state == BluetoothDevice.BOND_BONDED, (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    } else if (prevState == BluetoothDevice.BOND_BONDED) {
                        parentObj.btUnpairingDone(state == BluetoothDevice.BOND_NONE);
                    }
                }
            } catch (Exception ex) {
                Log.d(TAG, "receiver" + ex.getMessage());
                ex.printStackTrace();
                mBluetoothAdapter.cancelDiscovery();
                parentObj.btScanningDone(null, true);
            }
        }
    };

    public boolean isValidMacAddress(String recvdMacAddress) {
        return GetBluetoothAdapter().checkBluetoothAddress(recvdMacAddress);
    }


    //###########################################
    private class TimeoutTask extends AsyncTask<Object, Void, String> {

        public TimeoutTask() {
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Object... params) {
            String errorCode = null;
            try {
                Thread.sleep(SCANNING_TIMEOUT);
                abortOperation();
            } catch (InterruptedException ex) {

            }
            return (errorCode);
        }

        @Override
        protected void onPostExecute(String errorCode) {
        }
    }


    public static void getAvailableDevices(Set<BluetoothDevice> devices) {
        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices())
            if (isRFIDReader(device))
                devices.add(device);
    }

    public static boolean isRFIDReader(BluetoothDevice device) {
        if (device.getName() != null && device.getName().startsWith("RFD8500"))
            return true;
        return false;
    }

    public static boolean isDevicePaired(String deviceID) {

        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            Log.d("isDevicePaired", device.getName() + "   " + deviceID);
            if (device.getName() != null && device.getName().equals(deviceID))
                return true;
        }

        return false;

    }

    public static void scanDevices() {

        mBluetoothAdapter.startDiscovery();
    }


   /* public static boolean pairDeviceIfNotPaired(BluetoothDevice deviceToPair) {

        if (deviceToPair == null) return false;

        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            if (device.getName() != null && device.getName().equals(deviceToPair.getName())) {
                return false;
            }
        }

        pairDevice(deviceToPair);

        return true;

    }*/
}
