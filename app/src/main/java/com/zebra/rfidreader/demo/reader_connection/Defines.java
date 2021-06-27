package com.zebra.rfidreader.demo.reader_connection;

public final class Defines {
    public static final int NO_ERROR = 0;

    public static final int ERROR_OPEN_NFC_ADAPTER = -100;

    public static final int INFO_ALREADY_PAIRED = 200;
    public static final int ERROR_PAIRING_FAILED = -200;
    public static final int ERROR_PAIRING_TIMEOUT = -201;

    public static final int INFO_UNPAIRING_NO_PAIRED = 220;
    public static final int ERROR_UNPAIRING_FAILED = -220;
    public static final int ERROR_UNPAIRING_TIMEOUT = -221;
    public static final String NameStartString = "RFD8500";


    /// Strings
    public static final String INFO_ALREADY_CONNECTED_STR = "RFD8500 already connected!";
    public static final String INFO_ALREADY_PAIRED_CONNECTING_STR = "RFD8500 already paired! Connecting";
    public static final String INFO_PAIRING = "Pairing with ";
    public static final String INFO_DONE_PAIRING_CONNECTING_STR = "Pairing done. Connecting";
    public static final String SUCCESS_CONNECTING_DONE_STR = "SUCCESS - Connecting done.";
    public static final String CONFIRM_CONNECTION_ACTION_STR = "Please confirm connection by pressing Yellow Trigger button on RFD8500!";
    public static final String INFO_CONNECTING_ABORTED_STR = "INFO - Connecting aborted.";
    public static final String INFO_CONNECTING_TIMED_OUT_STR = "INFO - Connecting timed out.";
    public static final String ERROR_CONNECTING_FAILED_STR = "ERROR - Connecting failed!";
    public static final String ERROR_PAIRING_FAILED_STR = "ERROR - Pairing failed!";
    public static final String ERROR_PAIRING_FAILED_TIMEOUT_STR = "ERROR - Pairing failed (timeout)!";

    public static final int BT_ADDRESS_LENGTH = 12;
}
