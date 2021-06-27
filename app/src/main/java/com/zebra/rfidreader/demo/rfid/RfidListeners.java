package com.zebra.rfidreader.demo.rfid;

public interface RfidListeners {

    void onSuccess(Object object);

    void onFailure(Exception exception);

    void onFailure(String message);

}
