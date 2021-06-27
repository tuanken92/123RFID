package com.zebra.rfidreader.demo.reader_connection;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.settings.RegulatorySettingsFragment;
import com.zebra.rfidreader.demo.settings.SettingsDetailActivity;

import java.util.ArrayList;


public class ScanAndPairFragment extends DialogFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_LOCATION_ENABLE = 0;
    ScanPair scanPair;
    Button buttonClear;
    Button buttonPair;
    Button buttonUnPair;
    EditText scanCode;
    ListView list;
    String deviceId;
    int startingLength = 0;
    LocationManager locationManager;
    private Location mylocation;
    private GoogleApiClient googleApiClient;
    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;

    private ArrayAdapter<String> mAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialogStyle);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.dw_action));
        filter.addCategory(getResources().getString(R.string.dw_category));
        getActivity().registerReceiver(scanResultBroadcast, filter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        deviceId = bundle.getString("device_id");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_pair, container, false);
        Toolbar toolbar = view.findViewById(R.id.my_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        scanPair = new ScanPair();
        scanPair.Init(getActivity(), this);
        // UI
        scanCode = view.findViewById(R.id.editText);
        // scanCode.addTextChangedListener(scanTextWatcher);
        scanCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN)
                        return true;
                    if (buttonPair != null)
                        buttonPair.performClick();
                }
                return false;
            }
        });

        buttonClear = view.findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(v -> scanCode.setText(""));
        buttonPair = view.findViewById(R.id.buttonPair);
        buttonPair.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT <=28 || (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                scanCode.setEnabled(false);
                scanPair.barcodeDeviceNameConnect(scanCode.getText().toString());
            } else {
                showLocationServiceEnablePopup();
                //showGPSEnablePopup();
            }
        });
        mAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_multiple_choice, scanPair.readers);
        list = view.findViewById(R.id.readerList);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setAdapter(mAdapter);
        //list.setOnItemClickListener(mItemClick);
        buttonUnPair = view.findViewById(R.id.bt_unpair);
        buttonUnPair.setOnClickListener(v -> {
            SparseBooleanArray checked = list.getCheckedItemPositions();
            ArrayList<String> selectedItems = new ArrayList<String>();
            for (int i = 0; i < checked.size(); i++) {
                // Item position in adapter
                int position = checked.keyAt(i);
                // Add sport if it is checked i.e.) == TRUE!
                if (checked.valueAt(i) && position <= mAdapter.getCount() - 1)
                    scanPair.unpair(mAdapter.getItem(position));
            }

        });
        // permissions
        checkForExportPermission();
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOCATION_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
                scanCode.setEnabled(false);
                scanPair.barcodeDeviceNameConnect(scanCode.getText().toString());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private TextWatcher scanTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            startingLength = start;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            boolean atleastOneAlpha = s.toString().matches(".*[a-zA-Z]+.*");
            if ((atleastOneAlpha && s.length() == 12) || !atleastOneAlpha && s.length() == 14) {
                scanCode.setEnabled(false);
                scanPair.barcodeDeviceNameConnect(scanCode.getText().toString());

            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
        scanCode.setText(deviceId);
    }

    @Override
    public void onResume() {
        super.onResume();
        scanPair.onResume();
        enableScanner();
    }

    @Override
    public void onPause() {
        super.onPause();
        scanPair.onPause();
        disableScanner();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scanPair.onDestroy();
        getActivity().unregisterReceiver(scanResultBroadcast);
    }

    void alertLocationServiceUsage() {

        AlertDialog builder = new AlertDialog.Builder(getContext()).setCancelable(false)
                .setTitle(R.string.Location_Alert_title).setMessage(R.string.warning_text_location_enable)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if (Build.VERSION.SDK_INT <= 28 || (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                            scanCode.setEnabled(false);
                            scanPair.barcodeDeviceNameConnect(scanCode.getText().toString());
                        } else {
                            //showGPSEnablePopup();
                            showLocationServiceEnablePopup();
                        }
                    }
                }).create();
                builder.show();
    }
    void checkForExportPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }
    }

    public void refreshList() {
        if (scanCode != null)
            scanCode.setEnabled(true);
        getActivity().runOnUiThread(() -> {
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();

        });
    }

    public void removeDevice(String deviceId) {

        if (mAdapter != null) {
            if (list != null)
                list.clearChoices();
            mAdapter.remove(deviceId);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void processCompleted(String message) {

        scanCode.setEnabled(true);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


/*    public void toggle(View v) {
        CheckedTextView checkedTextView = v.findViewById(R.id.readerItem);
        checkedTextView.setChecked(!checkedTextView.isChecked());
        checkedTextView.setCheckMarkDrawable(checkedTextView.isChecked() ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);
    }*/

    public void connectDevice(String rdDevice, boolean b) {
        Toast.makeText(getContext(), "Device ready to connect:" + rdDevice, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void enableScanner() {

        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "ENABLE_PLUGIN");
        i.putExtra("SEND_RESULT", "false");
        i.putExtra("COMMAND_IDENTIFIER", Application.RFID_DATAWEDGE_ENABLE_SCANNER);
        getActivity().sendBroadcast(i);


    }

    private void disableScanner() {

        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "DISABLE_PLUGIN");
        i.putExtra("SEND_RESULT", "false");
        i.putExtra("COMMAND_IDENTIFIER", Application.RFID_DATAWEDGE_DISABLE_SCANNER);  //Unique identifier
        getActivity().sendBroadcast(i);

    }


    private BroadcastReceiver scanResultBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action!=null && action.equals(getResources().getString(R.string.dw_action))) {
                displayScanResult(intent);
            }

        }
    };

    private void displayScanResult(Intent initiatingIntent) {
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        if (decodedData != null && scanCode!=null) {
            scanCode.setText(decodedData);
            scanCode.setSelection(decodedData.length());
            if (buttonPair != null)
                buttonPair.performClick();

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void showLocationServiceEnablePopup()
    {
        AlertDialog builder = new AlertDialog.Builder(getContext()).setCancelable(false)
                           .setTitle(R.string.Location_Alert_title).setMessage(R.string.warning_text_location_enable)
                           .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                       showGPSEnablePopup();
                   }
                }).create();
                            builder.show();

    }

    private void showGPSEnablePopup() {
        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Toast.makeText(getActivity(), "GPS Enabled", Toast.LENGTH_SHORT).show();
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Toast.makeText(getActivity(), "GPS Need to be Enable ..!!", Toast.LENGTH_SHORT).show();
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                //status.startResolutionForResult(getActivity(), REQUEST_CODE_LOCATION_ENABLE);
                                startIntentSenderForResult(status.getResolution().getIntentSender(), REQUEST_CODE_LOCATION_ENABLE, null, 0, 0, 0, null);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Toast.makeText(getActivity(), "GPS can not be enable", Toast.LENGTH_SHORT).show();
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

}

