package com.zebra.rfidreader.demo.common;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

/**
 * Class to maintain the strings used for notifications and intent actions
 */
public class Constants {

    public static final String IMMEDIATE = "Immediate";
    public static final String HANDHELD = "Handheld";
    public static final String PERIODIC = "Periodic";
    public static final String DURATION = "Duration";
    public static final String TAG_OBSERVATION = "Tag Observation";
    public static final String N_ATTEMPTS = "N attempts";
    public static final int BATCH_MODE_ATTR_NUM = 1500;
    public static final int QUIET_BEEPER = 3;
    public static final String READER_PASSWORDS = "ReadersPasswords";
    public static final String ZERO_TIME = "00:00";
    public static final String FROM_NOTIFICATION = "fromNotification";

    public static final String MESSAGE_BATTERY_CRITICAL = "Battery level critical";
    public static final String MESSAGE_BATTERY_LOW = "Battery level low";
    public static final int BATTERY_FULL = 100;
    //For Debugging
    public static final boolean DEBUG = false;
    public static final int TYPE_DEBUG = 60;
    public static final int TYPE_ERROR = 61;
    //Intent Data
    public static final String INTENT_ACTION = "intent_action";
    public static final String INTENT_DATA = "intent_data";
    //Action strings for various RFID Events
    public static final String ACTION_READER_BATTERY_LOW = "com.rfidreader.battery.low";
    public static final String ACTION_READER_BATTERY_CRITICAL = "com.rfidreader.battery.critical";
    public static final String ACTION_READER_CONNECTED = "com.rfidreader.connected";
    public static final String ACTION_READER_DISCONNECTED = "com.rfidreader.disconnected";
    public static final String ACTION_READER_AVAILABLE = "com.rfidreader.available";
    public static final String ACTION_READER_CONN_FAILED = "com.rfidreader.conn.failed";
    public static final String ACTION_READER_STATUS_OBTAINED = "com.rfidreader.status.received";

    //Data related to notifications
    public static final String NOTIFICATIONS_TEXT = "notifications_text";
    public static final String NOTIFICATIONS_ID = "notifications_id";
    //timeout for sled response
    public static final int RESPONSE_TIMEOUT = 6000;
    public static final long SAVE_CONFIG_RESPONSE_TIMEOUT = 15000;
    //Strings for storing the checkbox status of connection settings in shared preferences
    public static final String SOCKET_STATION_TYPE = "SocketStationType";
    public static final String SOCKET_STATION_IP = "SocketStationIP";
    public static final String SOCKET_STATION_PORT = "SocketStationPort";
    public static final int SOCKET_MODE_DONT_USE = 0;
    public static final int SOCKET_MODE_SERVER = 1;
    public static final int SOCKET_MODE_CLIENT = 2;

    public static final String APP_SETTINGS_STATUS = "AppSettingStatus";
    public static final String AUTO_DETECT_READERS = "AutoDetectReaders";
    public static final String AUTO_RECONNECT_READERS = "AutoReconnectReaders";
    public static final String NOTIFY_READER_AVAILABLE = "NotifyReaderAvailable";
    public static final String NOTIFY_READER_CONNECTION = "NotifyReaderConnection";
    public static final String NOTIFY_BATTERY_STATUS = "NotifyBatteryStatus";
    public static final String EXPORT_DATA = "ExportData";
    public static final String TAG_LIST_MATCH_MODE = "TagListMatchMode";
    public static final String SHOW_CSV_TAG_NAMES = "TagListMatchcsvTagNames";
    public static final String NON_MATCHING = "PRE_FILTER_NON_MATCHING";
    public static final String PREFILTER_ADV_OPTIONS = "PREFILTER_ADV_OPTIONS";
    public static final String ACCESS_ADV_OPTIONS = "ACCESS_ADV_OPTIONS";
    public static final String ASCII_MODE = "ASCII_MODE";
    public static final String LAST_READER = "LAST_CONNECTED_READER";
    // Canned profile storage
    public static final String APP_SETTINGS_PROFILE = "AppSettingProfile";
    public static final String PROFILE_POWER = "PROFILE_POWER";
    public static final String PROFILE_LINK_PROFILE = "PROFILE_LINK_PROFILE";
    public static final String PROFILE_SESSION = "PROFILE_SESSION";
    public static final String PROFILE_DPO = "PROFILE_DPO";
    public static final String PROFILE_IS_ON = "PROFILE_IS_ON";
    public static final String PROFILE_UI_ENABLED = "PROFILE_UI_ENABLED";
    public static final String MULTITAG_LOCATE_DATA_SORT = "MultiTagLocate";
    public static final String MULTITAG_LOCATE_FOUND_PROXI_PERCENT_DATA = "MultiTagLocateFoundProxiPercent";
    public static final String MULTITAG_LOCATE_CSV_URI = "MultiTagLocateCsv";
    //Bundle name for setting item selected
    public static final String SETTING_ITEM_ID = "settingItemId";
    public static final int UNIQUE_TAG_LIMIT = 120000;
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String NGE = "NGE";
    public static final String RFID_DEVICE = "RFID_DEVICE";
    public static final String GENX_DEVICE = "GENX_DEVICE";
    //toast messages
    public static final String TAG_EMPTY = "Please fill Tag Id";
    public static final int NO_OF_BITS = 16;
    public static final String TAGS_SEC = "";//" t/s";
    //max offset for prefilter and access
    public static final Integer MAX_OFFSET = 1024;
    //max offset for access
    public static final Integer MAX_LEGTH = 1024;
    // tag match file name and directory
    public static final String RFID_FILE_DIR = "/rfid";
    public static final String TAG_MATCH_FILE_NAME = "taglist.csv";
    public static int INVENTORY_LIST_FONT_SIZE;

    /**
     * Method to be used throughout the app for logging debug messages
     *
     * @param type    - One of TYPE_ERROR or TYPE_DEBUG
     * @param TAG     - Simple String indicating the origin of the message
     * @param message - Message to be logged
     */
    public static void logAsMessage(int type, String TAG, String message) {
        if (DEBUG) {
            if (type == TYPE_DEBUG)
                Log.d(TAG, (message == null || message.isEmpty()) ? "Message is Empty!!" : message);
            else if (type == TYPE_ERROR)
                Log.e(TAG, (message == null || message.isEmpty()) ? "Message is Empty!!" : message);
        }
    }

    public static String getIPDevice(Context context)
    {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

}
