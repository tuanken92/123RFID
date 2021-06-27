package com.zebra.rfidreader.demo.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.InputFilterMax;
import com.zebra.rfidreader.demo.common.PreFilters;
import com.zebra.rfidreader.demo.common.hextoascii;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import static com.zebra.rfidreader.demo.home.MainActivity.filter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link PreFilter1ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to show the pre-filter 1 UI.
 */
public class PreFilter1ContentFragment extends Fragment {
    private EditText preFilterOffset;

    public PreFilter1ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PreFilter1ContentFragment.
     */
    public static PreFilter1ContentFragment newInstance() {
        return new PreFilter1ContentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pre_filter_content, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeSpinner();
        AutoCompleteTextView tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID));
        RFIDController.getInstance().updateTagIDs();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.tagIDs);
        tagIDField.setAdapter(adapter);
        if(RFIDController.asciiMode == true)
            tagIDField.setFilters(new InputFilter[]{filter});
        else
            tagIDField.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});

        preFilterOffset = ((EditText) getActivity().findViewById(R.id.preFilterOffset));
        preFilterOffset.setFilters(new InputFilter[]{new InputFilterMax(Long.valueOf(Constants.MAX_OFFSET))});

        //
        // apply default settings
        ((Spinner) getActivity().findViewById(R.id.preFilterTarget)).setSelection(1);
        if (RFIDController.singulationControl != null)
            ((Spinner) getActivity().findViewById(R.id.preFilterTarget)).setSelection(RFIDController.singulationControl.getSession().getValue());
        ((Spinner) getActivity().findViewById(R.id.preFilterAction)).setSelection(4);
        ((EditText) getActivity().findViewById(R.id.preFilterOffset)).setText("2");
        ((Spinner) getActivity().findViewById(R.id.preFilterMemoryBank)).setSelection(0);

        //Load the saved states
        loadPreFilterStates();
        //


        // Do not restore and enable setting if operations are running

    }

    /**
     * method to initialize memory bank spinner
     */
    private void initializeSpinner() {
        Spinner memoryBankSpinner = (Spinner) getActivity().findViewById(R.id.preFilterMemoryBank);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_memory_bank_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        memoryBankSpinner.setAdapter(memoryBankAdapter);

        Spinner actionSpinner = (Spinner) getActivity().findViewById(R.id.preFilterAction);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> actionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_action_array, R.layout.spinner_small_font);
        // Specify the layout to use when the list of choices appears
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        actionSpinner.setAdapter(actionAdapter);

        Spinner targetSpinner = (Spinner) getActivity().findViewById(R.id.preFilterTarget);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_target_options, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        targetSpinner.setAdapter(targetAdapter);
    }

    /**
     * Method to load the pre-filter states
     */
    private void loadPreFilterStates() {
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_memory_bank_array, R.layout.custom_spinner_layout);

        if (RFIDController.preFilters != null && RFIDController.preFilters[0] != null) {
            PreFilters preFilter = RFIDController.preFilters[0];
            if(RFIDController.asciiMode == true)
                ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).setText( hextoascii.convert(preFilter.getTag()));
            else
                ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).setText(preFilter.getTag());
            preFilterOffset.setText("" + preFilter.getOffset());
            ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).setChecked(preFilter.isFilterEnabled());
            ((Spinner) getActivity().findViewById(R.id.preFilterMemoryBank)).setSelection(memoryBankAdapter.getPosition(preFilter.getMemoryBank().trim().toUpperCase()));
            ((Spinner) getActivity().findViewById(R.id.preFilterAction)).setSelection(preFilter.getAction());
            ((Spinner) getActivity().findViewById(R.id.preFilterTarget)).setSelection(preFilter.getTarget());

        } else {
            ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).setText("");
            preFilterOffset.setText("0");
            ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).setChecked(false);
            ((Spinner) getActivity().findViewById(R.id.preFilterMemoryBank)).setSelection(0);
            ((Spinner) getActivity().findViewById(R.id.preFilterAction)).setSelection(0);
            ((Spinner) getActivity().findViewById(R.id.preFilterTarget)).setSelection(0);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static boolean setSingulation(boolean nonmatching, boolean restore) {
        Antennas.SingulationControl singulationControl;
        try {
            if(RFIDController.mConnectedReader == null || !RFIDController.mConnectedReader.isConnected() || !RFIDController.mConnectedReader.isCapabilitiesReceived())
                return false;
            singulationControl = RFIDController.mConnectedReader.Config.Antennas.getSingulationControl(1);
            if (nonmatching)
                singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
            else
                singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_B);
            // incase it is called because of disable of prefilter restore the singulation
            if(restore)
            {
                if(RFIDController.ActiveProfile.content.equals("Fastest Read"))
                    singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_AB_FLIP);
            }
            singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
            RFIDController.mConnectedReader.Config.Antennas.setSingulationControl(1, singulationControl);
            RFIDController.singulationControl = singulationControl;
        } catch (InvalidUsageException e) {
            e.printStackTrace();
            return false;
        } catch (OperationFailureException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
