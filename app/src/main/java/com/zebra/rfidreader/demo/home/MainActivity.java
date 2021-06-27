package com.zebra.rfidreader.demo.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.LOCK_DATA_FIELD;
import com.zebra.rfid.api3.LOCK_PRIVILEGE;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.access_operations.AccessOperationsFragment;
import com.zebra.rfidreader.demo.access_operations.AccessOperationsLockFragment;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.CustomProgressDialog;
import com.zebra.rfidreader.demo.common.CustomToast;
import com.zebra.rfidreader.demo.common.Inventorytimer;
import com.zebra.rfidreader.demo.common.MatchModeFileLoader;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces.BatteryNotificationHandler;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces.ReaderDeviceFoundHandler;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces.TriggerEventHandler;
import com.zebra.rfidreader.demo.common.asciitohex;
import com.zebra.rfidreader.demo.common.hextoascii;
import com.zebra.rfidreader.demo.data_export.DataExportTask;
import com.zebra.rfidreader.demo.inventory.InventoryFragment;
import com.zebra.rfidreader.demo.inventory.InventoryListItem;
import com.zebra.rfidreader.demo.locate_tag.LocateOperationsFragment;
import com.zebra.rfidreader.demo.locate_tag.multitag_locate.MultiTagLocateResponseHandlerTask;
import com.zebra.rfidreader.demo.locate_tag.RangeGraph;
import com.zebra.rfidreader.demo.locate_tag.SingleTagLocateFragment;
import com.zebra.rfidreader.demo.locate_tag.multitag_locate.MultiTagLocateFragment;
import com.zebra.rfidreader.demo.notifications.NotificationUtil;
import com.zebra.rfidreader.demo.rapidread.RapidReadFragment;
import com.zebra.rfidreader.demo.reader_connection.BluetoothHandler;
import com.zebra.rfidreader.demo.reader_connection.ReadersListFragment;
import com.zebra.rfidreader.demo.rfid.ConnectionController;
import com.zebra.rfidreader.demo.rfid.RFIDController;
import com.zebra.rfidreader.demo.rfid.RfidListeners;
import com.zebra.rfidreader.demo.settings.AdvancedOptionItemFragment;
import com.zebra.rfidreader.demo.settings.AdvancedOptionsContent;
import com.zebra.rfidreader.demo.settings.BackPressedFragment;
import com.zebra.rfidreader.demo.settings.ISettingsUtil;
import com.zebra.rfidreader.demo.settings.PreFilterFragment;
import com.zebra.rfidreader.demo.settings.ProfileContent;
import com.zebra.rfidreader.demo.settings.ProfileFragment;
import com.zebra.rfidreader.demo.settings.SettingListFragment;
import com.zebra.rfidreader.demo.settings.SettingsDetailActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;
import static com.zebra.rfidreader.demo.application.Application.TAG_LIST_LOADED;
import static com.zebra.rfidreader.demo.application.Application.TAG_LIST_MATCH_MODE;
import static com.zebra.rfidreader.demo.application.Application.TOTAL_TAGS;
import static com.zebra.rfidreader.demo.application.Application.UNIQUE_TAGS;
import static com.zebra.rfidreader.demo.application.Application.UNIQUE_TAGS_CSV;
import static com.zebra.rfidreader.demo.application.Application.iBrandIDLen;
import static com.zebra.rfidreader.demo.application.Application.inventoryList;
import static com.zebra.rfidreader.demo.application.Application.mIsMultiTagLocatingRunning;
import static com.zebra.rfidreader.demo.application.Application.matchingTags;
import static com.zebra.rfidreader.demo.application.Application.matchingTagsList;
import static com.zebra.rfidreader.demo.application.Application.memoryBankId;
import static com.zebra.rfidreader.demo.application.Application.missedTags;
import static com.zebra.rfidreader.demo.application.Application.missingTagsList;
import static com.zebra.rfidreader.demo.application.Application.strBrandID;
import static com.zebra.rfidreader.demo.application.Application.tagListMap;
import static com.zebra.rfidreader.demo.application.Application.tagsListCSV;
import static com.zebra.rfidreader.demo.application.Application.tagsReadForSearch;
import static com.zebra.rfidreader.demo.application.Application.tagsReadInventory;
import static com.zebra.rfidreader.demo.application.Application.unknownTagsList;
import static com.zebra.rfidreader.demo.rfid.RFIDController.AUTO_DETECT_READERS;
import static com.zebra.rfidreader.demo.rfid.RFIDController.AUTO_RECONNECT_READERS;
import static com.zebra.rfidreader.demo.rfid.RFIDController.BatteryData;
import static com.zebra.rfidreader.demo.rfid.RFIDController.EXPORT_DATA;
import static com.zebra.rfidreader.demo.rfid.RFIDController.LAST_CONNECTED_READER;
import static com.zebra.rfidreader.demo.rfid.RFIDController.NON_MATCHING;
import static com.zebra.rfidreader.demo.rfid.RFIDController.NOTIFY_BATTERY_STATUS;
import static com.zebra.rfidreader.demo.rfid.RFIDController.NOTIFY_READER_AVAILABLE;
import static com.zebra.rfidreader.demo.rfid.RFIDController.NOTIFY_READER_CONNECTION;
import static com.zebra.rfidreader.demo.rfid.RFIDController.SHOW_CSV_TAG_NAMES;
import static com.zebra.rfidreader.demo.rfid.RFIDController.TagProximityPercent;
import static com.zebra.rfidreader.demo.rfid.RFIDController.asciiMode;
import static com.zebra.rfidreader.demo.rfid.RFIDController.bFound;
import static com.zebra.rfidreader.demo.rfid.RFIDController.beeperVolume;
import static com.zebra.rfidreader.demo.rfid.RFIDController.brandidcheckenabled;
import static com.zebra.rfidreader.demo.rfid.RFIDController.channelIndex;
import static com.zebra.rfidreader.demo.rfid.RFIDController.clearInventoryData;
import static com.zebra.rfidreader.demo.rfid.RFIDController.currentFragment;
import static com.zebra.rfidreader.demo.rfid.RFIDController.dynamicPowerSettings;
import static com.zebra.rfidreader.demo.rfid.RFIDController.getInstance;
import static com.zebra.rfidreader.demo.rfid.RFIDController.inventoryMode;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isAccessCriteriaRead;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isBatchModeInventoryRunning;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isGettingTags;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isInventoryAborted;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isLocatingTag;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isLocationingAborted;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isTriggerRepeat;
import static com.zebra.rfidreader.demo.rfid.RFIDController.is_connection_requested;
import static com.zebra.rfidreader.demo.rfid.RFIDController.is_disconnection_requested;
import static com.zebra.rfidreader.demo.rfid.RFIDController.ledState;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mConnectedDevice;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mConnectedReader;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mInventoryStartPending;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mIsInventoryRunning;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mRRStartedTime;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mReaderDisappeared;
import static com.zebra.rfidreader.demo.rfid.RFIDController.pc;
import static com.zebra.rfidreader.demo.rfid.RFIDController.phase;
import static com.zebra.rfidreader.demo.rfid.RFIDController.readers;
import static com.zebra.rfidreader.demo.rfid.RFIDController.regionNotSet;
import static com.zebra.rfidreader.demo.rfid.RFIDController.reset;
import static com.zebra.rfidreader.demo.rfid.RFIDController.rssi;
import static com.zebra.rfidreader.demo.rfid.RFIDController.settings_startTrigger;
import static com.zebra.rfidreader.demo.rfid.RFIDController.settings_stopTrigger;
import static com.zebra.rfidreader.demo.rfid.RFIDController.tagListMatchAutoStop;
import static com.zebra.rfidreader.demo.rfid.RFIDController.tagListMatchNotice;
import static com.zebra.rfidreader.demo.rfid.RFIDController.toneGenerator;
import static com.zebra.rfidreader.demo.settings.AdvancedOptionsContent.DPO_ITEM_INDEX;


public class MainActivity extends AppCompatActivity implements Readers.RFIDReaderEventHandler,
        NavigationView.OnNavigationItemSelectedListener, ISettingsUtil {
    //Tag to identify the currently displayed fragment
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    //Messages for progress bar
    private static final String MSG_READ = "Reading Tags";
    private static final String MSG_WRITE = "Writing Data";
    private static final String MSG_LOCK = "Executing Lock Command";
    private static final String MSG_KILL = "Executing Kill Command";
    private static final int BEEP_DELAY_TIME_MIN = 0;
    private static final int BEEP_DELAY_TIME_MAX = 300;
    public static final String BRAND_ID = "brandid";
    public static final String EPC_LEN = "epclen";
    public static final String IS_BRANDID_CHECK = "brandidcheck";
    /**
     * method to start a timer task for LED glow for the duration of 10ms
     */
    public static Timer tLED;
    private static ArrayList<ReaderDeviceFoundHandler> readerDeviceFoundHandlers = new ArrayList<>();
    private static ArrayList<BatteryNotificationHandler> batteryNotificationHandlers = new ArrayList<>();

    /**
     * method to start a timer task to beep for the duration of 10ms
     */
    public Timer tbeep;
    /**
     * method to start a timer task to beep for locate functionality and configure the ON OFF duration.
     */
    public Timer locatebeep;

    protected boolean isInventoryAbortedNotifier;

    protected int accessTagCount;
    //To indicate indeterminate progress
    protected CustomProgressDialog progressDialog;
    protected Menu menu;
    NotificationManager notificationManager;
    MediaPlayer mPlayer;
    //Special layout for Navigation Drawer
    private DrawerLayout mDrawerLayout;
    //List view for navigation drawer items
    private CharSequence mTitle;
    private String[] mOptionTitles;

    public static AsyncTask<Void, Void, Boolean> DisconnectTask;

    //for beep and LED
    private boolean beepON = false;
    private boolean beepONLocate = false;
    private String TAG = "RFIDDEMO";
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 10;
    private static final int REQUEST_CODE_ASK_PERMISSIONS_CSV = 11;

    //common Result Intent broadcasted by DataWedge
    private static final String DW_APIRESULT_ACTION = "com.symbol.datawedge.api.RESULT_ACTION";
    private static final String scanner_status = "com.symbol.datawedge.scanner_status";
    private NavigationView navigationView;

    private Fragment previousFragment;

    public static MainActivity.EventHandler eventHandler;

    private static boolean activityVisible;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }
    //For multitag locate operation
    protected boolean isMultiTagLocationingAborted;
    /**
     * method to know whether bluetooth is enabled or not
     *
     * @return - true if bluetooth enabled
     * - false if bluetooth disabled
     */
    public static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    /**
     * Method for registering the classes for device events like paired,unpaired, connected and disconnected.
     * The registered classes will get notified when device event occurs.
     *
     * @param readerDeviceFoundHandler - handler class to register with base receiver activity
     */
    public static void addReaderDeviceFoundHandler(ReaderDeviceFoundHandler readerDeviceFoundHandler) {
        readerDeviceFoundHandlers.add(readerDeviceFoundHandler);
    }

    public static void addBatteryNotificationHandler(BatteryNotificationHandler batteryNotificationHandler) {
        batteryNotificationHandlers.add(batteryNotificationHandler);
    }

    public static void removeReaderDeviceFoundHandler(ReaderDeviceFoundHandler readerDeviceFoundHandler) {
        readerDeviceFoundHandlers.remove(readerDeviceFoundHandler);
    }

    public static void removeBatteryNotificationHandler(BatteryNotificationHandler batteryNotificationHandler) {
        batteryNotificationHandlers.remove(batteryNotificationHandler);
    }


    private static FragmentManager supportmanager;
    private ArrayList<ReaderDevice> readersListArray;
    FloatingActionButton inventoryBT = null;
    private Toast myToast;
    private static final int TIME_DELAY = 4000;
    private static long lastToastShowTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTitle = getTitle();
        mOptionTitles = getResources().getStringArray(R.array.options_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        android.support.v7.app.ActionBarDrawerToggle toggle = new android.support.v7.app.ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        supportmanager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            Toast.makeText(getApplicationContext(), "savedInstanceState == null", Toast.LENGTH_SHORT).show();
            selectItem(0);
            eventHandler = new EventHandler();
            initializeConnectionSettings();
        } else {
            Toast.makeText(getApplicationContext(), "savedInstanceState != null", Toast.LENGTH_SHORT).show();
            try {
                if (mConnectedReader != null) {
                    mConnectedReader.Events.removeEventsListener(eventHandler);
                    eventHandler = new EventHandler();
                    mConnectedReader.Events.addEventsListener(eventHandler);
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
        Inventorytimer.getInstance().setActivity(this);
        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }
        readers.attach(this);
        // Create a filter for the broadcast intent
        IntentFilter filter = new IntentFilter();
        // filter.addAction(scanner_status);
        filter.addAction(ACTION_SCREEN_OFF);
        filter.addAction(ACTION_SCREEN_ON);
        filter.addAction(DW_APIRESULT_ACTION);
        filter.addCategory("android.intent.category.DEFAULT");
        registerReceiver(BroadcastReceiver, filter);
        if (savedInstanceState == null) {
            loadReaders(this);
            // creates DW profile for Demo application
            getInstance().clearAllInventoryData();
            createDWProfile();
        } else if (AUTO_RECONNECT_READERS) {
            AutoConnectDevice();

        }
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mReceiver, bluetoothFilter);
    }

    private void loadReaders(final MainActivity context) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                InvalidUsageException invalidUsageException = null;
                try {
                    ArrayList<ReaderDevice> readersListArray = readers.GetAvailableRFIDReaderList();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                    invalidUsageException = e;
                }
                if (invalidUsageException != null) {
                    readers.Dispose();
                    readers = null;
                    if (!isBluetoothEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableIntent);
                    }
                    Application.isReaderConnectedThroughBluetooth = true;
                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                    readers.attach(context);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (AUTO_RECONNECT_READERS && mConnectedDevice == null) {
                    AutoConnectDevice();
                }
            }
        }.execute();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        // setButtonText("Bluetooth off");

                        if (ReadersListFragment.getInstance().isVisible()) {

                            ReadersListFragment.getInstance().loadUIData();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (ReadersListFragment.getInstance().isVisible()) {

                            ReadersListFragment.getInstance().loadUIData();
                        }
                        //Toast.makeText(context, "Bluetooth on", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // setButtonText("Turning Bluetooth on...");
                        break;
                }

            }

            final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            if (bondState == BluetoothDevice.BOND_NONE) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mConnectedReader != null && mConnectedReader.getHostName() != null && mConnectedReader.getHostName().equals(device.getName())) {

                    if (mConnectedReader.isConnected()) {
                        try {
                            mConnectedReader.disconnect();
                        } catch (InvalidUsageException e) {
                            e.printStackTrace();
                        } catch (OperationFailureException e) {
                            e.printStackTrace();
                        }

                        clearConnectedReader();
                        //BluetoothHandler.pair(device.getAddress());
                    } else {
                        clearConnectedReader();
                    }

                } else if (LAST_CONNECTED_READER.equals(device.getName())) {
                    clearConnectedReader();

                }
            }
        }
    };


    private void ledsettigs() {
        SharedPreferences sharedPref = this.getSharedPreferences("LEDPreferences", Context.MODE_PRIVATE);
        ledState = sharedPref.getBoolean("LED_STATE1", true);

    }

    private void beeperSettings() {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.pref_beeper), Context.MODE_PRIVATE);
        int volume = sharedPref.getInt(getString(R.string.beeper_volume), 0);
        int streamType = AudioManager.STREAM_DTMF;
        int percantageVolume = 100;
        if (volume == 0) {
            beeperVolume = BEEPER_VOLUME.HIGH_BEEP;
            percantageVolume = 100;
        }
        if (volume == 1) {
            beeperVolume = BEEPER_VOLUME.MEDIUM_BEEP;
            percantageVolume = 75;
        }
        if (volume == 2) {
            beeperVolume = BEEPER_VOLUME.LOW_BEEP;
            percantageVolume = 50;
        }
        if (volume == 3) {
            beeperVolume = BEEPER_VOLUME.QUIET_BEEP;
            percantageVolume = 0;
        }

        try {
            toneGenerator = new ToneGenerator(streamType, percantageVolume);
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            toneGenerator = null;

        }
    }

    private String getReaderPassword(String address) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.READER_PASSWORDS, 0);
        return sharedPreferences.getString(address, null);
    }

    // Autoconnect reader on detatch and attach or reader reboot.
    public synchronized void AutoConnectDevice() {
        CustomProgressDialog progressDialog = null;
        if (MainActivity.isActivityVisible()) {
            progressDialog =
                    new CustomProgressDialog(MainActivity.this,
                            "Connecting with " + LAST_CONNECTED_READER);
            progressDialog.show();

        }
        final CustomProgressDialog finalProgressDialog = progressDialog;
        getInstance().AutoConnectDevice(getReaderPassword(LAST_CONNECTED_READER), eventHandler,
                new RfidListeners() {
                    @Override
                    public void onSuccess(Object object) {

                        StoreConnectedReader();
                        if (mConnectedDevice != null) {
                            ReaderDeviceConnected(mConnectedDevice);
                        }
                        if (finalProgressDialog != null && finalProgressDialog.isShowing())
                            finalProgressDialog.dismiss();
                        if (regionNotSet) {
                            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                            startActivity(intent);
                            Intent detailsIntent = new Intent(getApplicationContext(), SettingsDetailActivity.class);
                            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            detailsIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.regulatory);
                            startActivity(detailsIntent);
                        }

                    }

                    @Override
                    public void onFailure(Exception exception) {
                        if (exception != null && ((OperationFailureException) exception).getResults() ==
                                RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {
                            ReaderDeviceConnected(mConnectedDevice);
                            sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.set_region_msg));
                            Intent detailsIntent = new Intent(getApplicationContext(), SettingsDetailActivity.class);
                            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            detailsIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.regulatory);
                            startActivity(detailsIntent);


                        } else if (exception != null && ((OperationFailureException) exception).getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                            if (NOTIFY_READER_CONNECTION)
                                sendNotification(Constants.ACTION_READER_CONNECTED,
                                        "Connected to " + mConnectedDevice.getName());
                        }
                        if (finalProgressDialog != null)
                            finalProgressDialog.dismiss();
                        if (exception != null && exception.getMessage() != null)
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        if (finalProgressDialog != null)
                            finalProgressDialog.dismiss();
                        //  Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    }


                }, message -> runOnUiThread(() -> {

                    if (finalProgressDialog != null) {
                        finalProgressDialog.setMessage("Connecting with " + message);
                        finalProgressDialog.show();
                    }
                }));


    }


    private void EnableLogger() {
        if (mConnectedReader != null)
            mConnectedReader.Config.setLogLevel(Level.INFO);
    }

    private void PrintLogs() {
        if (mConnectedReader != null) {
            try {
                String str[] = mConnectedReader.Config.GetLogBuffer().split("\n");
                for (String st : str) {
                    Log.d(TAG, st);
                }
            } catch (InvalidUsageException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Method to initialize the connection settings like notifications, auto detection, auto reconnection etc..
     */
    private void initializeConnectionSettings() {
        SharedPreferences settings = getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        AUTO_DETECT_READERS = settings.getBoolean(Constants.AUTO_DETECT_READERS, true);
        AUTO_RECONNECT_READERS = settings.getBoolean(Constants.AUTO_RECONNECT_READERS, true);
        NOTIFY_READER_AVAILABLE = settings.getBoolean(Constants.NOTIFY_READER_AVAILABLE, false);
        NOTIFY_READER_CONNECTION = settings.getBoolean(Constants.NOTIFY_READER_CONNECTION, false);
        if (Build.MODEL.contains("MC33"))
            NOTIFY_BATTERY_STATUS = settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, false);
        else
            NOTIFY_BATTERY_STATUS = settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, true);
        EXPORT_DATA = settings.getBoolean(Constants.EXPORT_DATA, false);
        TAG_LIST_MATCH_MODE = settings.getBoolean(Constants.TAG_LIST_MATCH_MODE, false);
        SHOW_CSV_TAG_NAMES = settings.getBoolean(Constants.SHOW_CSV_TAG_NAMES, false);
        asciiMode = settings.getBoolean(Constants.ASCII_MODE, false);
        NON_MATCHING = settings.getBoolean(Constants.NON_MATCHING, false);
        LAST_CONNECTED_READER = settings.getString(Constants.LAST_READER, "");
        LoadProfiles();
        beeperSettings();
        ledsettigs();
        LoadTagListCSV();
        loadBrandIdValues();
        Application.MULTI_TAG_LOCATE_SORT = settings.getBoolean(Constants.MULTITAG_LOCATE_DATA_SORT, true);
        Application.MULTI_TAG_LOCATE_FOUND_PROXI_PERCENT = settings.getInt(Constants.MULTITAG_LOCATE_FOUND_PROXI_PERCENT_DATA, 100);
    }


    public void loadBrandIdValues() {
        SharedPreferences pref = getSharedPreferences("BrandIdValues", 0);
        strBrandID = pref.getString(BRAND_ID, "AAAA"); // getting String
        iBrandIDLen = pref.getInt(EPC_LEN, 12); // getting Integer
        brandidcheckenabled = pref.getBoolean(IS_BRANDID_CHECK, false);
    }

    private void LoadProfiles() {
        ProfileContent content = new ProfileContent(this);
        content.LoadDefaultProfiles();
    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(BroadcastReceiver);
        unregisterReceiver(mReceiver);
        disconnectReaderConnections();
        if (myToast != null)
            myToast.cancel();

        super.onDestroy();
    }

    private void disconnectReaderConnections() {
        //disconnect from reader
        if (mConnectedReader != null) {

            try {
                if (mConnectedReader.isConnected()) {
                    mConnectedReader.Events.removeEventsListener(eventHandler);
                }
                mConnectedReader.disconnect();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
        mConnectedReader = null;
        // update dpo icon in settings list
        AdvancedOptionsContent.ITEMS.get(DPO_ITEM_INDEX).icon = R.drawable.title_dpo_disabled;
        getInstance().clearSettings();
        mConnectedDevice = null;
        ReadersListFragment.readersList.clear();
        if (readers != null) {
            readers.deattach(MainActivity.this);
            readers.Dispose();
            readers = null;
        }
        reset();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_dpo:
                Intent detailsIntent = new Intent(MainActivity.this, SettingsDetailActivity.class);
                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                detailsIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.battery);
                startActivity(detailsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_rapidread:
                selectItem(1);
                break;
            case R.id.nav_inventory:
                selectItem(2);
                break;
            case R.id.nav_locatetag:
                selectItem(3);
                break;
            case R.id.nav_profiles:
                selectItem(9);
//            {
//                Intent detailsIntent = new Intent(getApplicationContext(), SettingsDetailActivity.class);
//                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                detailsIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.profiles);
//                startActivity(detailsIntent);
//            }
                break;
            case R.id.nav_settings:
                selectItem(4);
                break;
            case R.id.nav_access_control:
                selectItem(5);
                break;
            case R.id.nav_prefilters:
                selectItem(6);
                break;
//            case R.id.nav_readerslist:
//                selectItem(7);
//                break;
            case R.id.nav_about:
                selectItem(8);
                break;
            case R.id.nav_socket:
                selectItem(10);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Method called on the click of a NavigationDrawer item to update the UI with the new selection
     *
     * @param position - postion of the item selected
     */
    public void selectItem(int position) {
        // update the no_items content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = HomeFragment.newInstance();
                break;
            case 1:
                fragment = RapidReadFragment.newInstance();
                break;
            case 2:
                fragment = InventoryFragment.newInstance();
                break;
            case 3:
                fragment = LocateOperationsFragment.newInstance();
                break;
            case 4:
                fragment = new SettingListFragment();
                break;
            case 5:
                fragment = AccessOperationsFragment.newInstance();
                break;
            case 6:
                fragment = PreFilterFragment.newInstance();
                break;
            case 7:
                fragment = ReadersListFragment.newInstance();
                break;
            case 8:
                fragment = AboutFragment.newInstance();
                break;
            case 9:
                fragment = ProfileFragment.newInstance();
                break;
            case 10:
                fragment = SocketFragment.newInstance();
                break;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        previousFragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (position == 0) {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Don't add the transaction to back stack since we are navigating to the first fragment
            //being displayed and adding the same to the backstack will result in redundancy
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        } else {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Add the transaction to the back stack since we want the state to be preserved in the back stack
            //if (position != 9)
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).addToBackStack(null).commit();
        }
        // update selected item and title, then close the drawer
        //mDrawerList.setItemChecked(position, true);
        setTitle(mOptionTitles[position]);
        //mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void showPreviousFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = previousFragment;
        previousFragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).addToBackStack(null).commit();


    }

    /**
     * method to get currently displayed action bar icon
     *
     * @return resource id of the action bar icon
     */
    private int getActionBarIcon() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof RapidReadFragment)
            return R.drawable.dl_rr;
        else if (fragment instanceof InventoryFragment)
            return R.drawable.dl_inv;
        else if (fragment instanceof SingleTagLocateFragment)
            return R.drawable.dl_loc;
        else if (fragment instanceof SettingListFragment)
            return R.drawable.dl_sett;
        else if (fragment instanceof AccessOperationsFragment)
            return R.drawable.dl_access;
        else if (fragment instanceof PreFilterFragment)
            return R.drawable.dl_filters;
        else if (fragment instanceof ReadersListFragment)
            return R.drawable.dl_rdl;
        else if (fragment instanceof AboutFragment)
            return R.drawable.dl_about;
        else
            return -1;
    }


    @Override
    public void onResume() {
        super.onResume();
        activityResumed();
        disableScanner();
    }


    /**
     * call back of activity,which will call before activity went to paused
     */
    @Override
    public void onPause() {
        super.onPause();
        activityPaused();
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //mDrawerToggle.syncState();
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks whether a hardware keyboard is available
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment != null && fragment instanceof InventoryFragment && newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            findViewById(R.id.inventoryDataLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.inventoryButton).setVisibility(View.INVISIBLE);
        } else if (fragment != null && fragment instanceof InventoryFragment && newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            findViewById(R.id.inventoryDataLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.inventoryButton).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //update the selected item in the drawer and the title
            //mDrawerList.setItemChecked(0, true);
            setTitle(mOptionTitles[0]);
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
            if (fragment != null && fragment instanceof BackPressedFragment) {
                ((BackPressedFragment) fragment).onBackPressed();
            } else if (fragment != null && fragment instanceof HomeFragment) {
                //stop Timer
                Inventorytimer.getInstance().stopTimer();
                getInstance().stopTimer();
                //
                if (DisconnectTask != null)
                    DisconnectTask.cancel(true);

                //Alert Dialog
                showMessageOKCancel("Do you want to close this application?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.super.onBackPressed();
                            }
                        });
            } else {
                super.onBackPressed();
            }
        }
    }

    public void inventoryStartOrStop(View view) {
        inventoryStartOrStop();
    }

    /**
     * Callback method to handle the click of start/stop button in the inventory fragment
     */
    public synchronized void inventoryStartOrStop() {

        if (MatchModeFileLoader.getInstance(this).isImportTaskRunning()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.loading_csv), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Loading CSV");
            return;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment != null && fragment instanceof InventoryFragment) {
            inventoryBT = findViewById(R.id.inventoryButton);
        } else if (fragment != null && fragment instanceof RapidReadFragment) {
            inventoryBT = findViewById( R.id.rr_inventoryButton );
        }

        //tagListMatchNotice = false;
        if (mConnectedReader != null && mConnectedReader.isConnected()) {
            if (!mIsInventoryRunning) {
                clearInventoryData();
                //button.setText("STOP");
                if (inventoryBT != null) {
                    inventoryBT.setImageResource(R.drawable.ic_play_stop);
                }
                //Here we send the inventory command to start reading the tags
                if (fragment != null && fragment instanceof InventoryFragment) {
                    Spinner memoryBankSpinner = ((Spinner) findViewById(R.id.inventoryOptions));
                    memoryBankSpinner.setSelection(memoryBankId);
                    memoryBankSpinner.setEnabled(false);
                    ((InventoryFragment) fragment).resetTagsInfo();
                }
                //set flag value
                isInventoryAborted = false;
                RFIDController.getInstance().getTagReportingFields();
                //if (!Application.mConnectedReader.getHostName().startsWith("RFD8500") && Application.batchMode != 2 || Application.mConnectedReader.getHostName().startsWith("RFD8500") && Application.batchMode != 2)
                {
                    PrepareMatchModeList();
                }
                // UI update for inventory fragment
                if (fragment != null && fragment instanceof InventoryFragment && TAG_LIST_MATCH_MODE) {
                    // TODO: This logic requires updates adpater being assigned particular list
                    if (memoryBankId == 0) {
                        ((InventoryFragment) fragment).getAdapter().originalInventoryList = tagsReadInventory;
                        ((InventoryFragment) fragment).getAdapter().searchItemsList = tagsReadInventory;
                        ((InventoryFragment) fragment).getAdapter().notifyDataSetChanged();
                    } else if (memoryBankId == 1) {  //matching tags
                        ((InventoryFragment) fragment).getAdapter().originalInventoryList = matchingTagsList;
                        ((InventoryFragment) fragment).getAdapter().searchItemsList = matchingTagsList;
                        ((InventoryFragment) fragment).getAdapter().notifyDataSetChanged();
                    } else if (memoryBankId == 2) {  //missing tags
                        missingTagsList.addAll(tagsListCSV);
                        ((InventoryFragment) fragment).getAdapter().originalInventoryList = missingTagsList;
                        ((InventoryFragment) fragment).getAdapter().searchItemsList = missingTagsList;
                        ((InventoryFragment) fragment).getAdapter().notifyDataSetChanged();
                    } else if (memoryBankId == 3) {  //unknown tags
                        ((InventoryFragment) fragment).getAdapter().originalInventoryList = unknownTagsList;
                        ((InventoryFragment) fragment).getAdapter().searchItemsList = unknownTagsList;
                        ((InventoryFragment) fragment).getAdapter().notifyDataSetChanged();
                    }
                    tagsReadForSearch.addAll(((InventoryFragment) fragment).getAdapter().searchItemsList);
                }
                // UI update for RR fragment
                if (fragment != null && fragment instanceof RapidReadFragment) {
                    memoryBankId = -1;
                    ((RapidReadFragment) fragment).resetTagsInfo();
                    if (TAG_LIST_MATCH_MODE) {
                        if (missedTags > 9999) {
                            TextView uniqueTags = (TextView) findViewById(R.id.uniqueTagContent);
                            //orignal size is 60sp - reduced size 45sp
                            uniqueTags.setTextSize(45);
                        }
                    }
                    ((RapidReadFragment) fragment).updateTexts();
                }
                // perform read or inventory
                if (fragment != null && fragment instanceof InventoryFragment && !RFIDController.regionNotSet && !((InventoryFragment) fragment).getMemoryBankID().equalsIgnoreCase("none") && !TAG_LIST_MATCH_MODE) {
                    //If memory bank is selected, call read command with appropriate memory bank
                    getInstance().inventoryWithMemoryBank(
                            ((InventoryFragment) fragment).getMemoryBankID(),
                            new RfidListeners() {
                                @Override
                                public void onSuccess(Object object) {
                                    Log.d(TAG, "onSuccess");
                                }

                                @Override
                                public void onFailure(Exception exception) {

                                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                                    if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                                        ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).
                                                handleStatusResponse(((OperationFailureException) exception).getResults());
                                    sendNotification( Constants.ACTION_READER_STATUS_OBTAINED, ((OperationFailureException) exception).getVendorMessage());
                                }

                                @Override
                                public void onFailure(String message) {
                                    Toast.makeText( MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                    if (inventoryBT != null) {
                                        inventoryBT.setImageResource(android.R.drawable.ic_media_play);
                                    }
                                }
                            }
                    );
                } else {
                    //Perform inventory
                    try {
                        mIsInventoryRunning = true;
                        getInstance().performInventory(new RfidListeners() {
                            @Override
                            public void onSuccess(Object object) {
                                //Log.d(TAG, "onSuccess");
                                tagListMatchNotice = false;
                            }

                            @Override
                            public void onFailure(Exception exception) {

                                Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                                if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                                    ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).
                                            handleStatusResponse(((OperationFailureException) exception).getResults());
                                sendNotification( Constants.ACTION_READER_STATUS_OBTAINED, ((OperationFailureException) exception).getVendorMessage());
                            }

                            @Override
                            public void onFailure(String message) {
                                if (inventoryBT != null) {
                                    inventoryBT.setImageResource(android.R.drawable.ic_media_play);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //Perform stop inventory
            } else if (mIsInventoryRunning) {
                mInventoryStartPending = false;
                if (fragment != null && fragment instanceof InventoryFragment) {
                    ((Spinner) findViewById(R.id.inventoryOptions)).setEnabled(true);
                }
                //button.setText("START");
                if (inventoryBT != null) {
                    inventoryBT.setImageResource(android.R.drawable.ic_media_play);
                }

                isInventoryAborted = true;
                //Here we send the abort command to stop the inventory
                try {
                    getInstance().stopInventory(new RfidListeners() {
                        @Override
                        public void onSuccess(Object object) {
                            Application.bBrandCheckStarted = false;
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            if (exception == null || exception instanceof OperationFailureException) {
                                operationHasAborted();
                            }
                        }

                        @Override
                        public void onFailure(String message) {
                            if (inventoryBT != null) {
                                inventoryBT.setImageResource(android.R.drawable.ic_media_play);
                            }
                        }
                    });
                    Log.d(TAG, "Inventory.stop");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback method to handle the click of start/stop button in the multitag locate fragment
     *
     * @param v - Button Clicked
     */
    public void multiTagLocateStartOrStop(View v) {
        //if (isBluetoothEnabled() || !RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
            if (mConnectedReader != null && mConnectedReader.isConnected()) {
                if (Application.multiTagLocateTagListExist) {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                    if (!Application.mIsMultiTagLocatingRunning) {
                        ((FloatingActionButton) v).setImageResource(R.drawable.ic_play_stop);
                        Application.mIsMultiTagLocatingRunning = true;
                        new AsyncTask<Void, Void, Boolean>() {
                            private InvalidUsageException invalidUsageException;
                            private OperationFailureException operationFailureException;

                            @Override
                            protected Boolean doInBackground(Void... voids) {
                                try {
                                    mConnectedReader.Actions.MultiTagLocate.perform();
                                } catch (InvalidUsageException e) {
                                    e.printStackTrace();
                                    invalidUsageException = e;
                                } catch (OperationFailureException e) {
                                    e.printStackTrace();
                                    operationFailureException = e;
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Boolean result) {
                                if (invalidUsageException != null) {
                                    if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                                        ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).handleStatusResponse(operationFailureException.getResults());
                                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, invalidUsageException.getInfo());
                                } else if (operationFailureException != null) {
                                    if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                                        ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).handleStatusResponse(operationFailureException.getResults());
                                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, operationFailureException.getVendorMessage());
                                }
                                if(invalidUsageException == null && operationFailureException == null) {
                                    ((LocateOperationsFragment)fragment).enableGUIComponents(false);
                                }
                            }
                        }.execute();
                    } else {
                        new AsyncTask<Void, Void, Boolean>() {
                            private InvalidUsageException invalidUsageException;
                            private OperationFailureException operationFailureException;

                            @Override
                            protected Boolean doInBackground(Void... voids) {
                                try {
                                    mConnectedReader.Actions.MultiTagLocate.stop();
                                    if (((Application.settings_startTrigger != null && (Application.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD || Application.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC)))
                                            || (Application.isBatchModeInventoryRunning != null && Application.isBatchModeInventoryRunning))
                                        operationHasAborted();
                                } catch (InvalidUsageException e) {
                                    invalidUsageException = e;
                                    e.printStackTrace();
                                } catch (OperationFailureException e) {
                                    operationFailureException = e;
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Boolean result) {
                                if (invalidUsageException != null) {
                                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, invalidUsageException.getInfo());
                                } else if (operationFailureException != null) {
                                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, operationFailureException.getVendorMessage());
                                }
                                if(invalidUsageException == null && operationFailureException == null) {
                                    ((LocateOperationsFragment)fragment).enableGUIComponents(true);
                                }
                            }
                        }.execute();
                        ((FloatingActionButton) v).setImageResource(android.R.drawable.ic_media_play);
                        isMultiTagLocationingAborted = true;
                    }
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_error_no_data_loaded), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        //} else
        //    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }

    public void multiTagLocateAddTagItem(View v) {
        //if (isBluetoothEnabled() || !RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
            if (mConnectedReader != null && mConnectedReader.isConnected()) {
                if (Application.multiTagLocateTagListExist) {
                    if (!Application.mIsMultiTagLocatingRunning) {
                        String tagID = ((AutoCompleteTextView) findViewById(R.id.multiTagLocate_epc)).getText().toString();
                        if(RFIDController.asciiMode) {
                            tagID = asciitohex.convert(tagID);
                        }
                        if (!tagID.isEmpty()) {
                            if(Application.multiTagLocateTagListMap.containsKey(tagID)) {
                                try {
                                    if(mConnectedReader.Actions.MultiTagLocate.addItem(tagID, Application.multiTagLocateTagMap.get(tagID)) == 0) {
                                        Application.multiTagLocateTagListMap.get(tagID).setReadCount(0);
                                        Application.multiTagLocateTagListMap.get(tagID).setProximityPercent((short)0);
                                        Application.multiTagLocateActiveTagItemList.add(Application.multiTagLocateTagListMap.get(tagID));
                                        ((LocateOperationsFragment) getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT)).handleLocateTagResponse();
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_add_item_success), Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_add_item_failed), Toast.LENGTH_SHORT).show();
                                } catch (InvalidUsageException e) {
                                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, e.getInfo());
                                } catch (OperationFailureException e) {
                                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, e.getVendorMessage());
                                }
                            } else
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_add_item_failed), Toast.LENGTH_SHORT).show();
                        }
                    } else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_error_operation_running), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_error_no_data_loaded), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        //} else
        //    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }

    public void multiTagLocateDeleteTagItem(View v) {
        //if (isBluetoothEnabled() || !RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
            if (mConnectedReader != null && mConnectedReader.isConnected()) {
                if (Application.multiTagLocateTagListExist) {
                    if (!Application.mIsMultiTagLocatingRunning) {
                        String tagID = ((AutoCompleteTextView) findViewById(R.id.multiTagLocate_epc)).getText().toString();
                        if(RFIDController.asciiMode) {
                            tagID = asciitohex.convert(tagID);
                        }
                        if (!tagID.isEmpty()) {
                            if(Application.multiTagLocateTagListMap.containsKey(tagID)) {
                                try {
                                    if(mConnectedReader.Actions.MultiTagLocate.deleteItem(tagID) == 0) {
                                        Application.multiTagLocateActiveTagItemList.remove(Application.multiTagLocateTagListMap.get(tagID));
                                        ((LocateOperationsFragment) getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT)).handleLocateTagResponse();
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_delete_item_success), Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_delete_item_failed), Toast.LENGTH_SHORT).show();
                                } catch (InvalidUsageException e) {
                                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, e.getInfo());
                                } catch (OperationFailureException e) {
                                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, e.getVendorMessage());
                                }
                            } else
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_delete_item_failed), Toast.LENGTH_SHORT).show();
                        }
                    } else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_error_operation_running), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_error_no_data_loaded), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        //} else
        //    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }

    public void multiTagLocateReset(View v) {
        //if (isBluetoothEnabled() || !RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
            if (mConnectedReader != null && mConnectedReader.isConnected()) {
                if (Application.multiTagLocateTagListExist) {
                    if (!Application.mIsMultiTagLocatingRunning) {
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                        if (fragment instanceof LocateOperationsFragment) {
                            ((LocateOperationsFragment) fragment).resetMultiTagLocateDetail(false);
                        }
                    } else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_error_operation_running), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.multiTag_locate_error_no_data_loaded), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        //} else
        //    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }

    private void PrepareMatchModeList() {
        Log.d(TAG, "PrepareMatchModeList");
        if (TAG_LIST_MATCH_MODE && !TAG_LIST_LOADED) {
            //This for loop will reset all the items in the tagsListCSV(making Tag count to zero)
            for (int i = 0; i < tagsListCSV.size(); i++) {
                InventoryListItem inv = null;
                if (tagsListCSV.get(i).getCount() != 0) {
                    inv = tagsListCSV.remove(i);
                    InventoryListItem inventoryListItem = new InventoryListItem(inv.getTagID(), 0, null, null, null, null, null, null);
                    inventoryListItem.setTagDetails(inv.getTagDetails());
                    tagsListCSV.add(i, inventoryListItem);
                } else {
                    if (tagsListCSV.get(i).isVisible()) {
                        tagsListCSV.get(i).setVisible(false);
                    }
                }
            }
            UNIQUE_TAGS_CSV = tagsListCSV.size();
            tagsReadInventory.addAll(tagsListCSV);
            inventoryList.putAll(tagListMap);
            missedTags = tagsListCSV.size();
            matchingTags = 0;
            TAG_LIST_LOADED = true;
            Log.d(TAG, "PrepareMatchModeList done");
        }
    }
    /**
     * Method to call when we want inventory to happen with memory bank parameters
     *
     * @param memoryBankID id of the memory bank
     */


    /**
     * Method called when read button in AccessOperationsFragment is clicked
     *
     * @param v - Read Button
     */
    public void accessOperationsReadClicked(View v) {
        AutoCompleteTextView tagIDField = findViewById(R.id.accessRWTagID);
        final String tagId = (asciiMode == true ? asciitohex.convert(tagIDField.getText().toString()) : tagIDField.getText().toString());
        String offsetText = ((EditText) findViewById(R.id.accessRWOffsetValue)).getText().toString();
        String lengthText = ((EditText) findViewById(R.id.accessRWLengthValue)).getText().toString();
        final TextView accessRWData = findViewById(R.id.accessRWData);
        String accessRWpassword = ((EditText) findViewById(R.id.accessRWPassword)).getText().toString();
        String bankItem = ((Spinner) findViewById(R.id.accessRWMemoryBank)).getSelectedItem().toString();
        progressDialog = new CustomProgressDialog(this, MSG_READ);
        progressDialog.show();
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (accessRWData != null) {
            accessRWData.setText("");
        }
        timerDelayRemoveDialog(Constants.RESPONSE_TIMEOUT, progressDialog, "Read");
        String tagValue;
        if (asciiMode == true)
            tagValue = asciitohex.convert(tagId);
        else tagValue = tagId;
        if (mConnectedReader == null || !mConnectedReader.isConnected())
            Toast.makeText(getApplicationContext(), "No Active Connection with Reader", Toast.LENGTH_SHORT).show();
        else if (!mConnectedReader.isCapabilitiesReceived())
            Toast.makeText(getApplicationContext(), "Reader capabilities not updated", Toast.LENGTH_SHORT).show();
        else if (tagValue.isEmpty())
            Toast.makeText(getApplicationContext(), "Please fill Tag Id", Toast.LENGTH_SHORT).show();
        else if (offsetText.isEmpty())
            Toast.makeText(getApplicationContext(), "Please fill offset", Toast.LENGTH_SHORT).show();
        else if (lengthText.isEmpty())
            Toast.makeText(getApplicationContext(), "Please fill length", Toast.LENGTH_SHORT).show();
        else
            getInstance().accessOperationsRead(tagId, offsetText, lengthText, accessRWpassword, bankItem,
                    new RfidListeners() {
                        @Override
                        public void onSuccess(Object object) {
                            progressDialog.dismiss();
                            if (isAccessCriteriaRead && !mIsInventoryRunning) {
                                if (fragment instanceof AccessOperationsFragment)
                                    ((AccessOperationsFragment) fragment).handleTagResponse((TagData) object);
                            }


                        }

                        @Override
                        public void onFailure(Exception exception) {
                            progressDialog.dismiss();
                            if (exception instanceof InvalidUsageException) {
                                sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((InvalidUsageException) exception).getInfo());
                            } else if (exception instanceof OperationFailureException) {
                                sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((OperationFailureException) exception).getVendorMessage());
                            } else {
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(String message) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });


    }


    /**
     * Method called when write button in AccessOperationsFragment is clicked
     *
     * @param v - Write Button
     */
    public void accessOperationsWriteClicked(View v) {
        AutoCompleteTextView tagIDField = findViewById(R.id.accessRWTagID);
        final String tagId = (asciiMode == true ? asciitohex.convert(tagIDField.getText().toString()) : tagIDField.getText().toString());
        String offsetText = ((EditText) findViewById(R.id.accessRWOffsetValue)).getText().toString();
        String lengthText = ((EditText) findViewById(R.id.accessRWLengthValue)).getText().toString();
        final String accessRWData = ((EditText) findViewById(R.id.accessRWData)).getText().toString();
        String accessRWpassword = ((EditText) findViewById(R.id.accessRWPassword)).getText().toString();
        String bankItem = ((Spinner) findViewById(R.id.accessRWMemoryBank)).getSelectedItem().toString();
        progressDialog = new CustomProgressDialog(this, MSG_WRITE);
        progressDialog.show();
        timerDelayRemoveDialog(Constants.RESPONSE_TIMEOUT, progressDialog, "Write");
        String tagValue;
        if (asciiMode == true)
            tagValue = asciitohex.convert(tagId);
        else tagValue = tagId;
        if (mConnectedReader == null || !mConnectedReader.isConnected())
            Toast.makeText(getApplicationContext(), "No Active Connection with Reader", Toast.LENGTH_SHORT).show();
        else if (!mConnectedReader.isCapabilitiesReceived())
            Toast.makeText(getApplicationContext(), "Reader capabilities not updated", Toast.LENGTH_SHORT).show();
        else if (tagValue.isEmpty())
            Toast.makeText(getApplicationContext(), "Please fill Tag Id", Toast.LENGTH_SHORT).show();
        else if (offsetText.isEmpty())
            Toast.makeText(getApplicationContext(), "Please fill offset", Toast.LENGTH_SHORT).show();
        else if (lengthText.isEmpty())
            Toast.makeText(getApplicationContext(), "Please fill length", Toast.LENGTH_SHORT).show();
        else
            getInstance().accessOperationsWrite(tagValue, offsetText, lengthText, accessRWData, accessRWpassword, bankItem,
                    new RfidListeners() {
                        @Override
                        public void onSuccess(Object object) {
                            progressDialog.dismiss();
                            startbeepingTimer();
                            Toast.makeText(getApplicationContext(), getString(R.string.msg_write_succeed), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            progressDialog.dismiss();
                            if (exception instanceof InvalidUsageException) {
                                sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((InvalidUsageException) exception).getInfo());
                            } else if (exception instanceof OperationFailureException) {
                                sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((OperationFailureException) exception).getVendorMessage());
                            }

                        }

                        @Override
                        public void onFailure(String message) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });

    }

    /**
     * Method called when lock button in AccessOperationsFragment is clicked
     *
     * @param v - Lock button
     */
    public void accessOperationLockClicked(View v) {
        AutoCompleteTextView tagIDField = findViewById(R.id.accessRWTagID);
        final String tagId = (asciiMode == true ? asciitohex.convert(tagIDField.getText().toString()) : tagIDField.getText().toString());
        String accessRWpassword = ((EditText) findViewById(R.id.accessLockPassword)).getText().toString();
        progressDialog = new CustomProgressDialog(this, MSG_LOCK);
        progressDialog.show();
        timerDelayRemoveDialog(Constants.RESPONSE_TIMEOUT, progressDialog, "Lock");
        LOCK_DATA_FIELD lockDataField = null;
        LOCK_PRIVILEGE lockPrivilege = null;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment != null && fragment instanceof AccessOperationsFragment) {
            Fragment innerFragment = ((AccessOperationsFragment) fragment).getCurrentlyViewingFragment();
            if (innerFragment != null && innerFragment instanceof AccessOperationsLockFragment) {
                AccessOperationsLockFragment lockFragment = ((AccessOperationsLockFragment) innerFragment);
                String lockMemoryBank = lockFragment.getLockMemoryBank();
                if (lockMemoryBank != null && !lockMemoryBank.isEmpty()) {
                    if (lockMemoryBank.equalsIgnoreCase("epc"))
                        lockDataField = LOCK_DATA_FIELD.LOCK_EPC_MEMORY;
                    else if (lockMemoryBank.equalsIgnoreCase("tid"))
                        lockDataField = LOCK_DATA_FIELD.LOCK_TID_MEMORY;
                    else if (lockMemoryBank.equalsIgnoreCase("user"))
                        lockDataField = LOCK_DATA_FIELD.LOCK_USER_MEMORY;
                    else if (lockMemoryBank.equalsIgnoreCase("access pwd"))
                        lockDataField = LOCK_DATA_FIELD.LOCK_ACCESS_PASSWORD;
                    else if (lockMemoryBank.equalsIgnoreCase("kill pwd"))
                        lockDataField = LOCK_DATA_FIELD.LOCK_KILL_PASSWORD;
                    lockPrivilege = lockFragment.getLockAccessPermission();

                }
            }
        }
        String tagValue;
        if (asciiMode == true)
            tagValue = asciitohex.convert(tagId);
        else tagValue = tagId;
        if (mConnectedReader == null || !mConnectedReader.isConnected())
            Toast.makeText(getApplicationContext(), "No Active Connection with Reader", Toast.LENGTH_SHORT).show();
        else if (!mConnectedReader.isCapabilitiesReceived())
            Toast.makeText(getApplicationContext(), "Reader capabilities not updated", Toast.LENGTH_SHORT).show();
        else if (tagValue.isEmpty())
            Toast.makeText(getApplicationContext(), "Please fill Tag Id", Toast.LENGTH_SHORT).show();
        else
            getInstance().accessOperationLock(tagValue, accessRWpassword, lockDataField, lockPrivilege,
                    new RfidListeners() {
                        @Override
                        public void onSuccess(Object object) {
                            progressDialog.dismiss();
                            startbeepingTimer();
                            Toast.makeText(getApplicationContext(), getString(R.string.msg_lock_succeed), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            progressDialog.dismiss();
                            if (exception instanceof InvalidUsageException) {
                                sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((InvalidUsageException) exception).getInfo());
                            } else if (exception instanceof OperationFailureException) {
                                sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((OperationFailureException) exception).getVendorMessage());
                            }

                        }

                        @Override
                        public void onFailure(String message) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });

    }

    /**
     * Method called when kill button in AccessOperationsFragment is clicked
     *
     * @param v - Kill button
     */
    public void accessOperationsKillClicked(View v) {
        AutoCompleteTextView tagIDField = findViewById(R.id.accessKillTagID);
        final String tagId = (asciiMode == true ? asciitohex.convert(tagIDField.getText().toString()) : tagIDField.getText().toString());
        String accessRWpassword = ((EditText) findViewById(R.id.accessKillPassword)).getText().toString();
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        progressDialog = new CustomProgressDialog(this, MSG_KILL);
        progressDialog.show();
        timerDelayRemoveDialog(Constants.RESPONSE_TIMEOUT, progressDialog, "Kill");
        String tagValue;
        if (asciiMode == true)
            tagValue = asciitohex.convert(tagId);
        else tagValue = tagId;
        if (mConnectedReader == null || !mConnectedReader.isConnected())
            Toast.makeText(getApplicationContext(), "No Active Connection with Reader", Toast.LENGTH_SHORT).show();
        else if (!mConnectedReader.isCapabilitiesReceived())
            Toast.makeText(getApplicationContext(), "Reader capabilities not updated", Toast.LENGTH_SHORT).show();
        else if (tagValue.isEmpty())
            Toast.makeText(getApplicationContext(), "Please fill Tag Id", Toast.LENGTH_SHORT).show();
        else
            getInstance().accessOperationsKill(tagValue, accessRWpassword,
                    new RfidListeners() {
                        @Override
                        public void onSuccess(Object object) {
                            progressDialog.dismiss();
                            startbeepingTimer();
                            Toast.makeText(getApplicationContext(), getString(R.string.msg_kill_succeed), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            progressDialog.dismiss();
                            if (exception instanceof InvalidUsageException) {
                                sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((InvalidUsageException) exception).getInfo());
                            } else if (exception instanceof OperationFailureException) {
                                sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((OperationFailureException) exception).getVendorMessage());
                            }
                        }

                        @Override
                        public void onFailure(String message) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    /**
     * Method called when stop in locationing is clicked
     *
     * @param v - Locationing stop clicked
     */
    public void locationingButtonClicked(final View v) {

        FloatingActionButton btn_locate = findViewById( R.id.btn_locate );
        EditText lt_et_epc = (AutoCompleteTextView)findViewById( R.id.lt_et_epc );
        String locateTag = lt_et_epc.getText().toString();

        if (locateTag != null && !isLocatingTag && !locateTag.isEmpty()) {

            lt_et_epc.setFocusable( false );
            if (btn_locate != null) {
                btn_locate.setImageResource( R.drawable.ic_play_stop );
            }
            RangeGraph locationBar = findViewById( R.id.locationBar );
            locationBar.setValue( 0 );
            locationBar.invalidate();
            locationBar.requestLayout();
        } else {
            isLocationingAborted = true;
            if (btn_locate != null) {
                btn_locate.setImageResource(android.R.drawable.ic_media_play);
            }
            (findViewById(R.id.lt_et_epc)).setFocusableInTouchMode(true);
            (findViewById(R.id.lt_et_epc)).setFocusable(true);
        }
        getInstance().locationing(locateTag, new RfidListeners() {
            @Override
            public void onSuccess(Object object) {
                //  progressDialog.dismiss();
            }

            @Override
            public void onFailure(Exception exception) {
                //  progressDialog.dismiss();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                if (exception instanceof InvalidUsageException ) {
                    if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                        ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).handleStatusResponse(RFIDResults.RFID_API_PARAM_ERROR);
                    sendNotification( Constants.ACTION_READER_STATUS_OBTAINED, ((InvalidUsageException) exception).getInfo());
                } else if (exception instanceof OperationFailureException) {
                    if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                        ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).handleStatusResponse(((OperationFailureException) exception).getResults());
                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, ((OperationFailureException) exception).getVendorMessage());
                }
            }
            @Override
            public void onFailure(String message) {
                //  progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                    ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).handleStatusResponse(RFIDResults.RFID_API_UNKNOWN_ERROR);
            }
        });
    }
    /**
     * Method to change operation status and ui in app on recieving abort status
     */
    private void operationHasAborted() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        ConnectionController.operationHasAborted(new RfidListeners() {
            @Override
            public void onSuccess(Object object) {
                if (mIsInventoryRunning) {
                    if (isInventoryAborted) {
                        mIsInventoryRunning = false;
                        isInventoryAborted = true; //false
                        isTriggerRepeat = null;
                        if (Inventorytimer.getInstance().isTimerRunning())
                            Inventorytimer.getInstance().stopTimer();
                        if (fragment instanceof InventoryFragment)
                            ((InventoryFragment) fragment).resetInventoryDetail();
                        else if (fragment instanceof RapidReadFragment)
                            ((RapidReadFragment) fragment).resetInventoryDetail();
                        //export Data to the file
                        if (EXPORT_DATA)
                            if (tagsReadInventory != null && !tagsReadInventory.isEmpty()) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                    exportData();
                                } else {
                                    checkForExportPermission(REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            }
                    }
                } else if (isLocatingTag) {
                    if (isLocationingAborted) {
                        isLocatingTag = false;
                        isLocationingAborted = false;
                        if (fragment instanceof SingleTagLocateFragment)
                            ((SingleTagLocateFragment) fragment).resetLocationingDetails(false);

                    }
                } else if (mIsMultiTagLocatingRunning) {
                    if (isMultiTagLocationingAborted) {
                        Application.mIsMultiTagLocatingRunning = false;
                        isMultiTagLocationingAborted = false;
                    }
                }
            }
            @Override
            public void onFailure(Exception exception) {
            }

            @Override
            public void onFailure(String message) {
            }
        });


    }

    void exportData() {
        if (mConnectedReader != null) {
            new DataExportTask(getApplicationContext(), tagsReadInventory, mConnectedReader.getHostName(), TOTAL_TAGS, UNIQUE_TAGS, mRRStartedTime).execute();

        }
    }

    public void LoadTagListCSV() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            MatchModeFileLoader.getInstance(this).LoadMatchModeCSV();
        } else {
            checkForExportPermission(REQUEST_CODE_ASK_PERMISSIONS_CSV);
        }
    }

    void checkForExportPermission(final int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                switch (code) {
                    case REQUEST_CODE_ASK_PERMISSIONS:
                        exportData();
                        break;
                    case REQUEST_CODE_ASK_PERMISSIONS_CSV:
                        MatchModeFileLoader.getInstance(this).LoadMatchModeCSV();
                        break;
                }
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //Toast.makeText(this,"Write to external storage permission needed to export inventory.",Toast.LENGTH_LONG).show();
                    showMessageOKCancel("Write to external storage permission needed to export the inventory.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            code);
                                }
                            });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, code);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportData();
            }
        } else if (requestCode == REQUEST_CODE_ASK_PERMISSIONS_CSV) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MatchModeFileLoader.getInstance(this).LoadMatchModeCSV();
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     * method to set DPO status on Action bar
     *
     * @param level
     */
    public void setActionBarBatteryStatus(final int level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (menu != null && menu.findItem(R.id.action_dpo) != null) {
                    if (dynamicPowerSettings != null && dynamicPowerSettings.getValue() == 1) {
                        menu.findItem(R.id.action_dpo).setIcon(R.drawable.action_battery_dpo_level);
                    } else {
                        menu.findItem(R.id.action_dpo).setIcon(R.drawable.action_battery_level);
                    }
                    menu.findItem(R.id.action_dpo).getIcon().setLevel(level);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.common_menu, menu);
        this.menu = menu;
        if (BatteryData != null)
            setActionBarBatteryStatus(BatteryData.getLevel());
        return true;
    }
    /**
     * method lear inventory data like total tags, unique tags, read rate etc..
     */
    /**
     * RR button in {@link com.zebra.rfidreader.demo.rapidread.RapidReadFragment} is clicked
     *
     * @param view - Button clicked
     */
    public void rrClicked(View view) {
        selectNavigationMenuItem(0);
        selectItem(1);
    }

    /**
     * Inventory button in {@link com.zebra.rfidreader.demo.inventory.InventoryFragment} is clicked
     *
     * @param view - Button clicked
     */
    public void invClicked(View view) {
        selectNavigationMenuItem(1);
        selectItem(2);
    }

    /**
     * Locationing button in {@link com.zebra.rfidreader.demo.locate_tag.SingleTagLocateFragment} is clicked
     *
     * @param view - Button clicked
     */
    public void locateClicked(View view) {
        selectNavigationMenuItem(2);
        selectItem(3);
    }

    /**
     * Settings button in {@link com.zebra.rfidreader.demo.settings.SettingListFragment} is clicked
     *
     * @param view - Button clicked
     */
    public void settClicked(View view) {
        selectNavigationMenuItem(5);
        selectItem(4);
    }

    /**
     * Access button in {@link com.zebra.rfidreader.demo.access_operations.AccessOperationsFragment} is clicked
     *
     * @param view - Button clicked
     */
    public void accessClicked(View view) {
        selectNavigationMenuItem(3);
        selectItem(5);
    }

    /**
     * Filter button in {@link com.zebra.rfidreader.demo.settings.PreFilterFragment} is clicked
     *
     * @param view - Button clicked
     */
    public void filterClicked(View view) {
        selectNavigationMenuItem(6);
        selectItem(6);
    }

    /**
     * About option in {@link com.zebra.rfidreader.demo.home.AboutFragment} is selected
     */
    public void aboutClicked() {
        selectNavigationMenuItem(7);
        selectItem(8);
    }

    private void readerReconnected(ReaderDevice readerDevice) {
        // store app reader
        mConnectedDevice = readerDevice;
        mConnectedReader = readerDevice.getRFIDReader();
        if (isBatchModeInventoryRunning != null &&
                isBatchModeInventoryRunning) {
            getInstance().clearInventoryData();
            mIsInventoryRunning = true;
            memoryBankId = 0;
            getInstance().startTimer();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
            if (fragment instanceof ResponseHandlerInterfaces.BatchModeEventHandler) {
                ((ResponseHandlerInterfaces.BatchModeEventHandler) fragment).batchModeEventReceived();
            }
        } else
            try {
                getInstance().updateReaderConnection(false);
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        ReaderDeviceConnected(readerDevice);
    }

    /**
     * Method to notify device disconnection
     *
     * @param readerDevice
     */
    private void readerDisconnected(ReaderDevice readerDevice) {
        getInstance().stopTimer();
        //updateConnectedDeviceDetails(readerDevice, false);
        if (NOTIFY_READER_CONNECTION)
            sendNotification(Constants.ACTION_READER_DISCONNECTED, "Disconnected from " + readerDevice.getName());
        getInstance().clearSettings();
        setActionBarBatteryStatus(0);
        ReaderDeviceDisConnected(readerDevice);
        mConnectedDevice = null;
        mConnectedReader = null;
        is_disconnection_requested = false;
    }

    public void inventoryAborted() {
        Inventorytimer.getInstance().stopTimer();
        mIsInventoryRunning = false;
    }

    public void ReaderDeviceConnected(ReaderDevice device) {

        if (!Application.isReaderConnectedThroughBluetooth || BluetoothHandler.isDevicePaired(device.getName())) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
            if (fragment instanceof ReadersListFragment) {
                ((ReadersListFragment) fragment).ReaderDeviceConnected(device);
            } else if (fragment instanceof AboutFragment) {
                ((AboutFragment) fragment).deviceConnected();
            } else if (fragment instanceof AdvancedOptionItemFragment) {
                ((AdvancedOptionItemFragment) fragment).settingsListUpdated();
            }
//        else if(fragment instanceof AccessOperationsFragment)
//            ((AccessOperationsFragment) fragment).deviceConnected(device);
            if (readerDeviceFoundHandlers != null && readerDeviceFoundHandlers.size() > 0) {
                for (ReaderDeviceFoundHandler readerDeviceFoundHandler : readerDeviceFoundHandlers)
                    readerDeviceFoundHandler.ReaderDeviceConnected(device);
            }
            if (NOTIFY_READER_CONNECTION)
                sendNotification(Constants.ACTION_READER_CONNECTED, "Connected to " + device.getName());
        } else {

            try {
                mConnectedReader.disconnect();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }

            clearConnectedReader();

        }
    }

    public void ReaderDeviceDisConnected(ReaderDevice device) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (mIsInventoryRunning) {
            inventoryAborted();
            //export Data to the file if inventory is running in batch mode
            if (isBatchModeInventoryRunning != null && !isBatchModeInventoryRunning)
                if (EXPORT_DATA) {
                    if (tagsReadInventory != null && !tagsReadInventory.isEmpty()) {
                        new DataExportTask(getApplicationContext(), tagsReadInventory, device.getName(), TOTAL_TAGS, UNIQUE_TAGS, mRRStartedTime).execute();
                    }
                }
            isBatchModeInventoryRunning = false;
        }
        if (isLocatingTag) {
            isLocatingTag = false;
        }
        //update dpo icon in settings list
        AdvancedOptionsContent.ITEMS.get(DPO_ITEM_INDEX).icon = R.drawable.title_dpo_disabled;
        isAccessCriteriaRead = false;
        accessTagCount = 0;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).readerDisconnected(device, false);
            ((ReadersListFragment) fragment).ReaderDeviceDisConnected(device);
        } else if (fragment instanceof SingleTagLocateFragment) {
            ((SingleTagLocateFragment) fragment).resetLocationingDetails(true);
        } /*else if (fragment instanceof InventoryFragment) {
            ((InventoryFragment) fragment).resetInventoryDetail();
        } else if (fragment instanceof RapidReadFragment) {
            ((RapidReadFragment) fragment).resetInventoryDetail();
        }*/ else if (fragment instanceof AboutFragment) {
            ((AboutFragment) fragment).resetVersionDetail();
        } else if (fragment instanceof AdvancedOptionItemFragment) {
            ((AdvancedOptionItemFragment) fragment).settingsListUpdated();
        }
        if (readerDeviceFoundHandlers != null && readerDeviceFoundHandlers.size() > 0) {
            for (ReaderDeviceFoundHandler readerDeviceFoundHandler : readerDeviceFoundHandlers)
                readerDeviceFoundHandler.ReaderDeviceDisConnected(device);
        }
        if (mConnectedReader != null && !AUTO_RECONNECT_READERS) {
            try {
                mConnectedReader.disconnect();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
            mConnectedReader = null;
        }
    }

    @Override
    public void RFIDReaderAppeared(ReaderDevice device) {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment)
            ((ReadersListFragment) fragment).RFIDReaderAppeared(device);
        if (NOTIFY_READER_AVAILABLE) {
            if (!device.getName().equalsIgnoreCase("null"))
                sendNotification(Constants.ACTION_READER_AVAILABLE, device.getName() + " is available.");
        }
        if (AUTO_RECONNECT_READERS && LAST_CONNECTED_READER != null && LAST_CONNECTED_READER.length() != 0 &&
                (mConnectedDevice == null || !mConnectedDevice.getRFIDReader().isConnected()))
            AutoConnectDevice();
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice device) {
        if (RFIDController.autoConnectDeviceTask != null) {
            RFIDController.autoConnectDeviceTask.cancel(true);
        }
        mReaderDisappeared = device;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment)
            ((ReadersListFragment) fragment).RFIDReaderDisappeared(device);
        if (NOTIFY_READER_AVAILABLE)
            sendNotification(Constants.ACTION_READER_AVAILABLE, device.getName() + " is unavailable.");
    }

    /**
     * Method which will called once notification received from reader.
     * update the operation status in the application based on notification type
     *
     * @param rfidStatusEvents - notification received from reader
     */
    private void notificationFromGenericReader(RfidStatusEvents rfidStatusEvents) {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
            if (mConnectedReader != null)
                DisconnectTask = new UpdateDisconnectedStatusTask(mConnectedReader.getHostName()).execute();
//            RFIDController.mConnectedReader = null;
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.INVENTORY_START_EVENT) {
            if (!isAccessCriteriaRead && !isLocatingTag && !Application.mIsMultiTagLocatingRunning) {
                //if (!getRepeatTriggers() && Inventorytimer.getInstance().isTimerRunning()) {
                mIsInventoryRunning = true;
                Inventorytimer.getInstance().startTimer();
                //}
            }
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            //tagListMatchNotice = false;
            //TODO: revisit why to clear here
            //accessTagCount = 0;
            //RFIDController.isAccessCriteriaRead = false;
            if (mIsInventoryRunning) {
                Inventorytimer.getInstance().stopTimer();
            } else if (isGettingTags) {
                isGettingTags = false;
                if (mConnectedReader != null)
                    mConnectedReader.Actions.purgeTags();
                if (EXPORT_DATA) {
                    if (TAG_LIST_MATCH_MODE) {
                        if (tagsReadInventory != null && !tagsReadInventory.isEmpty() && fragment instanceof InventoryFragment) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new DataExportTask(getApplicationContext(), ((InventoryFragment) fragment).getAdapter().searchItemsList, mConnectedDevice.getName(), TOTAL_TAGS, UNIQUE_TAGS, mRRStartedTime).execute();
                                }
                            });
                        } else if (tagsReadInventory != null && !tagsReadInventory.isEmpty() && fragment instanceof RapidReadFragment && UNIQUE_TAGS != 0) {
                            currentFragment = "RapidReadFragment";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new DataExportTask(getApplicationContext(), tagsReadInventory, mConnectedDevice.getName(), TOTAL_TAGS, UNIQUE_TAGS, mRRStartedTime).execute();
                                }
                            });
                        }
                    } else if (tagsReadInventory != null && !tagsReadInventory.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new DataExportTask(getApplicationContext(), tagsReadInventory, mConnectedDevice.getName(), TOTAL_TAGS, UNIQUE_TAGS, mRRStartedTime).execute();
                            }
                        });
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (fragment instanceof ReadersListFragment) {
                            //((ReadersListFragment) fragment).cancelProgressDialog();
                            if (mConnectedReader != null && mConnectedReader.ReaderCapabilities.getModelName() != null) {
                                ((ReadersListFragment) fragment).capabilitiesRecievedforDevice();
                            }
                        }
                    }
                });
            }
            if (!getInstance().getRepeatTriggers()) {
                if (mIsInventoryRunning)
                    isInventoryAborted = true;
                else if (isLocatingTag)
                    isLocationingAborted = true;
                operationHasAborted();
            }
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.OPERATION_END_SUMMARY_EVENT) {
            if (fragment instanceof RapidReadFragment)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((RapidReadFragment) fragment).updateInventoryDetails();
                    }
                });
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT && isActivityVisible()) {
            Boolean triggerPressed = false;
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED)
                triggerPressed = true;
            Log.d(TAG, "notificationFromGenericReader " + fragment + " screen " + m_ScreenOn);
            if (m_ScreenOn) {
                if (triggerPressed && isTriggerImmediateorRepeat(triggerPressed) && fragment instanceof TriggerEventHandler) {
                    ((TriggerEventHandler) fragment).triggerPressEventRecieved();
                } else if (!triggerPressed && isTriggerImmediateorRepeat(triggerPressed) && fragment instanceof TriggerEventHandler) {
                    ((TriggerEventHandler) fragment).triggerReleaseEventRecieved();
                    //tagListMatchNotice = false;
                }
            }
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.BATTERY_EVENT) {
            final Events.BatteryData batteryData = rfidStatusEvents.StatusEventData.BatteryData;
            BatteryData = batteryData;
            setActionBarBatteryStatus(batteryData.getLevel());
            if (batteryNotificationHandlers != null && batteryNotificationHandlers.size() > 0) {
                for (BatteryNotificationHandler batteryNotificationHandler : batteryNotificationHandlers)
                    batteryNotificationHandler.deviceStatusReceived(batteryData.getLevel(), batteryData.getCharging(), batteryData.getCause());
            }
            if (NOTIFY_BATTERY_STATUS && batteryData.getCause() != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (batteryData.getCause().trim().equalsIgnoreCase(Constants.MESSAGE_BATTERY_CRITICAL))
                            sendNotification(com.zebra.rfidreader.demo.common.Constants.ACTION_READER_BATTERY_CRITICAL, getString(R.string.battery_status__critical_message));
                        else if (batteryData.getCause().trim().equalsIgnoreCase(Constants.MESSAGE_BATTERY_LOW))
                            sendNotification(com.zebra.rfidreader.demo.common.Constants.ACTION_READER_BATTERY_CRITICAL, getString(R.string.battery_status_low_message));
                    }
                });
            }
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.BATCH_MODE_EVENT) {
            isBatchModeInventoryRunning = true;
            getInstance().startTimer();
            getInstance().clearInventoryData();
            mIsInventoryRunning = true;
            memoryBankId = 0;
            PrepareMatchModeList();
            isTriggerRepeat = rfidStatusEvents.StatusEventData.BatchModeEventData.get_RepeatTrigger();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (fragment instanceof ResponseHandlerInterfaces.BatchModeEventHandler) {
                        ((ResponseHandlerInterfaces.BatchModeEventHandler) fragment).batchModeEventReceived();
                    }
                    if (fragment instanceof ReadersListFragment) {
                        if (mConnectedReader != null && mConnectedReader.ReaderCapabilities.getModelName() == null) {
                            ((ReadersListFragment) fragment).capabilitiesRecievedforDevice();
                        }
                    }
                }
            });
        }
    }

    /*
     *method to check if both start and stop trigger is IMMEDIATE or repeat trigger
     */
    public Boolean isTriggerImmediateorRepeat(Boolean trigPress) {
        if (trigPress && settings_startTrigger.getTriggerType().toString().equalsIgnoreCase(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE.toString())
                && (!settings_stopTrigger.getTriggerType().toString().equalsIgnoreCase(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT.toString()))
        ) {
            return true;
        } else if (!trigPress && !settings_startTrigger.getTriggerType().toString().equalsIgnoreCase(START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD.toString())
                && (settings_stopTrigger.getTriggerType().toString().equalsIgnoreCase(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE.toString()))
        ) {
            return true;
        } else
            return false;
    }

    /**
     * method to send connect command request to reader
     * after connect button clicked on connect password pairTaskDailog
     *
     * @param password     - reader password
     * @param readerDevice
     */
    public void connectClicked(String password, ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).ConnectwithPassword(password, readerDevice);
        }
    }

    /**
     * method which will exe cute after cancel button clicked on connect pwd pairTaskDailog
     *
     * @param readerDevice
     */
    public void cancelClicked(ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).readerDisconnected(readerDevice, true);
        }
    }

    public void startbeepingTimer() {
        if (beeperVolume != BEEPER_VOLUME.QUIET_BEEP) {
            if (!beepON) {
                beepON = true;
                beep();
                if (tbeep == null) {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            stopbeepingTimer();
                            beepON = false;
                        }
                    };
                    tbeep = new Timer();
                    tbeep.schedule(task, 10);
                }
            }
        }
    }

    /**
     * method to stop timer
     */
    public void stopbeepingTimer() {
        if (tbeep != null && toneGenerator != null) {
            toneGenerator.stopTone();
            tbeep.cancel();
            tbeep.purge();
        }
        tbeep = null;
    }

    public void beep() {
        if (toneGenerator != null) {
            int toneType = ToneGenerator.TONE_PROP_BEEP;
            toneGenerator.startTone(toneType);
        }
    }

    public void startlocatebeepingTimer(int proximity) {
        if (beeperVolume != BEEPER_VOLUME.QUIET_BEEP) {
            int POLLING_INTERVAL1 = BEEP_DELAY_TIME_MIN + (((BEEP_DELAY_TIME_MAX - BEEP_DELAY_TIME_MIN) * (100 - proximity)) / 100);
            if (!beepONLocate) {
                beepONLocate = true;
                beep();
                if (locatebeep == null) {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            stoplocatebeepingTimer();
                            beepONLocate = false;
                        }
                    };
                    locatebeep = new Timer();
                    locatebeep.schedule(task, POLLING_INTERVAL1, 10);
                }
            }
        }
    }

    /**
     * method to stop timer locate beep
     */
    public void stoplocatebeepingTimer() {
        if (locatebeep != null && toneGenerator != null) {
            toneGenerator.stopTone();
            locatebeep.cancel();
            locatebeep.purge();
        }
        locatebeep = null;
    }

    public class EventHandler implements RfidEventsListener {

        @Override
        public void eventReadNotify(RfidReadEvents e) {
            if (mConnectedReader != null) {
                if(!mConnectedReader.Actions.MultiTagLocate.isMultiTagLocatePerforming()) {
                    final TagData[] myTags = mConnectedReader.Actions.getReadTags(100);
                    if (myTags != null) {
                        //Log.d("RFID_EVENT","l: "+myTags.length);
                        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                        for (int index = 0; index < myTags.length; index++) {
                            if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                                    myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                            }
                            if (myTags[index].isContainsLocationInfo()) {
                                final int tag = index;
                                TagProximityPercent = myTags[tag].LocationInfo.getRelativeDistance();
                                if (TagProximityPercent > 0) {
                                    startlocatebeepingTimer(TagProximityPercent);
                                }
                                if (fragment instanceof LocateOperationsFragment)
                                    ((LocateOperationsFragment) fragment).handleLocateTagResponse();
                            } else {
                                if (isAccessCriteriaRead && !mIsInventoryRunning) {
                                    accessTagCount++;
                                } else {
                                    if (myTags[index] != null && (myTags[index].getOpStatus() == null || myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)) {
                                        final int tag = index;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (TAG_LIST_MATCH_MODE)
                                                    new MatchingTagsResponseHandlerTask(myTags[tag], fragment).execute();
                                                else
                                                    new ResponseHandlerTask(myTags[tag], fragment).execute();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                } else { ////multi-tal locationing results
                    final TagData[] myTags = mConnectedReader.Actions.getMultiTagLocateTagInfo(100);
                    if (myTags != null) {
                        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                        for (int index = 0; index < myTags.length; index++) {
                            TagData tagData = myTags[index];
                            if (tagData.isContainsMultiTagLocateInfo()) {
                                new MultiTagLocateResponseHandlerTask(mContext, tagData, fragment).execute();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            notificationFromGenericReader(rfidStatusEvents);
        }
    }

    /**
     * Async Task, which will handle tag data response from reader. This task is used to check whether tag is in inventory list or not.
     * If tag is not in the list then it will add the tag data to inventory list. If tag is there in inventory list then it will update the tag details in inventory list.
     */
    public class ResponseHandlerTask extends AsyncTask<Void, Void, Boolean> {
        private TagData tagData;
        private InventoryListItem inventoryItem;
        private InventoryListItem oldObject;
        private Fragment fragment;
        private String memoryBank;
        private String memoryBankData;

        ResponseHandlerTask(TagData tagData, Fragment fragment) {
            this.tagData = tagData;
            this.fragment = fragment;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean added = false;
            try {
                if (inventoryList.containsKey(tagData.getTagID())) {
                    inventoryItem = new InventoryListItem(tagData.getTagID(), 1, null, null, null, null, null, null);
                    int index = inventoryList.get(tagData.getTagID());
                    if (index >= 0) {
                        //Tag is already present. Update the fields and increment the count
                        if (tagData.getOpCode() != null)
                            if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                memoryBank = tagData.getMemoryBank().toString();
                                memoryBankData = tagData.getMemoryBankData().toString();
                            }
                        oldObject = tagsReadInventory.get(index);
                        int tagSeenCount = 0;
                        if (Integer.toString(tagData.getTagSeenCount()) != null)
                            tagSeenCount = tagData.getTagSeenCount();
                        if (tagSeenCount != 0) {
                            TOTAL_TAGS += tagSeenCount;
                            oldObject.incrementCountWithTagSeenCount(tagSeenCount);
                        } else {
                            TOTAL_TAGS++;
                            oldObject.incrementCount();
                        }
                        if (oldObject.getMemoryBankData() != null && !oldObject.getMemoryBankData().equalsIgnoreCase(memoryBankData))
                            oldObject.setMemoryBankData(memoryBankData);
                        if (pc)
                            oldObject.setPC(Integer.toHexString(tagData.getPC()));
                        if (phase)
                            oldObject.setPhase(Integer.toString(tagData.getPhase()));
                        if (channelIndex)
                            oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                        if (rssi)
                            oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                        if (brandidcheckenabled) {
                            if (tagData.getBrandIDStatus()) {
                                //oldObject.brandIDfound = true;
                                oldObject.setBrandIDStatus(true);
                                bFound = true;
                                //Log.i("MainActivity", "getBrandIDStatus" + oldObject.getBrandIDStatus());
                            } else {
                                oldObject.setBrandIDStatus(false);
                            }
                        }
                    }
                } else {
                    //Tag is encountered for the first time. Add it.
                    if (inventoryMode == 0 || (inventoryMode == 1 && UNIQUE_TAGS <= Constants.UNIQUE_TAG_LIMIT)) {
                        int tagSeenCount = 0;
                        if (Integer.toString(tagData.getTagSeenCount()) != null)
                            tagSeenCount = tagData.getTagSeenCount();
                        if (tagSeenCount != 0) {
                            TOTAL_TAGS += tagSeenCount;
                            inventoryItem = new InventoryListItem(tagData.getTagID(), tagSeenCount, null, null, null, null, null, null);
                        } else {
                            TOTAL_TAGS++;
                            inventoryItem = new InventoryListItem(tagData.getTagID(), 1, null, null, null, null, null, null);
                        }
                        added = tagsReadInventory.add(inventoryItem);
                        if (added) {
                            inventoryList.put(tagData.getTagID(), UNIQUE_TAGS);
                            if (tagData.getOpCode() != null)
                                if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                    memoryBank = tagData.getMemoryBank().toString();
                                    memoryBankData = tagData.getMemoryBankData().toString();

                                }
                            oldObject = tagsReadInventory.get(UNIQUE_TAGS);
                            oldObject.setMemoryBankData(memoryBankData);
                            oldObject.setMemoryBank(memoryBank);
                            if (pc)
                                oldObject.setPC(Integer.toHexString(tagData.getPC()));
                            if (phase)
                                oldObject.setPhase(Integer.toString(tagData.getPhase()));
                            if (channelIndex)
                                oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                            if (rssi)
                                oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                            UNIQUE_TAGS++;
                            if (brandidcheckenabled) {
                                if (tagData.getBrandIDStatus()) {
                                    //oldObject.brandIDfound = true;
                                    oldObject.setBrandIDStatus(true);
                                    bFound = true;
                                    //Log.i("MainActivity", "getBrandIDStatus" + oldObject.getBrandIDStatus());
                                } else {
                                    oldObject.setBrandIDStatus(false);
                                }
                            }
                        }
                    }
                }
                // beep on each tag read
                startbeepingTimer();
            } catch (IndexOutOfBoundsException e) {
                //logAsMessage(TYPE_ERROR, TAG, e.getMessage());
                oldObject = null;
                added = false;
            } catch (Exception e) {
                // logAsMessage(TYPE_ERROR, TAG, e.getMessage());
                oldObject = null;
                added = false;
            }
            inventoryItem = null;
            memoryBank = null;
            memoryBankData = null;
            return added;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            cancel(true);
            if (oldObject != null && fragment instanceof ResponseHandlerInterfaces.ResponseTagHandler)
                ((ResponseHandlerInterfaces.ResponseTagHandler) fragment).handleTagResponse(oldObject, result);
            oldObject = null;
        }
    }

    //TextView tv_alert_retry_count;
    CustomProgressDialog retryCountDialog = null;
    boolean isCancelPressed = false;

    private void displayRetryCountDialog(final Context context) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (!(fragment instanceof ReadersListFragment) && context != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isCancelPressed = false;
                    retryCountDialog = new CustomProgressDialog(context,
                            "Connecting to " + LAST_CONNECTED_READER + "\n" +
                                    "Retry Count : 1");
                    try {
                        if (!isFinishing())
                            retryCountDialog.show();
                    } catch (WindowManager.BadTokenException ex) {
                        ex.printStackTrace();
                    }
                }
            });

        }
    }

    int retryCount;
    private Context mContext = this;

    protected class UpdateDisconnectedStatusTask extends AsyncTask<Void, Void, Boolean> {
        private final String device;
        // store current reader state
        private final ReaderDevice readerDevice;
        long disconnectedTime;
        boolean bConnected = false;

        public UpdateDisconnectedStatusTask(String device) {
            this.device = device;
            disconnectedTime = System.currentTimeMillis();
            // store current reader state
            readerDevice = mConnectedDevice;
            //
            mReaderDisappeared = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (readerDevice != null && readerDevice.getName().equalsIgnoreCase(device)) {
                        readerDisconnected(readerDevice);
                    } else {
                        readerDisconnected(new ReaderDevice(device, null));
                    }
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (!is_disconnection_requested && AUTO_RECONNECT_READERS &&
                    readerDevice != null && device != null && device.equalsIgnoreCase(readerDevice.getName())
                    && readerDevice.getName().startsWith("RFD8500")) {
                if (isBluetoothEnabled()) {
                    retryCount = 0;
                    if (!Application.isReaderConnectedThroughBluetooth || BluetoothHandler.isDevicePaired(readerDevice.getName())) {
                        displayRetryCountDialog(isActivityVisible() ? mContext : Application.contextSettingDetails);
                        while (!bConnected && retryCount < 10) {
                            if (isCancelled())
                                break;
                            try {
                                Thread.sleep(1000);
                                retryCount++;
                                if (!isCancelPressed && retryCountDialog != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            retryCountDialog.setMessage("Connecting to " + LAST_CONNECTED_READER + "\n" +
                                                    "Retry Count : " + retryCount);
                                            if (!retryCountDialog.isShowing()) {
                                                try {
                                                    if (!isFinishing())
                                                        retryCountDialog.show();
                                                } catch (WindowManager.BadTokenException ex) {
                                                    ex.printStackTrace();
                                                }
                                            }

                                        }
                                    });
                                }
                                if (is_connection_requested || isCancelled())
                                    break;

                                readerDevice.getRFIDReader().reconnect();
                                bConnected = true;
                                if (mReaderDisappeared != null && mReaderDisappeared.getName().equalsIgnoreCase(readerDevice.getName())) {
                                    readerDevice.getRFIDReader().disconnect();
                                    bConnected = false;
                                    break;
                                }

                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                if (e.getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                                    isBatchModeInventoryRunning = true;
                                    bConnected = true;
                                } else if (e.getResults() == RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {

                                    try {
                                        readerDevice.getRFIDReader().disconnect();
                                        bConnected = false;
                                        break;
                                    } catch (InvalidUsageException e1) {
                                        e1.printStackTrace();
                                    } catch (OperationFailureException e1) {
                                        e1.printStackTrace();
                                    }
                                } else {

                                    if (!BluetoothHandler.isDevicePaired(readerDevice.getName()) && Application.isReaderConnectedThroughBluetooth) {
                                        break;
                                    }
                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (retryCountDialog != null && retryCountDialog.isShowing())
                        retryCountDialog.dismiss();
                }
                return bConnected;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!isCancelled()) {
                if (result) {
                    if (readerDevice.getName().startsWith("RFD8500")) {

                        readerReconnected(readerDevice);
                        StoreConnectedReader();
                    }
                } else if (!is_connection_requested) {
                    //sendNotification(Constants.ACTION_READER_CONN_FAILED, "Connection Failed!! was received");
                    try {
                        if (readerDevice != null)
                            readerDevice.getRFIDReader().disconnect();
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(TAG, "onCancelled disconnect" + readerDevice);
            try {
                if (readerDevice != null)
                    readerDevice.getRFIDReader().disconnect();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    void StoreConnectedReader() {
        if (AUTO_RECONNECT_READERS && RFIDController.mConnectedReader != null) {
            LAST_CONNECTED_READER = mConnectedReader.getHostName();
            SharedPreferences settings = getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Constants.LAST_READER, LAST_CONNECTED_READER);
            editor.commit();
        }
    }

    void clearConnectedReader() {
        SharedPreferences settings = getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.LAST_READER, "");
        editor.commit();
        LAST_CONNECTED_READER = "";
        RFIDController.mConnectedDevice = null;
    }

    private boolean m_ScreenOn = true;
    // Broadcast receiver to receive the scanner_status, and disable the scanner
    public BroadcastReceiver BroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SCREEN_OFF:
                    //Log.d(TAG, "BroadcastReceiver: " + action + " " + RFIDController.mConnectedReader.getHostName());
                    if (!mIsInventoryRunning)
                        m_ScreenOn = false;
                    break;
                case ACTION_SCREEN_ON:
                    //Log.d(TAG, "BroadcastReceiver: " + action + " " + RFIDController.mConnectedReader.getHostName());
                    m_ScreenOn = true;
                    break;
                case scanner_status:
                    //Log.d(TAG, intent.getExtras().getString("STATUS"));
                    break;
                case DW_APIRESULT_ACTION: {
                    String command = intent.getStringExtra("COMMAND");
                    String commandidentifier = intent.getStringExtra("COMMAND_IDENTIFIER");
                    String result = intent.getStringExtra("RESULT");
                    if (command != null && command.equals("com.symbol.datawedge.api.SET_CONFIG")) {
                        if (commandidentifier.equals(Application.RFID_DATAWEDGE_PROFILE_CREATION)) {
                            Bundle bundle = new Bundle();
                            String resultInfo = "";
                            if (intent.hasExtra("RESULT_INFO")) {
                                bundle = intent.getBundleExtra("RESULT_INFO");
                                resultInfo = bundle.getString("RESULT_CODE");
                            }
                            if (result.equals("SUCCESS")) {
                                disableScanner();

                            } else {
                                //   Log.d(TAG, "Failed to Disable scanner " + resultInfo);
                            }
                            Set<String> keys = bundle.keySet();
                            resultInfo = "";
                            for (String key : keys) {
                                resultInfo += key + ": " + bundle.getString(key) + "\n";
                            }
                            Log.d(TAG, "Disable scanner " + resultInfo);
                        }
                    }
                }
                break;
            }
        }
    };


    public static InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!isAllowed(source.charAt(i)))
                    return "";
            }
            return null;
        }

        String allowed = "0123456789ABCDEFabcdef";

        private boolean isAllowed(char c) {
            if (asciiMode == false) {
                for (char ch : allowed.toCharArray()) {
                    if (ch == c)
                        return true;
                }
                return false;
            }
            return true;
        }
    };

    private class MatchingTagsResponseHandlerTask extends AsyncTask<Void, Void, Boolean> {

        private TagData tagData;
        private InventoryListItem inventoryItem;
        private InventoryListItem oldObject;
        private Fragment fragment;
        private String memoryBank;
        private String memoryBankData;
        //private Toast myToast;

        MatchingTagsResponseHandlerTask(TagData tagData, Fragment fragment) {
            this.tagData = tagData;
            this.fragment = fragment;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //TODO: background task with UI thread runnable requires fix, all lists are assigned to list adapter so always run from ui thread
            runOnUiThread(new Runnable() {
                @SuppressLint("WrongConstant")
                @Override
                public void run() {
                    boolean added = false;

                    //RFIDController.isCSVtagsLoaded=true;
                    //Toast.makeText(getApplicationContext(), getResources().getString(R.string.tag_match_complete), Toast.LENGTH_SHORT).show();
                    try {
                        String tagId = tagData.getTagID();
                        if(asciiMode) {
                            tagId = hextoascii.convert(tagId);
                        }
                        if (inventoryList.containsKey(tagId)) {
                            inventoryItem = new InventoryListItem(tagId, 1, null, null, null, null, null, null);
                            int index = inventoryList.get(tagId);
                            if (index >= 0) {
                                if (tagListMap.containsKey(tagId))
                                    tagsReadInventory.get(index).setTagStatus("MATCH");
                                else
                                    tagsReadInventory.get(index).setTagStatus("UNKNOWN");
                                TOTAL_TAGS++;
                                //Tag is already present. Update the fields and increment the count
                                if (tagData.getOpCode() != null)
                                    if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                        memoryBank = tagData.getMemoryBank().toString();
                                        memoryBankData = tagData.getMemoryBankData().toString();
                                    }
                                if (memoryBankId == 1) {  //matching tags
                                    if (tagListMap.containsKey(tagId) && !matchingTagsList.contains(tagsReadInventory.get(index))) {
                                        matchingTagsList.add(tagsReadInventory.get(index));
                                        tagsReadForSearch.add(tagsReadInventory.get(index));
                                        added = true;
                                    }
                                } else if (memoryBankId == 2 && tagListMap.containsKey(tagId)) {
                                    if (missingTagsList.contains(tagsReadInventory.get(index))) {
                                        missingTagsList.remove(tagsReadInventory.get(index));
                                        tagsReadForSearch.remove(tagsReadInventory.get(index));
                                        added = true;
                                    }
                                }
                                oldObject = tagsReadInventory.get(index);
                                if (oldObject.getCount() == 0) {
                                    missedTags--;
                                    matchingTags++;
                                    UNIQUE_TAGS++;
                                }
                                oldObject.incrementCount();
                                if (oldObject.getMemoryBankData() != null && !oldObject.getMemoryBankData().equalsIgnoreCase(memoryBankData))
                                    oldObject.setMemoryBankData(memoryBankData);
                                //oldObject.setEPCId(inventoryItem.getEPCId());
                                oldObject.setPC(Integer.toString(tagData.getPC()));
                                oldObject.setPhase(Integer.toString(tagData.getPhase()));
                                oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                                if (rssi) oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                            }

                        } else {
                            //Tag is encountered for the first time. Add it.
                            if (inventoryMode == 0 || (inventoryMode == 1 && UNIQUE_TAGS_CSV <= Constants.UNIQUE_TAG_LIMIT)) {
                                int tagSeenCount = tagData.getTagSeenCount();
                                if (tagSeenCount != 0) {
                                    TOTAL_TAGS += tagSeenCount;
                                    inventoryItem = new InventoryListItem(tagId, tagSeenCount, null, null, null, null, null, null);
                                } else {
                                    TOTAL_TAGS++;
                                    inventoryItem = new InventoryListItem(tagId, 1, null, null, null, null, null, null);
                                }
                                if (tagListMap.containsKey(tagId))
                                    inventoryItem.setTagStatus("MATCH");
                                else
                                    inventoryItem.setTagStatus("UNKNOWN");
                                if (memoryBankId == 1)
                                    tagsReadInventory.add(inventoryItem);
                                else if (memoryBankId == 3) {
                                    inventoryItem.setTagDetails("unknown");
                                    added = tagsReadInventory.add(inventoryItem);
                                    unknownTagsList.add(inventoryItem);
                                    tagsReadForSearch.add(inventoryItem);
                                } else {
                                    if (inventoryItem.getTagDetails() == null) {
                                        inventoryItem.setTagDetails("unknown");
                                    }
                                    added = tagsReadInventory.add(inventoryItem);
                                    if (memoryBankId != 2)
                                        tagsReadForSearch.add(inventoryItem);
                                }
                                if (added || memoryBankId == 1) {
                                    inventoryList.put(tagId, UNIQUE_TAGS_CSV);
                                    if (tagData.getOpCode() != null)
                                        if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                            memoryBank = tagData.getMemoryBank().toString();
                                            memoryBankData = tagData.getMemoryBankData().toString();
                                        }
                                    oldObject = tagsReadInventory.get(UNIQUE_TAGS_CSV);
                                    oldObject.setMemoryBankData(memoryBankData);
                                    oldObject.setMemoryBank(memoryBank);
                                    oldObject.setPC(Integer.toString(tagData.getPC()));
                                    oldObject.setPhase(Integer.toString(tagData.getPhase()));
                                    oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                                    if (rssi) oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                                    UNIQUE_TAGS++;
                                    UNIQUE_TAGS_CSV++;
                                }
                            }
                        }
                        // Notify user when tags from tag list are read atleast once  8613
                      /*  boolean allTagsMatched = false;
                        if (RFIDController.TAG_LIST_MATCH_MODE) {
                            if (RFIDController.tagsReadInventory != null && RFIDController.tagListMap != null) {
                                int matchedCount = 0;
                                for (InventoryListItem listItem : RFIDController.tagsReadInventory) {
                                    if (RFIDController.tagListMap.containsKey(listItem.getTagID()))
                                        matchedCount++;

                                }
                                if (matchedCount == RFIDController.tagListMap.size()) {
                                    Toast.makeText(MainActivity.this, "All tags from taglist read successfully ", Toast.LENGTH_SHORT).show();
                                    inventoryStartOrStop();
                                    allTagsMatched = true;
                                }
                            }
                        }
                        //*/
                        // beep on each tag read
                        startbeepingTimer();
                    } catch (IndexOutOfBoundsException e) {
                        //logAsMessage(TYPE_ERROR, TAG, e.getMessage());
                        oldObject = null;
                        added = false;
                    } catch (Exception e) {
                        //logAsMessage(TYPE_ERROR, TAG, e.getMessage());
                        oldObject = null;
                        added = false;
                    }
                    tagData = null;
                    inventoryItem = null;
                    memoryBank = null;
                    memoryBankData = null;
                    // call notifyDataSetChanged from same runnalbe instead of onPostExecute
                    if (oldObject != null && fragment instanceof ResponseHandlerInterfaces.ResponseTagHandler)
                        ((ResponseHandlerInterfaces.ResponseTagHandler) fragment).handleTagResponse(oldObject, false);
                    if (matchingTags != 0 && missedTags == 0 && !tagListMatchNotice) {
                        tagListMatchNotice = true;

                        /*Toast mToastToShow;
                        // Set the toast and duration
                        int toastDurationInMilliSeconds = 5000;
                        mToastToShow = Toast.makeText(getApplicationContext(), R.string.tag_match_complete, Toast.LENGTH_SHORT);

                        // Set the countdown to display the toast
                        CountDownTimer toastCountDown;
                        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000) {
                            public void onTick(long millisUntilFinished) {
                                mToastToShow.show();
                            }
                            public void onFinish() {
                                mToastToShow.cancel();
                            }
                        };

                        // Show the toast and starts the countdown
                        mToastToShow.show();
                        toastCountDown.start();*/

                        Toast.makeText(getApplicationContext(), getResources().getString( R.string.tag_match_complete), Toast.LENGTH_SHORT).show();

                        if (tagListMatchAutoStop) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (inventoryBT != null) {
                                        if (mIsInventoryRunning) {
                                            inventoryBT.performClick();
                                            startbeepingTimer();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
//            if (oldObject != null && fragment instanceof ResponseHandlerInterfaces.ResponseTagHandler)
//                ((ResponseHandlerInterfaces.ResponseTagHandler) fragment).handleTagResponse(oldObject, result);
            oldObject = null;
        }

    }

    public void selectNavigationMenuItem(int pos) {
        navigationView.getMenu().getItem(pos).setChecked(true);
    }

    public static void setAccessProfile(boolean bSet) {
        RFIDController.getInstance().setAccessProfile(bSet);
    }

    /**
     * method to stop progress pairTaskDailog on timeout
     *
     * @param time
     * @param d
     * @param command
     */
    public void timerDelayRemoveDialog(long time, final Dialog d, final String command) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d != null && d.isShowing()) {
                    d.dismiss();
                    //TODO: cross check on selective flag clearing
                    if (isAccessCriteriaRead) {
                        if (accessTagCount == 0)
                            sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.err_access_op_failed));
                        isAccessCriteriaRead = false;
                    } else {
                        sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, command + " timeout");
                        if (isActivityVisible())
                            callBackPressed();
                    }
                    isAccessCriteriaRead = false;
                    accessTagCount = 0;
                }
            }
        }, time);
    }

    /**
     * Method to send the notification
     *
     * @param action - intent action
     * @param data   - notification message
     */


    public void sendNotification(String action, String data) {
        if (isActivityVisible()) {
            if (action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_LOW)) {
                new CustomToast(MainActivity.this, R.layout.toast_layout, data).show();
            } else {
                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
            }
        } else {
          /*  Intent i = new Intent(MainActivity.this, NotificationsService.class);
            i.putExtra(Constants.INTENT_ACTION, action);
            i.putExtra(Constants.INTENT_DATA, data);
            startService(i);*/
            NotificationUtil.displayNotification(this, action, data);


        }
    }


    /**
     * Method to be called from Fragments of this activity after handling the response from the reader(success / failure)
     */
    public void callBackPressed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.super.onBackPressed();
            }
        });
    }

    public void createDWProfile() {
        // MAIN BUNDLE PROPERTIES
        Bundle bMain = new Bundle();
        bMain.putString("PROFILE_NAME", "RFIDMobileApp");
        bMain.putString("PROFILE_ENABLED", "true");              // <- that will be enabled
        bMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");   // <- or created if necessary.
        // PLUGIN_CONFIG BUNDLE PROPERTIES
        Bundle scanBundle = new Bundle();
        scanBundle.putString("PLUGIN_NAME", "BARCODE"); // barcode plugin
        scanBundle.putString("RESET_CONFIG", "true");
        // PARAM_LIST BUNDLE PROPERTIES
        Bundle scanParams = new Bundle();
        scanParams.putString("scanner_selection", "auto");
        scanParams.putString("scanner_input_enabled", "true"); // Mainly disable scanner plugin
        // NEST THE BUNDLE "bParams" WITHIN THE BUNDLE "bConfig"
        scanBundle.putBundle("PARAM_LIST", scanParams);

        Bundle keystrokeBundle = new Bundle();
        keystrokeBundle.putString("PLUGIN_NAME", "KEYSTROKE");
        Bundle keyStrokeParams = new Bundle();
        keyStrokeParams.putString("keystroke_output_enabled", "false");
        keyStrokeParams.putString("keystroke_action_char", "9"); // 0, 9 , 10, 13
        keyStrokeParams.putString("keystroke_delay_extended_ascii", "500");
        keyStrokeParams.putString("keystroke_delay_control_chars", "800");
        keystrokeBundle.putBundle("PARAM_LIST", keyStrokeParams);

        Bundle bConfigIntent = new Bundle();
        Bundle bParamsIntent = new Bundle();
        bParamsIntent.putString("intent_output_enabled", "true");
        bParamsIntent.putString("intent_action", "com.symbol.dwudiusertokens.udi");
        bParamsIntent.putString("intent_category", "zebra.intent.dwudiusertokens.UDI");
        bParamsIntent.putInt("intent_delivery", 2); //Use "0" for Start Activity, "1" for Start Service, "2" for Broadcast, "3" for start foreground service
        bConfigIntent.putString("PLUGIN_NAME", "INTENT");
        bConfigIntent.putString("RESET_CONFIG", "true");
        bConfigIntent.putBundle("PARAM_LIST", bParamsIntent);


        // THEN NEST THE "bConfig" BUNDLE WITHIN THE MAIN BUNDLE "bMain"
        ArrayList<Bundle> bundleArrayList = new ArrayList<>();
        bundleArrayList.add(scanBundle);
        bundleArrayList.add(keystrokeBundle);
        bundleArrayList.add(bConfigIntent);
        // following requires arrayList
        bMain.putParcelableArrayList("PLUGIN_CONFIG", bundleArrayList);
        // CREATE APP_LIST BUNDLES (apps and/or activities to be associated with the Profile)
        Bundle ActivityList = new Bundle();
        ActivityList.putString("PACKAGE_NAME", getPackageName());      // Associate the profile with this app
        ActivityList.putStringArray("ACTIVITY_LIST", new String[]{"*"});

        // NEXT APP_LIST BUNDLE(S) INTO THE MAIN BUNDLE
        bMain.putParcelableArray("APP_LIST", new Bundle[]{
                ActivityList
        });
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain);
        i.putExtra("SEND_RESULT", "true");
        i.putExtra("COMMAND_IDENTIFIER", Application.RFID_DATAWEDGE_PROFILE_CREATION);
        sendBroadcast(i);
    }

    public void disableScanner() {
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "DISABLE_PLUGIN");
        i.putExtra("SEND_RESULT", "false");
        i.putExtra("COMMAND_IDENTIFIER", Application.RFID_DATAWEDGE_DISABLE_SCANNER);  //Unique identifier
        sendBroadcast(i);
    }
}
