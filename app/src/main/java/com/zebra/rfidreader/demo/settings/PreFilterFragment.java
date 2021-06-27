package com.zebra.rfidreader.demo.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.zebra.rfid.api3.FILTER_ACTION;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.STATE_AWARE_ACTION;
import com.zebra.rfid.api3.TARGET;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.CustomProgressDialog;
import com.zebra.rfidreader.demo.common.PreFilters;
import com.zebra.rfidreader.demo.common.asciitohex;
import com.zebra.rfidreader.demo.common.hextoascii;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import static com.zebra.rfidreader.demo.home.MainActivity.filter;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isLocatingTag;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mIsInventoryRunning;
import static com.zebra.rfidreader.demo.settings.PreFilter1ContentFragment.setSingulation;

//import com.zebra.rfid.api3.PreFilters;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link PreFilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to act as a holder for individual pre-filter fragments
 */
public class PreFilterFragment extends BackPressedFragment {
    private static final String TAG = "PreFilterFragment";
    private static int prefilterIndex1 = 0, prefilterIndex2 = 0;
    private ViewPager viewPager;
    private com.zebra.rfid.api3.PreFilters.PreFilter preFilters1;
    private com.zebra.rfid.api3.PreFilters.PreFilter preFilters2;
    private boolean deletePrefilter1 = false, deletePrefilter2 = false;
    private int filterArraySize = 0;
    private int prefilterCombination = 0;/* value 1 means 1st prefilter,value 2 means 2nd prefilter,value 3 means both 1st and 2nd prefilter modified */
    private boolean showAdvancedOptions = false;
    private PreFilterAdapter mAdapter;
    private int start, stop;

    private boolean isPrefilterCheckBoxEnabled;

    public PreFilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PreFilterFragment.
     */
    public static PreFilterFragment newInstance() {
        return new PreFilterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_advanced_option, menu);
        super.onCreateOptionsMenu(menu, inflater);
        //
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        showAdvancedOptions = settings.getBoolean(Constants.PREFILTER_ADV_OPTIONS, false);
        UpdateViews();
    }

    private void UpdateViews() {
        if (showAdvancedOptions) {
            getActivity().findViewById(R.id.simplefilter).setVisibility(View.GONE);
            getActivity().findViewById(R.id.preFilterPager).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.simplefilter).setVisibility(View.VISIBLE);
            final AutoCompleteTextView tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagIDSimple));
            tagIDField.requestFocus();
            //tagIDField.setSelection(start, stop);
            tagIDField.dismissDropDown();
            getActivity().findViewById(R.id.preFilterPager).setVisibility(View.GONE);
        }
    }

    private void UpadatedAdvancedOption() {
        //viewPager.setAdvancedOptionEnabled(showAdvancedOptions);
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.PREFILTER_ADV_OPTIONS, showAdvancedOptions);
        editor.commit();
        UpdateViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_adv_op:
                if (showAdvancedOptions) {
                    if (CheckForSimpleFilterSwitch()) {
                        showAdvancedOptions = !showAdvancedOptions;
                        UpadatedAdvancedOption();
                        //  item.setIcon(R.drawable.icon_more);
                    } else
                        Toast.makeText(getActivity(), "Not compatible settings for Simple Prefilter", Toast.LENGTH_SHORT).show();
                } else {
                    showAdvancedOptions = !showAdvancedOptions;
                    UpadatedAdvancedOption();
                    // item.setIcon(R.drawable.icon_less);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean CheckForSimpleFilterSwitch() {

        if (RFIDController.preFilters == null)
            return true;
        if (RFIDController.preFilters != null && RFIDController.preFilters[1] != null)
            return false;
        if (RFIDController.preFilters[0] != null && (!RFIDController.preFilters[0].getMemoryBank().equals("EPC") || RFIDController.preFilters[0].getAction() != 4))
            return false;
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pre_filter, container, false);
    }

    private boolean canScroll() {
        if (!showAdvancedOptions) {
            return false;
        }
        return true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialization
        viewPager = (ViewPager) getActivity().findViewById(R.id.preFilterPager);

        viewPager.canScrollHorizontally(1);

        mAdapter = new PreFilterAdapter((getActivity()).getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        Log.d(TAG, "setPageChangeListener");

        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected()) {
            Log.d(TAG, "Reader connected");
            RFIDController.preFilters = new PreFilters[2];
            RFIDController.preFilters[0] = null;
            RFIDController.preFilters[1] = null;
            RFIDController.preFilterIndex = 0;
            deletePrefilter1 = false;
            deletePrefilter2 = false;

            try {
                filterArraySize = RFIDController.mConnectedReader.Actions.PreFilters.length();
                if (filterArraySize == 2) {
                    prefilterIndex1 = 0;
                    prefilterIndex2 = 1;

                    preFilters1 = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(prefilterIndex1);
                    preFilters2 = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(prefilterIndex2);
                } else if (filterArraySize == 1) {
                    prefilterIndex1 = 0;
                    preFilters1 = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(prefilterIndex1);
                }
                Log.d(TAG, "filter array size: " + filterArraySize);
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            }
        }
        if (preFilters1 != null) {
            Log.d(TAG, "Setting defaults for 1");
            String memoryBank = "EPC";
            if (preFilters1.getMemoryBank().toString().equalsIgnoreCase("MEMORY_BANK_EPC"))
                memoryBank = "EPC";
            else if (preFilters1.getMemoryBank().toString().equalsIgnoreCase("MEMORY_BANK_TID"))
                memoryBank = "TID";
            else if (preFilters1.getMemoryBank().toString().equalsIgnoreCase("MEMORY_BANK_USER"))
                memoryBank = "USER";
            else if (preFilters1.getMemoryBank().toString().equalsIgnoreCase("MEMORY_BANK_RESERVED"))
                memoryBank = "RESV";
            int target = 0;

            if (preFilters1.StateAwareAction.getTarget().getValue() == 1)
                target = 0;
            else if (preFilters1.StateAwareAction.getTarget().getValue() == 2)
                target = 1;
            else if (preFilters1.StateAwareAction.getTarget().getValue() == 3)
                target = 2;
            else if (preFilters1.StateAwareAction.getTarget().getValue() == 4)
                target = 3;
            else if (preFilters1.StateAwareAction.getTarget().getValue() == 0)
                target = 4;

            RFIDController.preFilters[0] = new PreFilters(preFilters1.getStringTagPattern(), memoryBank, preFilters1.getBitOffset() / 16, preFilters1.getTagPatternBitCount(), preFilters1.StateAwareAction.getStateAwareAction().getValue(),
                    target, true);

        }
        if (preFilters2 != null) {
            Log.d(TAG, "Setting defaults for 2");
            String memoryBank = "EPC";
            if (preFilters2.getMemoryBank().toString().equalsIgnoreCase("MEMORY_BANK_EPC"))
                memoryBank = "EPC";
            else if (preFilters2.getMemoryBank().toString().equalsIgnoreCase("MEMORY_BANK_TID"))
                memoryBank = "TID";
            else if (preFilters2.getMemoryBank().toString().equalsIgnoreCase("MEMORY_BANK_USER"))
                memoryBank = "USER";
            else if (preFilters2.getMemoryBank().toString().equalsIgnoreCase("MEMORY_BANK_RESERVED"))
                memoryBank = "RESV";
            int target = 0;

            if (preFilters2.StateAwareAction.getTarget().getValue() == 1)
                target = 0;
            else if (preFilters2.StateAwareAction.getTarget().getValue() == 2)
                target = 1;
            else if (preFilters2.StateAwareAction.getTarget().getValue() == 3)
                target = 2;
            else if (preFilters2.StateAwareAction.getTarget().getValue() == 4)
                target = 3;
            else if (preFilters2.StateAwareAction.getTarget().getValue() == 0)
                target = 4;
            RFIDController.preFilters[1] = new PreFilters(preFilters2.getStringTagPattern(), memoryBank, preFilters2.getBitOffset() / 16, preFilters2.getTagPatternBitCount(), preFilters2.StateAwareAction.getStateAwareAction().getValue(),
                    target, true);
        }

        //
        initializeSimplePrefilterView();
    }

    private void initializeSimplePrefilterView() {
        boolean bSelectID = false;
        final AutoCompleteTextView tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagIDSimple));
        RFIDController.getInstance().updateTagIDs();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.tagIDs);
        tagIDField.setAdapter(adapter);

        if (RFIDController.asciiMode == true)
            tagIDField.setFilters(new InputFilter[]{filter});
        else
            tagIDField.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});

        start = 0;
        if (RFIDController.preFilters != null && RFIDController.preFilters[0] != null) {
            if (RFIDController.preFilters[0].getMemoryBank().equals("EPC") && RFIDController.preFilters[0].getAction() == 4 && RFIDController.preFilters[1] == null) {
                PreFilters preFilter = RFIDController.preFilters[0];
                int offset1 = preFilters1.getBitOffset();
                String tag = preFilter.getTag();

                if (offset1 != 32) {
                    start = (offset1 - 32) / 4;
                }
                if (RFIDController.PreFilterTagID != null && !RFIDController.PreFilterTagID.equals("") && RFIDController.PreFilterTagID.toUpperCase().startsWith(tag.substring(0, tag.length() - 1), start)) {
                    tag = RFIDController.PreFilterTagID;
                    if (start <= tag.length())
                        bSelectID = true;
                }

                if (RFIDController.asciiMode == true)
                    tagIDField.setText(hextoascii.convert(tag));
                else
                    tagIDField.setText(tag);

                ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilterSimple)).setChecked(preFilter.isFilterEnabled());


            } else {
                showAdvancedOptions = true;
                UpadatedAdvancedOption();
            }
        } else if (Application.PreFilterTag != null) {
            if (RFIDController.asciiMode == true)
                tagIDField.setText(hextoascii.convert(Application.PreFilterTag));
            else
                tagIDField.setText((Application.PreFilterTag));
        }


        //tagIDField.setSelectAllOnFocus(true);
        int length = tagIDField.getText().length();

        SeekBar seekBarOffset = getActivity().findViewById(R.id.seekBar);
        SeekBar seekLength = getActivity().findViewById(R.id.seekBar2);

        seekBarOffset.setMax(length);
        seekBarOffset.setProgress(start);
        seekBarOffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                start = progress;

                if (tagIDField.getText().length() >= stop)
                    tagIDField.setSelection(start, stop);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tagIDField.requestFocus();
                seekBar.setMax(tagIDField.getText().length());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekLength.setMax(length);

        if (bSelectID) {
            String tagid = RFIDController.preFilters[0].getTag();
            stop = start + RFIDController.preFilters[0].getBitCount() / 4;//tagid.length();
            if ((RFIDController.PreFilterTagID + "0").endsWith(tagid))
                stop = RFIDController.PreFilterTagID.length();
            if (RFIDController.asciiMode == true && tagIDField.getText().toString().startsWith("'")) {
                //Converting hex values to ascci characters for each ascii value contains 2 digits of hexa value.
                //Incrementing 1 to start and stop position if the string is having singal quotes
                start /= 2;
                start += 1;
                stop /= 2;
                stop += 1;
            }
            tagIDField.requestFocus();
            tagIDField.setSelection(start, stop);
            tagIDField.dismissDropDown();
        } else {
            start = 0;
            stop = length;
        }
        seekBarOffset.setProgress(start);
        seekLength.setProgress(stop);
        seekLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                stop = progress;
                if (tagIDField.getText().length() >= stop)
                    tagIDField.setSelection(start, stop);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tagIDField.requestFocus();
                seekBar.setMax(tagIDField.getText().length());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //
        final CheckBox preFilterEnableNonMatchingFilterSimple = (CheckBox) getActivity().findViewById(R.id.preFilterEnableNonMatchingFilterSimple);
        preFilterEnableNonMatchingFilterSimple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.NON_MATCHING, isChecked);
                editor.commit();
                if (((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilterSimple)).isChecked()) {
                    if (!setSingulation(isChecked, false)) {
                        preFilterEnableNonMatchingFilterSimple.setChecked(!isChecked);
                        editor.putBoolean(Constants.NON_MATCHING, !isChecked);
                        editor.commit();
                    }
                }
            }
        });

        CheckBox preFilterEnableFilterSimple = (CheckBox) getActivity().findViewById(R.id.preFilterEnableFilterSimple);

        preFilterEnableFilterSimple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    // clear singulation
                    setSingulation(true, true);
                    isPrefilterCheckBoxEnabled = false;

                } else {
                    setSingulation(((CheckBox) getActivity().findViewById(R.id.preFilterEnableNonMatchingFilterSimple)).isChecked(), false);

                    isPrefilterCheckBoxEnabled = true;
                }
            }
        });

        //
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);

        // Do not restore and enable setting if operations are running
        boolean operationRunning = mIsInventoryRunning | isLocatingTag;
        if (!operationRunning)
            preFilterEnableNonMatchingFilterSimple.setChecked(settings.getBoolean(Constants.NON_MATCHING, false));
        preFilterEnableNonMatchingFilterSimple.setEnabled(!operationRunning);

    }

    private View getFragmentView(int index) {
        return viewPager.getChildAt(index);
    }

    /**
     * method to know whether pre filter settings has changed on back press of the fragment
     *
     * @return true if settings has changed or false if settings has not changed
     */
    public boolean issettingsChanged() {

        if (getActivity().findViewById(R.id.simplefilter).getVisibility() == View.VISIBLE) {
            AutoCompleteTextView tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagIDSimple));
            SeekBar seekLength = getActivity().findViewById(R.id.seekBar2);
            SeekBar seekBarOffset = getActivity().findViewById(R.id.seekBar);
            int offset = 0;
            int length = tagIDField.getText().length();
            if (tagIDField.hasSelection()) {
                offset = seekBarOffset.getProgress();
                length = seekLength.getProgress();

                int min = 0;
                int max = 0;

                min = Math.max(0, Math.min(offset, length));
                max = Math.max(0, Math.max(offset, length));
                offset = min;
                length = max;
            }
            if (RFIDController.preFilters == null)
                return false;
            if ((RFIDController.preFilters[0] == null && ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilterSimple)).isChecked())) {
                prefilterCombination = 1;
                return true;
            } else if ((RFIDController.preFilters[0] != null && !((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilterSimple)).isChecked())) {
                prefilterCombination = 1;
                deletePrefilter1 = true;
                return true;
            } else if ((RFIDController.preFilters[0] != null) && ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilterSimple)).isChecked()) {
                String tagID = RFIDController.asciiMode == true ? AsciitohexConvert(tagIDField.getText().toString(), offset, length) : tagIDField.getText().toString().substring(offset, length);
//                if (!tagIDField.hasSelection()) {
//                    offset = (RFIDController.preFilters[0].getOffset() * 16 - 32) / 4;
//                }
                if (RFIDController.preFilters[0].getTag().length() * 4 != RFIDController.preFilters[0].getBitCount()) {
                    tagID = (tagID + "0");
                }
                if (RFIDController.asciiMode && tagIDField.getText().toString().startsWith("'")) {
                    if (offset != 0)
                        offset -= 1;
                    offset *= 2;
                }
                if (RFIDController.PreFilterTagID == null || RFIDController.PreFilterTagID.equals(""))
                    offset = (RFIDController.preFilters[0].getOffset() - 2) * 4;
                PreFilters preFilterCurrent = new PreFilters(tagID,
                        "EPC",
                        (offset * 4 + 32) / 16,
                        0, 4,
                        RFIDController.singulationControl.getSession().getValue(), true);
                if (!preFilterCurrent.equals(RFIDController.preFilters[0])) {
                    prefilterCombination = 1;
                    return true;
                }
            }
        } else {

            deletePrefilter1 = false;
            deletePrefilter2 = false;
            CheckBox preFilterEnableFilter = ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter));
            CheckBox preFilter2EnableFilter = ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter));
            if (preFilter2EnableFilter != null && preFilterEnableFilter != null) {
                if (RFIDController.preFilters == null)
                    return false;
                if (RFIDController.preFilters[0] == null && RFIDController.preFilters[1] == null && !preFilterEnableFilter.isChecked() && !preFilter2EnableFilter.isChecked())
                    return false;
                else if ((RFIDController.preFilters[0] == null && ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked()) && (!((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked()) && RFIDController.preFilters[1] == null) {
                    prefilterCombination = 1;
                    return true;
                } else if ((RFIDController.preFilters[0] == null && !((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked()) && (((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked()) && RFIDController.preFilters[1] == null) {
                    prefilterCombination = 2;
                    return true;
                } else if ((RFIDController.preFilters[0] == null && ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked()) || (((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked()) && RFIDController.preFilters[1] == null) {
                    prefilterCombination = 3;
                    return true;
                } else if ((RFIDController.preFilters[0] != null && RFIDController.preFilters[1] == null)) {
                    if ((((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked()) && !(((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked())) {
                        int offset = ((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString().isEmpty() ? -1 : Integer.parseInt(((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString());
                        PreFilters preFilterCurrent = new PreFilters(((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).getText().toString(), ((Spinner) getActivity().findViewById(R.id.preFilterMemoryBank)).getSelectedItem().toString(), offset,
                                0, ((Spinner) getActivity().findViewById(R.id.preFilterAction)).getSelectedItemPosition(), ((Spinner) getActivity().findViewById(R.id.preFilterTarget)).getSelectedItemPosition(), true);

                        if (!preFilterCurrent.equals(RFIDController.preFilters[0])) {
                            prefilterCombination = 1;
                            return true;
                        }
                    } else if ((((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked()) && (((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked())) {
                        int offset = ((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString().isEmpty() ? -1 : Integer.parseInt(((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString());
                        PreFilters preFilterCurrent = new PreFilters(((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).getText().toString(), ((Spinner) getActivity().findViewById(R.id.preFilterMemoryBank)).getSelectedItem().toString(), offset,
                                0, ((Spinner) getActivity().findViewById(R.id.preFilterAction)).getSelectedItemPosition(), ((Spinner) getActivity().findViewById(R.id.preFilterTarget)).getSelectedItemPosition(), true);

                        if (!preFilterCurrent.equals(RFIDController.preFilters[0])) {
                            prefilterCombination = 3;
                            return true;
                        }
                    } else {
                        prefilterCombination = 1;
                        deletePrefilter1 = true;
                        return true;
                    }
                } else if ((RFIDController.preFilters[0] == null && RFIDController.preFilters[1] != null)) {
                    if ((((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked())) {
                        int offset = ((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString().isEmpty() ? -1 : Integer.parseInt(((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString());
                        PreFilters preFilterCurrent = new PreFilters(((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).getText().toString(), ((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).getSelectedItem().toString(), offset,
                                0, ((Spinner) getActivity().findViewById(R.id.preFilter2Action)).getSelectedItemPosition(), ((Spinner) getActivity().findViewById(R.id.preFilter2Target)).getSelectedItemPosition(), true);

                        if (!preFilterCurrent.equals(RFIDController.preFilters[1])) {
                            prefilterCombination = 2;
                            return true;
                        }
                    } else if ((((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked()) && (((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked())) {
                        int offset = ((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString().isEmpty() ? -1 : Integer.parseInt(((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString());
                        PreFilters preFilterCurrent = new PreFilters(((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).getText().toString(), ((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).getSelectedItem().toString(), offset,
                                0, ((Spinner) getActivity().findViewById(R.id.preFilter2Action)).getSelectedItemPosition(), ((Spinner) getActivity().findViewById(R.id.preFilter2Target)).getSelectedItemPosition(), true);
                        if (!preFilterCurrent.equals(RFIDController.preFilters[1])) {
                            prefilterCombination = 3;
                            return true;
                        }
                    } else {
                        prefilterCombination = 2;
                        deletePrefilter2 = true;
                        return true;
                    }
                } else if ((RFIDController.preFilters[0] != null && RFIDController.preFilters[1] != null)) {
                    if (!(((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked()) && (((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked())) {
                        int offset = ((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString().isEmpty() ? -1 : Integer.parseInt(((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString());
                        PreFilters preFilterCurrent = new PreFilters(((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).getText().toString(), ((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).getSelectedItem().toString(), offset,
                                0, ((Spinner) getActivity().findViewById(R.id.preFilter2Action)).getSelectedItemPosition(), ((Spinner) getActivity().findViewById(R.id.preFilter2Target)).getSelectedItemPosition(), true);


                        if (!preFilterCurrent.equals(RFIDController.preFilters[1])) {
                            prefilterCombination = 2;
                        } else
                            prefilterCombination = 1;

                        deletePrefilter1 = true;
                        return true;
                    } else if ((((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked()) && !(((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked())) {
                        int offset = ((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString().isEmpty() ? -1 : Integer.parseInt(((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString());
                        PreFilters preFilterCurrent = new PreFilters(((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).getText().toString(), ((Spinner) getActivity().findViewById(R.id.preFilterMemoryBank)).getSelectedItem().toString(), offset,
                                0, ((Spinner) getActivity().findViewById(R.id.preFilterAction)).getSelectedItemPosition(), ((Spinner) getActivity().findViewById(R.id.preFilterTarget)).getSelectedItemPosition(), true);

                        if (!preFilterCurrent.equals(RFIDController.preFilters[0])) {
                            prefilterCombination = 1;
                        } else
                            prefilterCombination = 2;
                        deletePrefilter2 = true;
                        return true;
                    } else if (((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked() && ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked()) {
                        int offset = ((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString().isEmpty() ? -1 : Integer.parseInt(((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString());
                        int offset2 = ((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString().isEmpty() ? -1 : Integer.parseInt(((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString());

                        PreFilters preFilterCurrent1 = new PreFilters(((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).getText().toString(), ((Spinner) getActivity().findViewById(R.id.preFilterMemoryBank)).getSelectedItem().toString(), offset,
                                0, ((Spinner) getActivity().findViewById(R.id.preFilterAction)).getSelectedItemPosition(), ((Spinner) getActivity().findViewById(R.id.preFilterTarget)).getSelectedItemPosition(), true);
                        PreFilters preFilterCurrent2 = new PreFilters(((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).getText().toString(), ((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).getSelectedItem().toString(), offset2,
                                0, ((Spinner) getActivity().findViewById(R.id.preFilter2Action)).getSelectedItemPosition(), ((Spinner) getActivity().findViewById(R.id.preFilter2Target)).getSelectedItemPosition(), true);

                        if (!preFilterCurrent1.equals(RFIDController.preFilters[0]) && preFilterCurrent2.equals(RFIDController.preFilters[1])) {
                            prefilterCombination = 3;
                            return true;
                        }
                        if (preFilterCurrent1.equals(RFIDController.preFilters[0]) && !preFilterCurrent2.equals(RFIDController.preFilters[1])) {
                            prefilterCombination = 3;
                            return true;
                        }
                        if (!preFilterCurrent1.equals(RFIDController.preFilters[0]) && !preFilterCurrent2.equals(RFIDController.preFilters[1])) {
                            prefilterCombination = 3;
                            return true;
                        }
                    } else if (!((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter)).isChecked() && !((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).isChecked()) {
                        deletePrefilter1 = true;
                        deletePrefilter2 = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void fillPrefilter(com.zebra.rfid.api3.PreFilters.PreFilter preFilterPassed1, com.zebra.rfid.api3.PreFilters.PreFilter preFilterPassed2) {
        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected()) {

            if (getActivity().findViewById(R.id.simplefilter).getVisibility() == View.VISIBLE) {
                AutoCompleteTextView tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagIDSimple));
                SeekBar seekLength = getActivity().findViewById(R.id.seekBar2);
                SeekBar seekBarOffset = getActivity().findViewById(R.id.seekBar);
                int offset = 0;
                int length = tagIDField.getText().length();
                if (tagIDField.hasSelection()) {
                    offset = seekBarOffset.getProgress();
                    length = seekLength.getProgress();
                    int min = 0;
                    int max = 0;

                    min = Math.max(0, Math.min(offset, length));
                    max = Math.max(0, Math.max(offset, length));
                    //min value is offset and max value is length
                    offset = min;
                    length = max;
                }
                if (RFIDController.asciiMode == true)
                    putTagPattern(preFilterPassed1, RFIDController.preFilters[0], AsciitohexConvert(tagIDField.getText().toString(), offset, length));
                else
                    putTagPattern(preFilterPassed1, RFIDController.preFilters[0], (tagIDField.getText().toString().substring(offset, length)));
                if (RFIDController.asciiMode == true && tagIDField.getText().toString().startsWith("'") && tagIDField.getText().toString().endsWith("'")) {
                    if (tagIDField.hasSelection()) {
                        //if the tagid is having single quotes on starting and ending length and offset are decreasing 1 as we are not converting single quotes into bits
                        if (offset != 0)
                            offset -= 1;
                        if (tagIDField.getSelectionEnd() == tagIDField.getText().toString().length())
                            length -= 2;
                        else
                            length -= 1;
                    } else if (offset == 0 && length == stop) {
                        //if offset value is 0 and length is end value length is decrementing by 2
                        length -= 2;
                    }
                    //multiplying by 2 both offset and length values for converting ascii to hexa values
                    offset *= 2;
                    length *= 2;

                }
                putOffSet(preFilterPassed1, RFIDController.preFilters[0], offset * 4 + 32);
                preFilterPassed1.setTagPatternBitCount((length - offset) * 4);
                putMemoryBank(preFilterPassed1, RFIDController.preFilters[0], MEMORY_BANK.MEMORY_BANK_EPC);
                preFilterPassed1.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE);

                putTarget(preFilterPassed1, RFIDController.preFilters[0], GetTargetForSession());
                putAction(preFilterPassed1, RFIDController.preFilters[0], STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);

            } else {

                CheckBox enablePreFilter = ((CheckBox) getActivity().findViewById(R.id.preFilterEnableFilter));
                CheckBox enablePreFilter2 = ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter));
                String offsetText = ((EditText) getActivity().findViewById(R.id.preFilterOffset)).getText().toString();
                String offsetText2 = ((EditText) getActivity().findViewById(R.id.preFilter2Offset)).getText().toString();
                if ((enablePreFilter.isChecked() && offsetText.isEmpty()) || (enablePreFilter2.isChecked() && offsetText2.isEmpty())) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), getResources().getString(R.string.status_failure_message) + "\n" + getResources().getString(R.string.error_empty_fields_preFilters), Toast.LENGTH_SHORT).show();
                        }
                    });
                    ((MainActivity) getActivity()).callBackPressed();
                } else {
                    if (enablePreFilter.isChecked()) {
                        Constants.logAsMessage(Constants.TYPE_DEBUG, "PreFilter1TagPattern", ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).getText().toString());

                        String data = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagID)).getText().toString();
                        if (RFIDController.asciiMode == true)
                            data = asciitohex.convert(data);

                        int offset = Integer.parseInt(offsetText);
                        putTagPattern(preFilterPassed1, RFIDController.preFilters[0], data);
                        putOffSet(preFilterPassed1, RFIDController.preFilters[0], offset * Constants.NO_OF_BITS);
                        preFilterPassed1.setTagPatternBitCount(data.length() * 4);

                        MEMORY_BANK memBank = getMemoryBankFromString(((Spinner) getActivity().findViewById(R.id.preFilterMemoryBank)).getSelectedItem().toString());
                        if (memBank != null) {
                            putMemoryBank(preFilterPassed1, RFIDController.preFilters[0], memBank);
                        }

                        preFilterPassed1.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE);


                        TARGET target = getTargetFromString(((Spinner) getActivity().findViewById(R.id.preFilterTarget)).getSelectedItem().toString());


                        if (target != null) {
                            putTarget(preFilterPassed1, RFIDController.preFilters[0], target);
                        }

                        STATE_AWARE_ACTION action = getStateAwareActionFromString(((Spinner) getActivity().findViewById(R.id.preFilterAction)).getSelectedItem().toString());
                        if (action != null) {
                            putAction(preFilterPassed1, RFIDController.preFilters[0], action);
                        }
                    }

                    if (enablePreFilter2.isChecked()) {
                        Constants.logAsMessage(Constants.TYPE_DEBUG, "PreFilter2TagPattern", ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).getText().toString());
                        String data = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).getText().toString();
                        int offset = Integer.parseInt(offsetText2);
                        putTagPattern(preFilterPassed2, RFIDController.preFilters[1], data);
                        putOffSet(preFilterPassed2, RFIDController.preFilters[1], offset * Constants.NO_OF_BITS);
                        preFilterPassed2.setTagPatternBitCount(data.length() * 4);

                        MEMORY_BANK memBank = getMemoryBankFromString(((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).getSelectedItem().toString());
                        if (memBank != null) {
                            putMemoryBank(preFilterPassed2, RFIDController.preFilters[1], memBank);
                        }

                        preFilterPassed2.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE);

                        TARGET target = getTargetFromString(((Spinner) getActivity().findViewById(R.id.preFilter2Target)).getSelectedItem().toString());
                        if (target != null) {
                            putTarget(preFilterPassed2, RFIDController.preFilters[1], target);
                        }

                        STATE_AWARE_ACTION action = getStateAwareActionFromString(((Spinner) getActivity().findViewById(R.id.preFilter2Action)).getSelectedItem().toString());
                        if (action != null) {
                            putAction(preFilterPassed2, RFIDController.preFilters[1], action);
                        }
                    }
                }
            }
        }
    }

    private TARGET GetTargetForSession() {
        TARGET target = TARGET.TARGET_INVENTORIED_STATE_S0;
        if (RFIDController.singulationControl != null) {
            SESSION session = RFIDController.singulationControl.getSession();
            if (session == SESSION.SESSION_S1)
                target = TARGET.TARGET_INVENTORIED_STATE_S1;
            else if (session == SESSION.SESSION_S2)
                target = TARGET.TARGET_INVENTORIED_STATE_S2;
            else if (session == SESSION.SESSION_S3)
                target = TARGET.TARGET_INVENTORIED_STATE_S3;
        }
        return target;
    }

    private MEMORY_BANK getMemoryBankFromString(String s) {
        MEMORY_BANK bank = null;
        if (s.equalsIgnoreCase("EPC"))
            bank = MEMORY_BANK.MEMORY_BANK_EPC;
        if (s.equalsIgnoreCase("TID"))
            bank = MEMORY_BANK.MEMORY_BANK_TID;
        if (s.equalsIgnoreCase("USER"))
            bank = MEMORY_BANK.MEMORY_BANK_USER;
        return bank;
    }

    private TARGET getTargetFromString(String s) {
        TARGET target = null;
        if (s.equalsIgnoreCase("SESSION S0"))
            target = TARGET.TARGET_INVENTORIED_STATE_S0;
        if (s.equalsIgnoreCase("SESSION S1"))
            target = TARGET.TARGET_INVENTORIED_STATE_S1;
        if (s.equalsIgnoreCase("SESSION S2"))
            target = TARGET.TARGET_INVENTORIED_STATE_S2;
        if (s.equalsIgnoreCase("SESSION S3"))
            target = TARGET.TARGET_INVENTORIED_STATE_S3;
        if (s.equalsIgnoreCase("SL FLAG"))
            target = TARGET.TARGET_SL;
        return target;
    }

    private STATE_AWARE_ACTION getStateAwareActionFromString(String strAction) {
        STATE_AWARE_ACTION action = null;
        if (strAction.equalsIgnoreCase("INV A NOT INV B OR ASRT SL NOT DSRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A_NOT_INV_B;
        if (strAction.equalsIgnoreCase("INV A OR ASRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A;
        if (strAction.equalsIgnoreCase("NOT INV B OR NOT DSRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_B;
        if (strAction.equalsIgnoreCase("INV A2BB2A NOT INV A OR NEG SL NOT ASRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A2BB2A_NOT_INV_A;
        if (strAction.equalsIgnoreCase("INV B NOT INV A OR DSRT SL NOT ASRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A;
        if (strAction.equalsIgnoreCase("INV B OR DSRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B;
        if (strAction.equalsIgnoreCase("NOT INV A OR NOT ASRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_A;
        if (strAction.equalsIgnoreCase("NOT INV A2BB2A OR NOT NEG SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_A2BB2A;
        return action;
    }

    private void putTagPattern(com.zebra.rfid.api3.PreFilters.PreFilter preFilter, PreFilters demoFilter, String tagPattern) {
        preFilter.setTagPattern(tagPattern);
        if (demoFilter != null)
            demoFilter.setTag(tagPattern);
        Log.d(TAG, "Tag Pattern: " + tagPattern);
    }

    private void putOffSet(com.zebra.rfid.api3.PreFilters.PreFilter preFilter, PreFilters demoFilter, int offset) {
        preFilter.setBitOffset(offset);
        if (demoFilter != null)
            demoFilter.setOffset(offset);
        Log.d(TAG, "Offset: " + offset);
    }

    private void putMemoryBank(com.zebra.rfid.api3.PreFilters.PreFilter preFilter, PreFilters demoFilter, MEMORY_BANK memBank) {
        preFilter.setMemoryBank(memBank);
        if (demoFilter != null)
            demoFilter.setMemoryBank(memBank.toString());
        Log.d(TAG, "Memory bank: " + memBank.toString());
    }

    private void putTarget(com.zebra.rfid.api3.PreFilters.PreFilter preFilter, PreFilters demoFilter, TARGET target) {
        preFilter.StateAwareAction.setTarget(target);
        if (demoFilter != null)
            demoFilter.setTarget(target.ordinal);
        Log.d(TAG, "Target: " + target.ordinal);
    }

    private void putAction(com.zebra.rfid.api3.PreFilters.PreFilter preFilter, PreFilters demoFilter, STATE_AWARE_ACTION action) {
        preFilter.StateAwareAction.setStateAwareAction(action);
        if (demoFilter != null)
            demoFilter.setAction(action.ordinal);
        Log.d(TAG, "Action: " + action.ordinal);
    }

    private String AsciitohexConvert(String id, int offset, int length) {
        String tempID;
        if (id.startsWith("'") && id.endsWith("'")) {
            tempID = id.substring(offset, length);
            if (!tempID.startsWith("'"))
                tempID = "'" + tempID;
            if (!tempID.endsWith("'"))
                tempID = tempID + "'";
            return asciitohex.convert(tempID);
        }
        return id.substring(offset, length);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    /**
     * Method to be called when back button is pressed by the user
     */
    @Override
    public void onBackPressed() {
        Constants.logAsMessage(Constants.TYPE_DEBUG, "PreFilterFragment", "Back Pressed called in Pre Filter fragment");
        if (issettingsChanged())
            new Task_SavePrefilter().execute();
        else
            ((MainActivity) getActivity()).callBackPressed();
    }

    private class Task_SavePrefilter extends AsyncTask<Void, Void, Boolean> {
        private com.zebra.rfid.api3.PreFilters.PreFilter PreFilterData1 = RFIDController.mConnectedReader.Actions.PreFilters.new PreFilter();
        private com.zebra.rfid.api3.PreFilters.PreFilter PreFilterData2 = RFIDController.mConnectedReader.Actions.PreFilters.new PreFilter();
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;
        private CustomProgressDialog progressDialog;
        // so sometimes this gets called after fragment has detached :(
        private Activity activity;

        public Task_SavePrefilter() {

        }

        @Override
        protected void onPreExecute() {
            activity = getActivity();
            progressDialog = new CustomProgressDialog(activity, getString(R.string.pre_filter));
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null && activity != null)
                        progressDialog.show();
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean bResult = false;
            fillPrefilter(PreFilterData1, PreFilterData2);

            try {
                Log.d(TAG, "SavePrefilter.doInBackground()");
                // Case when only 1st prefilter is to be modified
                if ((prefilterCombination == 1) && !deletePrefilter1 && !deletePrefilter2) {
                    if (filterArraySize > 0) {
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(prefilterIndex1);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    RFIDController.mConnectedReader.Actions.PreFilters.add(PreFilterData1);
                    RFIDController.preFilters[0] = new PreFilters(PreFilterData1);
                    // Case when only 2nd prefilter is to be modified
                } else if ((prefilterCombination == 2) && !deletePrefilter1 && !deletePrefilter2) {
                    if (filterArraySize > 1) {
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(prefilterIndex2);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    RFIDController.mConnectedReader.Actions.PreFilters.add(PreFilterData2);
                    RFIDController.preFilters[1] = new PreFilters(PreFilterData2);

                    // Case when both prefilters are modified
                } else if ((prefilterCombination == 3) && !deletePrefilter1 && !deletePrefilter2) {
                    if (filterArraySize > 0) {
                        filterArraySize--;
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    if (filterArraySize > 0) {
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    RFIDController.mConnectedReader.Actions.PreFilters.add(PreFilterData1);
                    RFIDController.preFilters[0] = new PreFilters(PreFilterData1);
                    RFIDController.mConnectedReader.Actions.PreFilters.add(PreFilterData2);
                    RFIDController.preFilters[1] = new PreFilters(PreFilterData2);

                    // Case when prefilter 1 is deleted while prefilter2 is modified
                } else if ((prefilterCombination == 2) && deletePrefilter1) {
                    if (filterArraySize > 0) {
                        filterArraySize--;
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    if (filterArraySize > 0) {
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    RFIDController.mConnectedReader.Actions.PreFilters.add(PreFilterData2);
                    RFIDController.preFilters[1] = new PreFilters(PreFilterData2);

                    // Case when prefilter 2 is deleted while prefilter1 is modified
                } else if ((prefilterCombination == 1) && deletePrefilter2) {
                    if (filterArraySize > 0) {
                        filterArraySize--;
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    if (filterArraySize > 0) {
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    RFIDController.mConnectedReader.Actions.PreFilters.add(PreFilterData1);
                    RFIDController.preFilters[0] = new PreFilters(PreFilterData1);

                    //Case when prefilter1 is deleted
                } else if ((prefilterCombination == 1) && deletePrefilter1) {
                    if (filterArraySize > 0) {
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(prefilterIndex1);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                        // clear singulation
                        setSingulation(true, true);
                    }
                    prefilterIndex2 = prefilterIndex1;
                    //Case when prefilter2 is deleted
                } else if ((prefilterCombination == 2) && deletePrefilter2) {
                    if (filterArraySize > 1) {
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(prefilterIndex2);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    prefilterIndex2--;
                    //Case when both prefilters are deleted
                } else if (deletePrefilter1 && deletePrefilter2) {
                   /* if (filterArraySize > 0) {
                        filterArraySize--;
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }
                    if (filterArraySize > 0) {
                        com.zebra.rfid.api3.PreFilters.PreFilter toBeDeleted = RFIDController.mConnectedReader.Actions.PreFilters.getPreFilter(0);
                        if (toBeDeleted != null)
                            RFIDController.mConnectedReader.Actions.PreFilters.delete(toBeDeleted);
                    }*/
                    RFIDController.mConnectedReader.Actions.PreFilters.deleteAll();
                }
                bResult = true;

            } catch (InvalidUsageException e) {
                Log.e(TAG, "Invalid Usage Exception", e);
                deletePrefilter1 = false;
                deletePrefilter2 = false;
                invalidUsageException = e;
            } catch (OperationFailureException e) {
                Log.e(TAG, "Operation Failure Exception", e);
                deletePrefilter1 = false;
                deletePrefilter2 = false;
                operationFailureException = e;
            }
            return bResult;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.cancel();

            if (getActivity() == null)
                return;

            boolean isSimpleFilter = getActivity().findViewById(R.id.simplefilter).getVisibility() == View.VISIBLE;
            if (isSimpleFilter) {
                final AutoCompleteTextView tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilterTagIDSimple));
                RFIDController.PreFilterTagID = RFIDController.asciiMode == true ? asciitohex.convert(tagIDField.getText().toString()) : tagIDField.getText().toString();
            }
            if (!result) {
                if (invalidUsageException != null)
                    ((MainActivity) activity).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                if (operationFailureException != null)
                    ((MainActivity) activity).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
                // clear singulation
                setSingulation(true, true);


            } else {
                Toast.makeText(activity, R.string.status_success_message, Toast.LENGTH_SHORT).show();

              /*  isPreFilterSimpleEnabled = isPrefilterCheckBoxEnabled;
                isPreFilterAdvanceEnabled = !isSimpleFilter;

                SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.PREFILTER_SIMPLE_ENABLED, isPreFilterSimpleEnabled);
                editor.putBoolean(Constants.PREFILTER_ADVANCE_ENABLED, isPreFilterAdvanceEnabled);
                editor.commit();*/

            }
            super.onPostExecute(result);
            ((MainActivity) activity).callBackPressed();


        }
    }
}