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

import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.InputFilterMax;
import com.zebra.rfidreader.demo.common.PreFilters;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import static com.zebra.rfidreader.demo.home.MainActivity.filter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link PreFilter2ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to show the pre-filter 2 UI.
 */
public class PreFilter2ContentFragment extends Fragment {
    private EditText preFilterOffset;
    private CheckBox preFilter2EnableFilter;

    public PreFilter2ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PreFilter2ContentFragment.
     */
    public static PreFilter2ContentFragment newInstance() {
        return new PreFilter2ContentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pre_filter2_content, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeSpinner();
        AutoCompleteTextView tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID));
        RFIDController.getInstance().updateTagIDs();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.tagIDs);
        tagIDField.setAdapter(adapter);
        tagIDField.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});
        preFilterOffset = ((EditText) getActivity().findViewById(R.id.preFilter2Offset));
        preFilterOffset.setFilters(new InputFilter[]{new InputFilterMax(Long.valueOf(Constants.MAX_OFFSET))});
        preFilter2EnableFilter = (CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter);
        //Load the saved states
        loadPreFilterStates();

    }

    /**
     * method to initialize memory bank spinner
     */
    private void initializeSpinner() {

        Spinner memoryBankSpinner = (Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_memory_bank_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        memoryBankSpinner.setAdapter(memoryBankAdapter);

        Spinner actionSpinner = (Spinner) getActivity().findViewById(R.id.preFilter2Action);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> actionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_action_array, R.layout.spinner_small_font);
        // Specify the layout to use when the list of choices appears
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        actionSpinner.setAdapter(actionAdapter);

        Spinner targetSpinner = (Spinner) getActivity().findViewById(R.id.preFilter2Target);
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

        if (RFIDController.preFilters != null && RFIDController.preFilters[1] != null) {
            PreFilters preFilter = RFIDController.preFilters[1];
            ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).setText(preFilter.getTag());
            preFilterOffset.setText("" + preFilter.getOffset());
            ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).setChecked(preFilter.isFilterEnabled());
            ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).jumpDrawablesToCurrentState();
            ((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).setSelection(memoryBankAdapter.getPosition(preFilter.getMemoryBank().trim().toUpperCase()));
            ((Spinner) getActivity().findViewById(R.id.preFilter2Action)).setSelection(preFilter.getAction());
            ((Spinner) getActivity().findViewById(R.id.preFilter2Target)).setSelection(preFilter.getTarget());

        } else {
            ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).setText("");
            preFilterOffset.setText("0");
            ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).setChecked(false);
            ((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).setSelection(0);
            ((Spinner) getActivity().findViewById(R.id.preFilter2Action)).setSelection(0);
            ((Spinner) getActivity().findViewById(R.id.preFilter2Target)).setSelection(0);
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
