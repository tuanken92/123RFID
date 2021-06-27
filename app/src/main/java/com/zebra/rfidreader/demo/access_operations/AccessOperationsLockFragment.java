package com.zebra.rfidreader.demo.access_operations;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.LOCK_PRIVILEGE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.asciitohex;
import com.zebra.rfidreader.demo.common.hextoascii;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import static com.zebra.rfidreader.demo.home.MainActivity.filter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link AccessOperationsLockFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle the Access Lock Operation
 */
public class AccessOperationsLockFragment extends Fragment implements AdapterView.OnItemSelectedListener, AccessOperationsFragment.OnRefreshListener {
    private LOCK_PRIVILEGE lockAccessPermission = LOCK_PRIVILEGE.LOCK_PRIVILEGE_READ_WRITE;
    private String lockMemoryBank = "epc";
    private AutoCompleteTextView tagIDField;
    private ArrayAdapter<String> adapter;

    public AccessOperationsLockFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccessOperationsLockFragment.
     */
    public static AccessOperationsLockFragment newInstance() {
        return new AccessOperationsLockFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_access_operations_lock, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeSpinner();
        tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.accessLockTagID));
        RFIDController.getInstance().updateTagIDs();
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.tagIDs);
        tagIDField.setAdapter(adapter);

        if (RFIDController.asciiMode == true) {
            tagIDField.setFilters(new InputFilter[]{filter});

        } else {
            tagIDField.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});


        }

        if (RFIDController.accessControlTag != null) {
            if (RFIDController.asciiMode == true)
                tagIDField.setText(hextoascii.convert(RFIDController.accessControlTag));
            else
                tagIDField.setText(RFIDController.accessControlTag);
        }
    }

    /**
     * method to initialize memory bank spinner
     */
    private void initializeSpinner() {
        Spinner memoryBankSpinner = (Spinner) getActivity().findViewById(R.id.accessLockMemoryBank);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.acess_lock_memory_bank_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        memoryBankSpinner.setAdapter(memoryBankAdapter);

        Spinner lockSpinner = (Spinner) getActivity().findViewById(R.id.accessLockPrivilege);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> lockAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.acess_lock_privilege_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        lockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        lockSpinner.setAdapter(lockAdapter);

        memoryBankSpinner.setOnItemSelectedListener(this);
        lockSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void handleTagResponse(final TagData response_tagData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ACCESS_OPERATION_STATUS readAccessOperation = null;

                if (readAccessOperation != null) {
                    if (response_tagData.getOpStatus() != null && !response_tagData.getOpStatus().equals(ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)) {
                        Toast.makeText(getActivity(), readAccessOperation.getValue(), Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getActivity(), R.string.msg_lock_succeed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * method to get lock access Permission type
     *
     * @return lock access permission type
     */
    public LOCK_PRIVILEGE getLockAccessPermission() {
        return lockAccessPermission;
    }

    /**
     * method to get memory bank on which lock permission is applying
     *
     * @return tag memory bank
     */
    public String getLockMemoryBank() {
        return lockMemoryBank;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        if (adapterView == getActivity().findViewById(R.id.accessLockPrivilege)) {
            switch (pos) {
                case 0:
                    lockAccessPermission = LOCK_PRIVILEGE.LOCK_PRIVILEGE_READ_WRITE;
                    ;
                    break;
                case 1:
                    lockAccessPermission = LOCK_PRIVILEGE.LOCK_PRIVILEGE_PERMA_LOCK;
                    break;
                case 2:
                    lockAccessPermission = LOCK_PRIVILEGE.LOCK_PRIVILEGE_PERMA_UNLOCK;
                    break;
                case 3:
                    lockAccessPermission = LOCK_PRIVILEGE.LOCK_PRIVILEGE_UNLOCK;
                    break;
            }
        } else if (adapterView == getActivity().findViewById(R.id.accessLockMemoryBank)) {
            lockMemoryBank = adapterView.getSelectedItem().toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onUpdate() {
        if (isVisible() && tagIDField != null) {
            if (RFIDController.asciiMode == true)
                RFIDController.accessControlTag = asciitohex.convert(tagIDField.getText().toString());
            else
                RFIDController.accessControlTag = tagIDField.getText().toString();
        }
    }

    @Override
    public void onRefresh() {
        if (RFIDController.accessControlTag != null && tagIDField != null) {
            if (RFIDController.asciiMode == true)
                tagIDField.setText(hextoascii.convert(RFIDController.accessControlTag));
            else
                tagIDField.setText(RFIDController.accessControlTag);
        }
    }
}
