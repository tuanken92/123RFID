package com.zebra.rfidreader.demo.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.CustomProgressDialog;
import com.zebra.rfidreader.demo.rfid.RFIDController;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isSimplePreFilterEnabled;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mConnectedReader;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link SingulationControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle singulation operations and UI.
 */
public class SingulationControlFragment extends BackPressedFragment implements Spinner.OnItemSelectedListener {

    private TextView tv_preFilterEnabled;
    private Spinner sessionSpinner;
    private Spinner tagPopulationSpinner;
    private Spinner inventoryStateSpinner;
    private Spinner slFlagSpinner;
    private ArrayAdapter<CharSequence> tagPopulationAdapter;

//    private Antennas.SingulationControl singulationControl;

    public SingulationControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SingulationControlFragment.
     */
    public static SingulationControlFragment newInstance() {
        return new SingulationControlFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_singulation_control, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tv_preFilterEnabled = (TextView) getActivity().findViewById(R.id.tv_preFilterEnabled);

        sessionSpinner = (Spinner) getActivity().findViewById(R.id.session);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> sessionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.session_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        sessionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sessionSpinner.setAdapter(sessionAdapter);
        tagPopulationSpinner = (Spinner) getActivity().findViewById(R.id.tagPopulation);
        // Create an ArrayAdapter using the string array and a default spinner layout
        tagPopulationAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.tag_population_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        tagPopulationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        tagPopulationSpinner.setAdapter(tagPopulationAdapter);
        inventoryStateSpinner = (Spinner) getActivity().findViewById(R.id.inventoryState);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> inventoryAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inventory_state_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        inventoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        inventoryStateSpinner.setAdapter(inventoryAdapter);
        slFlagSpinner = (Spinner) getActivity().findViewById(R.id.slFlag);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> slAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sl_flags_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        slAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        slFlagSpinner.setAdapter(slAdapter);
        // defaults
        sessionSpinner.setSelection(0, false);
        tagPopulationSpinner.setSelection(0, false);
        inventoryStateSpinner.setSelection(0, false);
        slFlagSpinner.setSelection(0, false);
        // reader settings
        if (mConnectedReader != null && mConnectedReader.isConnected() && mConnectedReader.isCapabilitiesReceived() && RFIDController.singulationControl != null) {
            sessionSpinner.setSelection(RFIDController.singulationControl.getSession().getValue());
            tagPopulationSpinner.setSelection(tagPopulationAdapter.getPosition(RFIDController.singulationControl.getTagPopulation() + ""));
            inventoryStateSpinner.setSelection(RFIDController.singulationControl.Action.getInventoryState().getValue());
            //slFlagSpinner.setSelection(RFIDController.singulationControl.Action.getSLFlag().getValue());
            switch (RFIDController.singulationControl.Action.getSLFlag().getValue()) {
                case 0:
                    slFlagSpinner.setSelection(2);
                    break;
                case 1:
                    slFlagSpinner.setSelection(1);
                    break;
                case 2:
                    slFlagSpinner.setSelection(0);
                    break;
            }
        }
        sessionSpinner.setOnItemSelectedListener(this);
        tagPopulationSpinner.setOnItemSelectedListener(this);
        inventoryStateSpinner.setOnItemSelectedListener(this);
        slFlagSpinner.setOnItemSelectedListener(this);


        // enable settings if reader is not connected
        // enable settings if advanced prefilter is enabled
        // disable if simple prefilter is enabled
        boolean enableSLOptions = !isSimplePreFilterEnabled();

        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        boolean showAdvancedOptions = settings.getBoolean(Constants.PREFILTER_ADV_OPTIONS, false);
        if(showAdvancedOptions)
            enableSLOptions = true;


        sessionSpinner.setEnabled(enableSLOptions);
        inventoryStateSpinner.setEnabled(enableSLOptions);
        slFlagSpinner.setEnabled(enableSLOptions);
        getActivity().findViewById(R.id.tv_preFilterEnabled).setVisibility(enableSLOptions ? View.GONE: View.VISIBLE);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //DO Nothing
    }

    @Override
    public void onBackPressed() {
        if ((sessionSpinner.getSelectedItem() != null && tagPopulationSpinner.getSelectedItem() != null && inventoryStateSpinner.getSelectedItem() != null
                && slFlagSpinner.getSelectedItem() != null)) {
            if (isSettingsChanged()) {
                new Task_SaveSingulationConfiguration(sessionSpinner.getSelectedItemPosition(), tagPopulationSpinner.getSelectedItemPosition(), inventoryStateSpinner.getSelectedItemPosition(), slFlagSpinner.getSelectedItemPosition()).execute();
            } else {
                AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
                replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
            }

        }
    }

    /**
     * method to know whether singulation control settings has changed
     *
     * @return true if settings has changed or false if settings has not changed
     */
    private boolean isSettingsChanged() {
        if (RFIDController.singulationControl != null) {
            if (RFIDController.singulationControl.getSession().getValue() != sessionSpinner.getSelectedItemPosition())
                return true;
            else if (RFIDController.singulationControl.getTagPopulation() != Integer.parseInt(tagPopulationSpinner.getSelectedItem().toString()))
                return true;
            else if (RFIDController.singulationControl.Action.getInventoryState().getValue() != inventoryStateSpinner.getSelectedItemPosition())
                return true;
            else {
                int pos = slFlagSpinner.getSelectedItemPosition();
                switch (pos) {
                    case 0:
                        if (RFIDController.singulationControl.Action.getSLFlag() != SL_FLAG.SL_ALL)
                            return true;
                        break;
                    case 1:
                        if (RFIDController.singulationControl.Action.getSLFlag() != SL_FLAG.SL_FLAG_DEASSERTED)
                            return true;
                        break;
                    case 2:
                        if (RFIDController.singulationControl.Action.getSLFlag() != SL_FLAG.SL_FLAG_ASSERTED)
                            return true;
                        break;
                }
            }
        }
        return false;
    }


    private class Task_SaveSingulationConfiguration extends AsyncTask<Void, Void, Boolean> {
        private CustomProgressDialog progressDialog;
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;
        private final int session;
        private final int tagpopulation;
        private final int inventorystate;
        private final int slflag;

        public Task_SaveSingulationConfiguration(int sessionIndex, int tagPopulationIndex, int invStateIndex, int slflagindex) {
            session = sessionIndex;
            tagpopulation = tagPopulationIndex;
            inventorystate = invStateIndex;
            slflag = slflagindex;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.singulation_progress_title));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            Antennas.SingulationControl singulationControl;
            try {
                singulationControl = mConnectedReader.Config.Antennas.getSingulationControl(1);
                singulationControl.setSession(SESSION.GetSession(session));
                singulationControl.setTagPopulation(Short.parseShort(tagPopulationAdapter.getItem(tagpopulation).toString()));
                singulationControl.Action.setInventoryState(INVENTORY_STATE.GetInventoryState(inventorystate));
                switch (slflag) {
                    case 0:
                        singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                        break;
                    case 1:
                        singulationControl.Action.setSLFlag(SL_FLAG.SL_FLAG_DEASSERTED);
                        break;
                    case 2:
                        singulationControl.Action.setSLFlag(SL_FLAG.SL_FLAG_ASSERTED);
                        break;
                }
                mConnectedReader.Config.Antennas.setSingulationControl(1, singulationControl);
                RFIDController.singulationControl = singulationControl;
                ProfileContent.UpdateActiveProfile();
                return true;
            } catch (InvalidUsageException e) {
                e.printStackTrace();
                invalidUsageException = e;
            } catch (OperationFailureException e) {
                e.printStackTrace();
                operationFailureException = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.cancel();
            if (!result) {
                if (invalidUsageException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                if (operationFailureException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
            }
            if (invalidUsageException == null && operationFailureException == null)
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
            AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
            replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
            //((SettingsDetailActivity) getActivity()).callBackPressed();
        }
    }

    public void deviceConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (RFIDController.singulationControl != null) {
                    sessionSpinner.setSelection(RFIDController.singulationControl.getSession().getValue());
                    tagPopulationSpinner.setSelection(tagPopulationAdapter.getPosition(RFIDController.singulationControl.getTagPopulation() + ""));
                    inventoryStateSpinner.setSelection(RFIDController.singulationControl.Action.getInventoryState().getValue());
                    switch (RFIDController.singulationControl.Action.getSLFlag().getValue()) {
                        case 0:
                            slFlagSpinner.setSelection(2);
                            break;
                        case 1:
                            slFlagSpinner.setSelection(1);
                            break;
                        case 2:
                            slFlagSpinner.setSelection(0);
                            break;
                    }
                }
            }
        });
    }

    public void deviceDisconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // defaults
                sessionSpinner.setSelection(0, false);
                tagPopulationSpinner.setSelection(0, false);
                inventoryStateSpinner.setSelection(0, false);
                slFlagSpinner.setSelection(0, false);
            }
        });
    }

    public static void replaceFragment(@NonNull FragmentManager fragmentManager,
                                       @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.disallowAddToBackStack();
        transaction.commit();
    }
//    @Override
//    public void handleStatusResponse(final Response_Status statusData) {
//
//        if (statusData.command.trim().equalsIgnoreCase(Constants.COMMAND_QUERYPARAMS))
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (statusData.Status.trim().equalsIgnoreCase("OK")) {
//                        ((BaseReceiverActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_success_message));
//                    } else
//                        ((BaseReceiverActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + statusData.Status);
//
//                    ((SettingsDetailActivity) getActivity()).callBackPressed();
//                }
//            });
//    }
}
