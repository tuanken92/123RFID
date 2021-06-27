package com.zebra.rfidreader.demo.reader_connection;

import android.bluetooth.BluetoothDevice;

/**
 * Created by qvfr34 on 2/6/2015.
 */
public class ReaderBTDevice extends com.zebra.rfid.api3.ReaderDevice {
    private boolean isConnected;
    private BluetoothDevice bluetoothDevice;

    /**
     * Empty Constructor. Handles the initialization.
     */
    public ReaderBTDevice() {

    }

    /**
     * Constructor. Handles the initialization.
     *
     * @param bluetoothDevice BT reference
     * @param name            name of the device
     * @param address         address of the device
     * @param serial          device serial
     * @param model           device model
     * @param isConnected     whether connected or not
     */
    public ReaderBTDevice(BluetoothDevice bluetoothDevice, String name, String address, String serial, String model, boolean isConnected) {

        super(name, address);
        this.bluetoothDevice = bluetoothDevice;
    }

    public boolean isConnected() {
        return isConnected;
    }


    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    /**
     * get BT reference of the reader device
     *
     * @return
     */
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    /**
     * set BT reference of the reader device
     *
     * @return
     */
    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }


}

