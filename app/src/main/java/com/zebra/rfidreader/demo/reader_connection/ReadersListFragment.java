package com.zebra.rfidreader.demo.reader_connection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.CustomProgressDialog;
import com.zebra.rfidreader.demo.common.Inventorytimer;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.rfid.RFIDController;
import com.zebra.rfidreader.demo.settings.AdvancedOptionsContent;
import com.zebra.rfidreader.demo.settings.SettingsDetailActivity;

import java.util.ArrayList;

import static android.os.AsyncTask.Status.FINISHED;
import static com.zebra.rfidreader.demo.rfid.RFIDController.AUTO_RECONNECT_READERS;
import static com.zebra.rfidreader.demo.rfid.RFIDController.LAST_CONNECTED_READER;
import static com.zebra.rfidreader.demo.rfid.RFIDController.TAG;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mConnectedDevice;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mConnectedReader;
import static com.zebra.rfidreader.demo.settings.AdvancedOptionsContent.DPO_ITEM_INDEX;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link ReadersListFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to maintain the list of readers
 */
public class ReadersListFragment extends Fragment {
    public static ArrayList<ReaderDevice> readersList = new ArrayList<>();
    private PasswordDialog passwordDialog;
    private DeviceConnectTask deviceConnectTask;
    private static final String RFD8500 = "RFD8500";
    private ReaderListAdapter readerListAdapter;
    private ListView pairedListView;
    private TextView tv_emptyView;
    private CustomProgressDialog progressDialog;
    private Activity activity = null;
    private static ReadersListFragment rlf = null;
    private ScanAndPairFragment scanAndPairFragment;
    private EditText scanCode;
    private boolean isOnStopCalled = false;

    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {
            // Get the device MAC address, which is the last 17 chars in the View


            ReaderDevice readerDevice = readerListAdapter.getItem(pos);
            if (RFIDController.mConnectedReader == null) {
                if (deviceConnectTask == null || deviceConnectTask.isCancelled()) {
                    RFIDController.is_connection_requested = true;
                    Toast.makeText(getActivity(), R.string.warning_bt_enable_on_sled, Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                        deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                        deviceConnectTask.execute();
                    }
                }
            } else {
                {
                    if (RFIDController.mConnectedReader.isConnected()) {
                        RFIDController.is_disconnection_requested = true;
                        try {
                            RFIDController.mConnectedReader.disconnect();
                        } catch (InvalidUsageException e) {
                            e.printStackTrace();
                        } catch (OperationFailureException e) {
                            e.printStackTrace();
                        }
                        //
                        ReaderDeviceDisConnected(RFIDController.mConnectedDevice);
                        if (RFIDController.NOTIFY_READER_CONNECTION)
                            sendNotification(Constants.ACTION_READER_DISCONNECTED, "Disconnected from " + RFIDController.mConnectedReader.getHostName());
                        //
                        clearSettings();
                    }
                    if (!RFIDController.mConnectedReader.getHostName().equalsIgnoreCase(readerDevice.getName())) {
                        RFIDController.mConnectedReader = null;
                        if (deviceConnectTask == null || deviceConnectTask.isCancelled()) {
                            if (MainActivity.DisconnectTask != null && AUTO_RECONNECT_READERS)
                                MainActivity.DisconnectTask.cancel(true);
                            RFIDController.is_connection_requested = true;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                                deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                                deviceConnectTask.execute();
                            }
                        }
                    } else {
                        RFIDController.mConnectedReader = null;
                    }
                }
            }
            // Create the result Intent and include the MAC address
        }
    };

    private void CancelReconnect() {
        if (MainActivity.DisconnectTask != null && AUTO_RECONNECT_READERS) {
            int timeout = 20;
            while (FINISHED != MainActivity.DisconnectTask.getStatus() && timeout > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeout--;
            }
        }
    }

    public ReadersListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReadersListFragment.
     */
    public static ReadersListFragment newInstance() {
        return new ReadersListFragment();
    }

    public static ReadersListFragment getInstance() {
        if (rlf == null)
            rlf = new ReadersListFragment();
        return rlf;
    }

    private void clearSettings() {
        RFIDController.clearSettings();
        RFIDController.stopTimer();
        getActivity().invalidateOptionsMenu();
        Inventorytimer.getInstance().stopTimer();
        RFIDController.mIsInventoryRunning = false;
        if (RFIDController.mIsInventoryRunning) {
            RFIDController.isBatchModeInventoryRunning = false;
        }
        if (RFIDController.isLocatingTag) {
            RFIDController.isLocatingTag = false;
        }
        //update dpo icon in settings list
        AdvancedOptionsContent.ITEMS.get(DPO_ITEM_INDEX).icon = R.drawable.title_dpo_disabled;
        RFIDController.mConnectedDevice = null;
        RFIDController.isAccessCriteriaRead = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (activity == null)
            activity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // registerReceivers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_readers_list, menu);
        boolean isScanAndPairVisible = Application.isReaderConnectedThroughBluetooth;
        /*if (RFIDController.mConnectedReader != null) {
            if (RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")) {
                isScanAndPairVisible = true;
            }
        } else {
            isScanAndPairVisible = true;
        }*/
        menu.findItem(R.id.action_scan_pair).setVisible(isScanAndPairVisible).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                scanAndPairFragment = new ScanAndPairFragment();
                Bundle bundle = new Bundle();
                bundle.putString("device_id", "");
                scanAndPairFragment.setArguments(bundle);
                scanAndPairFragment.show(getFragmentManager(), "fragment_edit_name");
                getFragmentManager().executePendingTransactions();

                return true;
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_readers_list, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeViews();
        readersList.clear();
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, readersList);
        if (readerListAdapter.getCount() == 0) {
            pairedListView.setEmptyView(tv_emptyView);
        } else
            pairedListView.setAdapter(readerListAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        loadPairedDevices(null, false);
    }

    public void loadUIData(){
        readersList.clear();
        loadPairedDevices(null, false);
    }

    private void initializeViews() {
        pairedListView = (ListView) getActivity().findViewById(R.id.bondedReadersList);
        tv_emptyView = (TextView) getActivity().findViewById(R.id.empty);

        scanCode = getActivity().findViewById(R.id.et_barcodevalue);
        scanCode.setInputType(0);
        scanCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 14 && !s.toString().contains(":")) {

                    String id = scanCode.getText().toString();
                    scanCode.setText("");
                    scanCode.setInputType(0);
                    connectDevice(id);
                }

            }

        });
    }

    private void loadPairedDevices(final String deviceId, final boolean isClick) {
        new AsyncTask<Void, Void, Boolean>() {
            InvalidUsageException exception;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    ArrayList<ReaderDevice> readersListArray = RFIDController.readers.GetAvailableRFIDReaderList();
                    readersList.addAll(readersListArray);
                    Log.d(TAG, "readersList: " + readersList.size());

                } catch (InvalidUsageException ex) {
                    exception = ex;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (exception != null)
                    Toast.makeText(getActivity(), exception.getInfo(), Toast.LENGTH_SHORT).show();
                else {
                    if (RFIDController.mConnectedDevice != null) {
                        int index = readersList.indexOf(RFIDController.mConnectedDevice);
                        Log.d(TAG, "index: " + index);
                        if (index != -1) {
                            readersList.remove(index);
                            readersList.add(index, RFIDController.mConnectedDevice);
                        } else {
                            RFIDController.mConnectedDevice = null;
                            RFIDController.mConnectedReader = null;
                        }
                    }
                    if (readerListAdapter.getCount() != 0) {
                        tv_emptyView.setVisibility(View.GONE);
                        pairedListView.setAdapter(readerListAdapter);
                    }
                    readerListAdapter.notifyDataSetChanged();
                }


             /*   // Toast.makeText(getActivity(), "loadPairedDevices" + readersList.size(), Toast.LENGTH_SHORT).show();
                if (isClick && deviceId != null && readerListAdapter != null && readerListAdapter.getCount() >= 1) {

                    int position = getPosition(deviceId);
                    if (position >= 0 && position < pairedListView.getAdapter().getCount())
                        pairedListView.performItemClick(
                                pairedListView.getAdapter().getView(position, null, null),
                                position,
                                pairedListView.getAdapter().getItemId(position));
                }

*/
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    private void connectDevice(String id) {

        if (getPosition("RFD8500" + id) != -1) {

            if (mConnectedDevice == null || !mConnectedDevice.getName().equals("RFD8500" + id)) {
                if (readersList != null)
                    readersList.clear();
                loadPairedDevices("RFD8500" + id, true);
            } else {

                Toast.makeText(activity, "Device already connected", Toast.LENGTH_SHORT).show();
            }

        } else {

            scanAndPairFragment = new ScanAndPairFragment();
            Bundle bundle = new Bundle();
            bundle.putString("device_id", id);
            scanAndPairFragment.setArguments(bundle);
            scanAndPairFragment.show(getFragmentManager(), "fragment_edit_name");
            getFragmentManager().executePendingTransactions();

        }


    }

    private int getPosition(String name) {

        for (int i = 0; i < readersList.size(); i++) {

            if (readersList.get(i).getName().equals(name))
                return i;
        }

        return -1;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (scanCode != null)
            scanCode.setInputType(0);
        isOnStopCalled = false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (scanCode != null)
            scanCode.setInputType(0);
        if (PasswordDialog.isDialogShowing) {
            if (passwordDialog == null || !passwordDialog.isShowing()) {
                showPasswordDialog(RFIDController.mConnectedDevice);
            }
        }
        capabilitiesRecievedforDevice();


    }

    @Override
    public void onPause() {
        super.onPause();
        if (passwordDialog != null && passwordDialog.isShowing()) {
            PasswordDialog.isDialogShowing = true;
            passwordDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isOnStopCalled = true;
    }

    /**
     * method to update connected reader device in the readers list on device connected event
     *
     * @param device device to be updated
     */
    public void ReaderDeviceConnected(ReaderDevice device) {
//        if (deviceConnectTask != null)
//            deviceConnectTask.cancel(true);


        if (device != null) {

            if (!Application.isReaderConnectedThroughBluetooth ||BluetoothHandler.isDevicePaired(device.getName())) {
                RFIDController.mConnectedDevice = device;
                RFIDController.is_connection_requested = false;
                changeTextStyle(device);
            } else {

                try {
                    mConnectedReader.disconnect();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
                RFIDController.mConnectedReader = null;
                clearConnectedReader();

            }
        } else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
    }

    void clearConnectedReader() {
        SharedPreferences settings = getContext().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.LAST_READER, "");
        editor.commit();
        LAST_CONNECTED_READER = "";
        RFIDController.mConnectedDevice = null;
    }

    public void ReaderDeviceDisConnected(ReaderDevice device) {
        if (deviceConnectTask != null && !deviceConnectTask.isCancelled() && deviceConnectTask.getConnectingDevice().getName().equalsIgnoreCase(device.getName())) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (deviceConnectTask != null)
                deviceConnectTask.cancel(true);
        }
        if (device != null) {
            changeTextStyle(device);
        } else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
        RFIDController.clearSettings();

    }

    public void readerDisconnected(ReaderDevice device, boolean forceDisconnect) {
        if (device != null) {
            if (RFIDController.mConnectedReader != null && (!AUTO_RECONNECT_READERS || forceDisconnect)) {
                try {
                    RFIDController.mConnectedReader.disconnect();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
                RFIDController.mConnectedReader = null;

            }
            for (int idx = 0; idx < readersList.size(); idx++) {
                if (readersList.get(idx).getName().equalsIgnoreCase(device.getName()))
                    changeTextStyle(readersList.get(idx));
            }
        }
    }

    /**
     * method to update reader device in the readers list on device connection failed event
     *
     * @param device device to be updated
     */
    public void ReaderDeviceConnFailed(ReaderDevice device) {
        if (isVisible() && progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
        if (device != null)
            changeTextStyle(device);
        else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
        sendNotification(Constants.ACTION_READER_CONN_FAILED, "Connection Failed!! was received");
        RFIDController.mConnectedReader = null;
        RFIDController.mConnectedDevice = null;
    }

    /**
     * check/un check the connected/disconnected reader list item
     *
     * @param device device to be updated
     */
    private void changeTextStyle(final ReaderDevice device) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int i = readerListAdapter.getPosition(device);
                    if (i >= 0) {
                        readerListAdapter.remove(device);
                        readerListAdapter.insert(device, i);
                        readerListAdapter.notifyDataSetChanged();

                    }
                }
            });
        }
    }


    public void RFIDReaderAppeared(final ReaderDevice readerDevice) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (readerListAdapter != null && readerDevice != null) {
                        if (readerListAdapter.getCount() == 0) {
                            tv_emptyView.setVisibility(View.GONE);
                            pairedListView.setAdapter(readerListAdapter);
                        }
                        if (readersList.contains(readerDevice))
                            Log.d(TAG, "Duplicate reader");
                        else
                            readersList.add(readerDevice);
                        readerListAdapter.notifyDataSetChanged();

                        // Connect automatically with the latest paired device.

/*                        if (!isOnStopCalled && LAST_CONNECTED_READER != null && LAST_CONNECTED_READER.length() !=0 &&
                                !LAST_CONNECTED_READER.equals(readerDevice.getName())) {
                            int position = getPosition(readerDevice.getName());
                            if (position >= 0 && position < pairedListView.getAdapter().getCount())
                                pairedListView.performItemClick(
                                        pairedListView.getAdapter().getView(position, null, null),
                                        position,
                                        pairedListView.getAdapter().getItemId(position));

                        }*/

                    }
                }
            });
        }
    }

    public void RFIDReaderDisappeared(final ReaderDevice readerDevice) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (readerListAdapter != null && readerDevice != null) {
                        readerListAdapter.remove(readerDevice);
                        readersList.remove(readerDevice);
                        if (readerListAdapter.getCount() == 0) {
                            pairedListView.setEmptyView(tv_emptyView);
                        }
                        readerListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    /**
     * method to update serial and model of connected reader device
     */
    public void capabilitiesRecievedforDevice() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (readerListAdapter.getPosition(RFIDController.mConnectedDevice) >= 0) {
                    ReaderDevice readerDevice = readerListAdapter.getItem(readerListAdapter.getPosition(RFIDController.mConnectedDevice));
                    //readerDevice.setModel(RFIDController.mConnectedDevice.getModel());
                    //readerDevice.setSerial(RFIDController.mConnectedDevice.getSerial());
                    readerListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * method to show connect password pairTaskDailog
     *
     * @param connectingDevice
     */
    public void showPasswordDialog(ReaderDevice connectingDevice) {
        if (MainActivity.isActivityVisible()) {
            passwordDialog = new PasswordDialog(activity, connectingDevice);
            passwordDialog.show();
        } else
            PasswordDialog.isDialogShowing = true;
    }

    /**
     * method to cancel progress pairTaskDailog
     */
    public void cancelProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
    }

    public void ConnectwithPassword(String password, ReaderDevice readerDevice) {
        try {
            if (mConnectedReader != null)
                mConnectedReader.disconnect();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
        deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), password);
        deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * method to get connect password for the reader
     *
     * @param address - device BT address
     * @return connect password of the reader
     */
    private String getReaderPassword(String address) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.READER_PASSWORDS, 0);
        return sharedPreferences.getString(address, null);
    }

    private void sendNotification(String action, String data) {
        if (activity != null) {
            if (activity.getTitle().toString().equalsIgnoreCase(getString(R.string.title_activity_settings_detail)) || activity.getTitle().toString().equalsIgnoreCase(getString(R.string.title_activity_readers_list)))
                ((SettingsDetailActivity) activity).sendNotification(action, data);
            else
                ((MainActivity) activity).sendNotification(action, data);
        }
    }

    void StoreConnectedReader() {
        if (AUTO_RECONNECT_READERS && mConnectedReader != null) {
            LAST_CONNECTED_READER = RFIDController.mConnectedReader.getHostName();
            SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Constants.LAST_READER, LAST_CONNECTED_READER);
            editor.commit();
        }
    }

    /**
     * async task to go for BT connection with reader
     */
    private class DeviceConnectTask extends AsyncTask<Void, String, Boolean> {
        private final ReaderDevice connectingDevice;
        private String prgressMsg;
        private OperationFailureException ex;
        private String password;

        DeviceConnectTask(ReaderDevice connectingDevice, String prgressMsg, String Password) {
            this.connectingDevice = connectingDevice;
            this.prgressMsg = prgressMsg;
            password = Password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(activity, prgressMsg);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... a) {
            CancelReconnect();
            try {
                if (password != null)
                    connectingDevice.getRFIDReader().setPassword(password);
                connectingDevice.getRFIDReader().connect();
                if (password != null) {
                    SharedPreferences.Editor editor = activity.getSharedPreferences(Constants.READER_PASSWORDS, 0).edit();
                    editor.putString(connectingDevice.getName(), password);
                    editor.commit();
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                ex = e;
            }
            if (connectingDevice.getRFIDReader().isConnected()) {
                RFIDController.mConnectedReader = connectingDevice.getRFIDReader();
                StoreConnectedReader();
                try {
                    RFIDController.mConnectedReader.Events.addEventsListener(MainActivity.eventHandler);
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
                connectingDevice.getRFIDReader().Events.setBatchModeEvent(true);
                connectingDevice.getRFIDReader().Events.setReaderDisconnectEvent(true);
                connectingDevice.getRFIDReader().Events.setBatteryEvent(true);
                connectingDevice.getRFIDReader().Events.setInventoryStopEvent(true);
                connectingDevice.getRFIDReader().Events.setInventoryStartEvent(true);
                RFIDController.mConnectedReader.Events.setTagReadEvent(true);
                // if no exception in connect
                if (ex == null) {
                    try {
                        RFIDController.getInstance().updateReaderConnection(false);
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                } else {
                    RFIDController.clearSettings();
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (activity != null && !activity.isFinishing())
                progressDialog.cancel();
            if (ex != null) {
                if (ex.getResults() == RFIDResults.RFID_CONNECTION_PASSWORD_ERROR) {
                    showPasswordDialog(connectingDevice);

                    ReaderDeviceConnected(connectingDevice);
                } else if (ex.getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                    RFIDController.isBatchModeInventoryRunning = true;
                    RFIDController.mIsInventoryRunning = true;

                    ReaderDeviceConnected(connectingDevice);
                    if (RFIDController.NOTIFY_READER_CONNECTION)
                        sendNotification(Constants.ACTION_READER_CONNECTED, "Connected to " + connectingDevice.getName());
                } else if (ex.getResults() == RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {

                    ReaderDeviceConnected(connectingDevice);
                    RFIDController.regionNotSet = true;
                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.set_region_msg));
                    Intent detailsIntent = new Intent(activity, SettingsDetailActivity.class);
                    detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    detailsIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.regulatory);
                    startActivity(detailsIntent);
                } else
                    ReaderDeviceConnFailed(connectingDevice);
            } else {
                if (result) {
                    if (RFIDController.NOTIFY_READER_CONNECTION)
                        sendNotification(Constants.ACTION_READER_CONNECTED, "Connected to " + connectingDevice.getName());

                    ReaderDeviceConnected(connectingDevice);
                } else {
                    ReaderDeviceConnFailed(connectingDevice);
                }
            }
            deviceConnectTask = null;
        }

        @Override
        protected void onCancelled() {
            deviceConnectTask = null;
            super.onCancelled();
        }

        public ReaderDevice getConnectingDevice() {
            return connectingDevice;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }


}
