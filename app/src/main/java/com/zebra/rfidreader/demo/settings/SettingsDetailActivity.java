package com.zebra.rfidreader.demo.settings;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.CustomProgressDialog;
import com.zebra.rfidreader.demo.common.CustomToast;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.notifications.NotificationUtil;
import com.zebra.rfidreader.demo.reader_connection.PasswordDialog;
import com.zebra.rfidreader.demo.reader_connection.ReadersListFragment;
import com.zebra.rfidreader.demo.rfid.RFIDController;

/**
 * Class to handle the UI for setting details like antenna config, singulation etc..
 * Hosts a fragment for UI.
 */
public class SettingsDetailActivity extends AppCompatActivity implements
        ResponseHandlerInterfaces.ReaderDeviceFoundHandler,
        Readers.RFIDReaderEventHandler,
        ResponseHandlerInterfaces.BatteryNotificationHandler,
        AdvancedOptionItemFragment.OnAdvancedListFragmentInteractionListener {
    //Tag to identify the currently displayed fragment
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    protected static final String TAG_CONTENT_FRAGMENTa = "ContentFragmenta";
    protected CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_detail);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.contextSettingDetails = this;

        MainActivity.addReaderDeviceFoundHandler(this);
        MainActivity.addBatteryNotificationHandler(this);
        if (RFIDController.readers == null) {
            RFIDController.readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }
        // attach to reader list handler
        RFIDController.readers.attach(this);

        if (savedInstanceState != null) {
            return;
        } else {
            startFragment(getIntent());
        }

    }


    /**
     * start the fragment based on intent data
     *
     * @param intent received intent from previous activity
     */
    private void startFragment(Intent intent) {
        Fragment fragment = null;
        int settingItemSelected = intent.getIntExtra(Constants.SETTING_ITEM_ID, R.id.readers_list);
        //Show the selected item
        switch (settingItemSelected) {
            case 0:
//                fragment = InventoryFragment.newInstance();
                break;
            case R.id.readers_list:
                fragment = ReadersListFragment.getInstance();
                break;
            case R.id.application:
                fragment = ApplicationSettingsFragment.newInstance();
                break;
            case R.id.profiles:
                fragment = ProfileFragment.newInstance();
                break;
            case R.id.advanced_options:
                fragment = AdvancedOptionItemFragment.newInstance();
                break;
            case R.id.regulatory:
                fragment = RegulatorySettingsFragment.newInstance();
                break;
            case R.id.battery:
                fragment = BatteryFragment.newInstance();
                break;
            case R.id.beeper:
                fragment = BeeperFragment.newInstance();
                break;
            case R.id.led:
                fragment = LedFragment.newInstance();
                break;
        }
        if (fragment != null) {

            getSupportFragmentManager().beginTransaction().replace(R.id.settings_content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();

        }
        setTitle(SettingsContent.ITEM_MAP.get(settingItemSelected + "").content);

    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.activityResumed();
        Application.contextSettingDetails = this;
    }

    /**
     * call back of activity,which will call before activity went to paused
     */
    @Override
    public void onPause() {
        super.onPause();
        MainActivity.activityPaused();
        Application.contextSettingDetails = null;
    }

    @Override
    protected void onDestroy() {
        // deattach to reader list handler
        RFIDController.readers.deattach(this);
        // remove notification handlers
        MainActivity.removeReaderDeviceFoundHandler(this);
        MainActivity.removeBatteryNotificationHandler(this);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startFragment(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        if (findViewById(android.R.id.home) != null)
            findViewById(android.R.id.home).setPadding(0, 0, 20, 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
            //return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //We are handling back pressed for saving settings(if any). Notify the appropriate fragment.
        //{@link BaseReceiverActivity # onBackPressed should be called by the fragment when the processing is done}
        //super.onBackPressed();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment != null && fragment instanceof BackPressedFragment) {
            ((BackPressedFragment) fragment).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Method to be called from Fragments of this activity after handling the response from the reader(success / failure)
     */
    public void callBackPressed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SettingsDetailActivity.super.onBackPressed();
            }
        });
    }

    /**
     * method to stop progress pairTaskDailog on timeout
     *
     * @param time    timeout of the progress pairTaskDailog
     * @param d       id of progress pairTaskDailog
     * @param command command that has been sent to the reader
     */
    public void timerDelayRemoveDialog(long time, final Dialog d, final String command, final boolean isPressBack) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d != null && d.isShowing()) {
                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, command + " timeout");
                    d.dismiss();
                    if (MainActivity.isActivityVisible() && isPressBack)
                        callBackPressed();
                }
            }
        }, time);
    }

    /**
     * Method called when save config button is clicked
     *
     * @param v - View to be addressed
     */
    public void saveConfigClicked(View v) {
        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected()) {
            progressDialog = new CustomProgressDialog(this, getString(R.string.save_config_progress_title));
            progressDialog.show();
            timerDelayRemoveDialog(Constants.SAVE_CONFIG_RESPONSE_TIMEOUT, progressDialog, getString(R.string.status_failure_message), false);
            new AsyncTask<Void, Void, Boolean>() {
                private OperationFailureException operationFailureException;

                @Override
                protected Boolean doInBackground(Void... voids) {
                    boolean bResult = false;
                    try {
                        RFIDController.mConnectedReader.Config.saveConfig();
                        bResult = true;
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                        operationFailureException = e;
                    }
                    return bResult;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    progressDialog.dismiss();
                    if (!result) {
                        Toast.makeText(getApplicationContext(), operationFailureException.getVendorMessage(), Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.status_success_message), Toast.LENGTH_SHORT).show();
                }
            }.execute();
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void ReaderDeviceConnected(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).ReaderDeviceConnected(device);
        } else if (fragment instanceof RegulatorySettingsFragment) {
            ((RegulatorySettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof TagReportingFragment) {
            ((TagReportingFragment) fragment).deviceConnected();
        } else if (fragment instanceof DPOSettingsFragment) {
            ((DPOSettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof AntennaSettingsFragment) {
            ((AntennaSettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof SaveConfigurationsFragment) {
            ((SaveConfigurationsFragment) fragment).deviceConnected();
        } else if (fragment instanceof SingulationControlFragment) {
            ((SingulationControlFragment) fragment).deviceConnected();
        }
    }

    @Override
    public void ReaderDeviceDisConnected(ReaderDevice device) {
        PasswordDialog.isDialogShowing = false;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).ReaderDeviceDisConnected(device);
            ((ReadersListFragment) fragment).readerDisconnected(device, false);
        } else if (fragment instanceof BatteryFragment) {
            ((BatteryFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof TagReportingFragment) {
            ((TagReportingFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof DPOSettingsFragment) {
            ((DPOSettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof AntennaSettingsFragment) {
            ((AntennaSettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof RegulatorySettingsFragment) {
            ((RegulatorySettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof SaveConfigurationsFragment) {
            ((SaveConfigurationsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof SingulationControlFragment) {
            ((SingulationControlFragment) fragment).deviceDisconnected();
        }
    }

    @Override
    public void ReaderDeviceConnFailed(ReaderDevice device) {
    }

    public void sendNotification(String action, String data) {
        if (MainActivity.isActivityVisible()) {
            if (action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_LOW)) {
                new CustomToast(this, R.layout.toast_layout, data).show();
            } else {
                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
            }
        } else {
            /*Intent i = new Intent(this, NotificationsService.class);
            i.putExtra(Constants.INTENT_ACTION, action);
            i.putExtra(Constants.INTENT_DATA, data);
            startService(i);*/

            NotificationUtil.displayNotification(this, action, data);
        }
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

    @Override
    public void RFIDReaderAppeared(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).RFIDReaderAppeared(device);
        }
//        if (RFIDController.NOTIFY_READER_AVAILABLE) {
//            if(!device.getName().equalsIgnoreCase("null"))
//                sendNotification(Constants.ACTION_READER_AVAILABLE, device.getName() + " is available.");
//        }
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).RFIDReaderDisappeared(device);
        }
//        if (RFIDController.NOTIFY_READER_AVAILABLE)
//            sendNotification(Constants.ACTION_READER_AVAILABLE, device.getName() + " is unavailable.");
    }

    @Override
    public void deviceStatusReceived(int level, boolean charging, String cause) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof BatteryFragment) {
            ((BatteryFragment) fragment).deviceStatusReceived(level, charging, cause);
        }
    }

    @Override
    public void OnAdvancedListFragmentInteractionListener(AdvancedOptionsContent.SettingItem item) {
        Fragment fragment = null;
        int settingItemSelected = Integer.parseInt(item.id);
        //Show the selected item
        switch (settingItemSelected) {
            case R.id.antenna:
                fragment = AntennaSettingsFragment.newInstance();
                break;
            case R.id.singulation_control:
                fragment = SingulationControlFragment.newInstance();
                break;
            case R.id.start_stop_triggers:
                fragment = StartStopTriggersFragment.newInstance();
                break;
            case R.id.tag_reporting:
                fragment = TagReportingFragment.newInstance();
                break;
            case R.id.save_configuration:
                fragment = SaveConfigurationsFragment.newInstance();
                break;
            case R.id.power_management:
                fragment = DPOSettingsFragment.newInstance();
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings_content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        }
        setTitle(item.content);
    }

}