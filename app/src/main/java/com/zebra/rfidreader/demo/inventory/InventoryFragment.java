package com.zebra.rfidreader.demo.inventory;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.rfid.RFIDController;
import com.zebra.rfidreader.demo.settings.ISettingsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static com.zebra.rfidreader.demo.common.Constants.TYPE_DEBUG;
import static com.zebra.rfidreader.demo.common.Constants.logAsMessage;
import static com.zebra.rfidreader.demo.rfid.RFIDController.ActiveProfile;
import static com.zebra.rfidreader.demo.rfid.RFIDController.TAG;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isInventoryAborted;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link InventoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle inventory operations and UI.
 */
public class InventoryFragment extends Fragment implements Spinner.OnItemSelectedListener, ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.BatchModeEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler,
        View.OnClickListener {
    private static final int TAGLIST_MATCH_MODE_IMPORT = 0;
    TextView totalNoOfTags;     //total Tags read
    TextView uniqueTags;        //number uniqueTags
    TextView uniqueTagsTitle;   //title of unique Tags
    TextView totalReads;        //title of total Tags
    LinearLayout inventoryHeaderRow; //row of title Name, Count, RSSI
    TextView rssiColumnHeader;  //title RSSI
    private ListView listView;  //list information of Tag
    private ModifiedInventoryAdapter adapter; //format information of Tag -> show on listView
    private ArrayAdapter<CharSequence> invAdapter;

    //ID to maintain the memory bank selected
    private String memoryBankID = "none";
    private FloatingActionButton inventoryButton;
    private FloatingActionButton fabMatchMode;
    private FloatingActionButton fabReset;

    private long prevTime = 0;
    private TextView timeText;
    private Spinner invSpinner;
    private TextView batchModeInventoryList;
    private ISettingsUtil settingsUtil;

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            if (!RFIDController.mIsInventoryRunning) {
                toggle(view, position);
                RFIDController.accessControlTag = adapter.getItem(position).getTagID();
                Application.locateTag = adapter.getItem(position).getTagID();
                Application.PreFilterTag = adapter.getItem(position).getTagID();
            }
        }
    };

    public InventoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InventoryFragment.
     */
    public static InventoryFragment newInstance() {
        return new InventoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        settingsUtil = (MainActivity) getActivity();
        if (Application.TAG_LIST_MATCH_MODE) {
            settingsUtil.LoadTagListCSV();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);
        //
        menu.findItem(R.id.action_dpo).setVisible(false);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.getFilter().filter(s);
                    }
                });
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                ((MainActivity) getActivity()).selectItem(3);
                break;
            case R.id.action_read_write:
                ((MainActivity) getActivity()).selectItem(5);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public ModifiedInventoryAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        activity.disableScanner();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        totalNoOfTags = (TextView) getActivity().findViewById(R.id.inventoryCountText);
        uniqueTags = (TextView) getActivity().findViewById(R.id.inventoryUniqueText);
        uniqueTagsTitle = (TextView) getActivity().findViewById(R.id.uniqueTags);
        totalReads = (TextView) getActivity().findViewById(R.id.totalReads);
        inventoryHeaderRow = (LinearLayout) getActivity().findViewById(R.id.inventoryHeaderRow);
        rssiColumnHeader = (TextView) getActivity().findViewById(R.id.rssiColumnHeader);
        rssiColumnHeader.setVisibility(View.GONE);
        if (RFIDController.tagStorageSettings != null) {
            for (TAG_FIELD tag_field : RFIDController.tagStorageSettings.getTagFields()) {
                if (tag_field == TAG_FIELD.PEAK_RSSI)
                    rssiColumnHeader.setVisibility(View.VISIBLE);
            }
        }
        if (Application.TAG_LIST_MATCH_MODE) {
            totalNoOfTags.setText(String.valueOf(Application.missedTags));
            uniqueTags.setText(String.valueOf(Application.matchingTags));
        } else {
            if (totalNoOfTags != null)
                totalNoOfTags.setText(String.valueOf(Application.TOTAL_TAGS));
            if (uniqueTags != null)
                uniqueTags.setText(String.valueOf(Application.UNIQUE_TAGS));
        }
        invSpinner = (Spinner) getActivity().findViewById(R.id.inventoryOptions);
        if (Application.TAG_LIST_MATCH_MODE && Application.TAG_LIST_FILE_EXISTS) {
            uniqueTagsTitle.setText("MATCHING TAGS");
            totalReads.setText("MISSED TAGS");
            ((TextView) getActivity().findViewById(R.id.inventorySpinnerText)).setText("TAG LIST");
            invAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inv_menu_items_for_matching_tags, R.layout.spinner_small_font);

        } else {
            // Create an ArrayAdapter using the string array and a default spinner layout
            invAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inv_menu_items, R.layout.spinner_small_font);
        }
        // Specify the layout to use when the list of choices appears
        invAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        invSpinner.setAdapter(invAdapter);
        if (Application.memoryBankId != -1 && Application.memoryBankId < invAdapter.getCount())
            invSpinner.setSelection(Application.memoryBankId);
        invSpinner.setOnItemSelectedListener(this);
        if (RFIDController.mIsInventoryRunning) {
            invSpinner.setEnabled(false);
        }
        inventoryButton = (FloatingActionButton) getActivity().findViewById(R.id.inventoryButton);
        if (inventoryButton != null) {
            if (RFIDController.mIsInventoryRunning) {
                inventoryButton.setImageResource(R.drawable.ic_play_stop);
                if (Application.TAG_LIST_MATCH_MODE)
                    totalNoOfTags.setText("0");
            }
        }
        //Set the font size in constants
        Constants.INVENTORY_LIST_FONT_SIZE = (int) getResources().getDimension(R.dimen.inventory_list_font_size);
        batchModeInventoryList = (TextView) getActivity().findViewById(R.id.batchModeInventoryList);
        listView = (ListView) getActivity().findViewById(R.id.inventoryList);
        adapter = new ModifiedInventoryAdapter(getActivity(), R.layout.inventory_list_item);
        //enables filtering for the contents of the given ListView
        listView.setTextFilterEnabled(true);
        if (RFIDController.isBatchModeInventoryRunning != null && RFIDController.isBatchModeInventoryRunning) {
            listView.setEmptyView(batchModeInventoryList);
            batchModeInventoryList.setVisibility(View.VISIBLE);
        } else {
            listView.setAdapter(adapter);
            batchModeInventoryList.setVisibility(View.GONE);
        }
        listView.setOnItemClickListener(onItemClickListener);
        adapter.notifyDataSetChanged();

        //  getActivity().findViewById(R.id.fab_prefilter).setVisibility(isPreFilterSimpleEnabled ? View.VISIBLE : View.GONE);
        getActivity().findViewById(R.id.tv_prefilter_enabled).setVisibility(
                RFIDController.getInstance().isPrefilterEnabled() ? View.VISIBLE : View.INVISIBLE);

        fabReset = (FloatingActionButton) getActivity().findViewById(R.id.resetButton);
        fabReset.setOnClickListener(this);
        if(ActiveProfile.id.equals("1")) {
            fabReset.show();
        } else {
            fabReset.hide();
        }

        fabMatchMode = (FloatingActionButton) getActivity().findViewById(R.id.matchModeButton);
        fabMatchMode.setOnClickListener(this);
        if(Application.TAG_LIST_MATCH_MODE) {
            fabMatchMode.show();
        } else {
            fabMatchMode.hide();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.matchModeButton:
                if (RFIDController.mIsInventoryRunning) {
                    Toast.makeText(getActivity(), "Inventory is running", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/*");
                    startActivityForResult(Intent.createChooser(intent, "ChooseFile to upload"), TAGLIST_MATCH_MODE_IMPORT);
                }
                break;
            case R.id.resetButton:
                if (RFIDController.mIsInventoryRunning) {
                    Toast.makeText(getActivity(), "Inventory is running", Toast.LENGTH_SHORT).show();
                } else {
                    Application.cycleCountProfileData = null;
                    RFIDController.getInstance().clearAllInventoryData();
                    resetTagsInfoDetails();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        File cacheMatchModeTagFile = new File(getActivity().getCacheDir().getAbsolutePath(), Application.CACHE_TAGLIST_MATCH_MODE_FILE);
        if (cacheMatchModeTagFile.exists()) {
            cacheMatchModeTagFile.delete();
        }

        if (resultCode == RESULT_OK && requestCode == TAGLIST_MATCH_MODE_IMPORT) {
            Uri uri = data.getData();
            if (data == null) {
                return;
            }
            try {
                InputStream in = getActivity().getContentResolver().openInputStream(uri);
                OutputStream out = new FileOutputStream(cacheMatchModeTagFile);
                Log.d("size", in.toString());
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!cacheMatchModeTagFile.exists()) {
                Toast.makeText(getActivity(), getString(R.string.REQUIRES_TAGLIST_CSV), Toast.LENGTH_SHORT).show();
            } else {
                settingsUtil.LoadTagListCSV();
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggle(View view, final int position) {
        InventoryListItem listItem = adapter.getItem(position);
        if (!listItem.isVisible()) {
            listItem.setVisible(true);
            if (Application.TAG_LIST_MATCH_MODE)
                view.findViewById(R.id.tagDetailsCSV).setVisibility(View.VISIBLE);
            view.setBackgroundColor(0x66444444);
        } else {
            listItem.setVisible(false);
            view.setBackgroundColor(Color.WHITE);
        }
        //if(!RFIDController.mIsInventoryRunning)
        adapter.notifyDataSetChanged();
    }

    boolean batchModeEventReceived = false;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        memoryBankID = adapterView.getSelectedItem().toString();
        Application.memoryBankId = invAdapter.getPosition(memoryBankID);
        memoryBankID = memoryBankID.toLowerCase();
        Application.tagsReadForSearch.clear();

        ArrayList<InventoryListItem> tempList = new ArrayList<>();

        if (Application.TAG_LIST_MATCH_MODE) {
            if (Application.memoryBankId == 0) {
                adapter.searchItemsList = Application.tagsReadInventory;
                Application.tagsReadForSearch.addAll(Application.tagsReadInventory);
                adapter.notifyDataSetChanged();
            } else if (Application.memoryBankId == 1) {  //matching tags
                for (int k = 0; k < Application.tagsReadInventory.size(); k++) {
                    if ((Application.tagsReadInventory.get(k).getCount() > 0) && Application.tagListMap.containsKey(Application.tagsReadInventory.get(k).getTagID())) {
                        tempList.add(Application.tagsReadInventory.get(k));
                        Application.tagsReadForSearch.add(Application.tagsReadInventory.get(k));
                    }
                }
                adapter.searchItemsList = tempList;
                adapter.notifyDataSetChanged();
            } else if (Application.memoryBankId == 2) {  //missed tags
                for (int j = 0; j < Application.tagsReadInventory.size(); j++) {
                    //adapter.searchItemsList.get(i).getCount()
                    if (Application.tagsReadInventory.get(j).getCount() == 0) {
                        tempList.add(Application.tagsReadInventory.get(j));
                        Application.tagsReadForSearch.add(Application.tagsReadInventory.get(j));
                    }
                }
                adapter.searchItemsList = tempList;
                adapter.notifyDataSetChanged();

            } else if (Application.memoryBankId == 3) {   //unknown tags
                for (int k = 0; k < Application.tagsReadInventory.size(); k++) {
                    if ((Application.tagsReadInventory.get(k).getCount() > 0) && !Application.tagListMap.containsKey(Application.tagsReadInventory.get(k).getTagID())) {
                        tempList.add(Application.tagsReadInventory.get(k));
                        Application.tagsReadForSearch.add(Application.tagsReadInventory.get(k));
                    }
                }
                adapter.searchItemsList = tempList;
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /**
     * Method to access the memory bank set
     *
     * @return - Memory bank set
     */
    public String getMemoryBankID() {
        return memoryBankID;
    }

    /**
     * method to reset the tag info
     */
    public void resetTagsInfo() {
        // Moved code to  resetTagsInfoDetails() method for testing purpose
        if (!ActiveProfile.id.equals("1"))
            resetTagsInfoDetails();
    }

    public void resetTagsInfoDetails() {
        if (Application.inventoryList != null && Application.inventoryList.size() > 0)
            Application.inventoryList.clear();
        if (totalNoOfTags != null)
            totalNoOfTags.setText(String.valueOf(Application.TOTAL_TAGS));
        if (uniqueTags != null)
            uniqueTags.setText(String.valueOf(Application.UNIQUE_TAGS));
        if (timeText != null) {
            String min = String.format("%d", TimeUnit.MILLISECONDS.toMinutes(RFIDController.mRRStartedTime));
            String sec = String.format("%d", TimeUnit.MILLISECONDS.toSeconds(RFIDController.mRRStartedTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(RFIDController.mRRStartedTime)));
            if (min.length() == 1) {
                min = "0" + min;
            }
            if (sec.length() == 1) {
                sec = "0" + sec;
            }
            timeText.setText(min + ":" + sec);
        }
        if (listView.getAdapter() != null) {
            ((ModifiedInventoryAdapter) listView.getAdapter()).clear();
            ((ModifiedInventoryAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
        if (Application.TAG_LIST_MATCH_MODE) {
            totalNoOfTags.setText(String.valueOf(Application.missedTags));
            uniqueTags.setText(String.valueOf(Application.matchingTags));
        }


    }

    @Override
    public void handleTagResponse(InventoryListItem inventoryListItem, boolean isAddedToList) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (listView.getAdapter() == null) {
                        listView.setAdapter(adapter);
                        batchModeInventoryList.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    }
                    if (Application.TAG_LIST_MATCH_MODE) {
                        totalNoOfTags.setText(String.valueOf(Application.missedTags));
                        uniqueTags.setText(String.valueOf(Application.matchingTags));
                    } else {
                        totalNoOfTags.setText(String.valueOf(Application.TOTAL_TAGS));
                        if (uniqueTags != null)
                            uniqueTags.setText(String.valueOf(Application.UNIQUE_TAGS));
                    }
                    if (isAddedToList) {
                        if (!Application.TAG_LIST_MATCH_MODE) {
                            adapter.add(inventoryListItem);

                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }

    //  @Override
    public void handleStatusResponse(final RFIDResults results) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (results.equals(RFIDResults.RFID_BATCHMODE_IN_PROGRESS)) {
                    if (batchModeInventoryList != null) {
                        //  adapter.clear();
                        //  adapter.notifyDataSetChanged();
                        batchModeInventoryList.setText(R.string.batch_mode_inventory_title);
                        batchModeInventoryList.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                } else if (!results.equals(RFIDResults.RFID_API_SUCCESS)) {
                    RFIDController.isBatchModeInventoryRunning = false;
                    RFIDController.mIsInventoryRunning = false;
                    if (inventoryButton != null)
                        inventoryButton.setImageResource(android.R.drawable.ic_media_play);
                    if (invSpinner != null)
                        invSpinner.setEnabled(true);

                    //TODO: need of clearing
                    if (results.equals(RFIDResults.RFID_OPERATION_IN_PROGRESS)) {
                        if (Application.TAG_LIST_MATCH_MODE) {
                            logAsMessage(TYPE_DEBUG, "Inventory Fragment", "handleStatusResponse");
                            if (Application.tagsListCSV.size() == Application.tagsReadInventory.size()) {
                                Application.tagsReadInventory.clear();
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });
    }

    //   @Override
    public void triggerPressEventRecieved() {
        if (!RFIDController.mIsInventoryRunning && getActivity() != null) {
            //RFIDController.mInventoryStartPending = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (inventoryButton != null) {
                        inventoryButton.setImageResource(R.drawable.ic_play_stop);
                    }
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.inventoryStartOrStop();
                    }
                }
            });
        }
    }

    //   @Override
    public void triggerReleaseEventRecieved() {
        if ((RFIDController.mIsInventoryRunning && getActivity() != null)) {
            //RFIDController.mInventoryStartPending = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (inventoryButton != null) {
                        inventoryButton.setImageResource(android.R.drawable.ic_media_play);
                    }
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.inventoryStartOrStop();
                    }
                }
            });
        }
    }

    /**
     * method to set inventory status to stopped on reader disconnection
     */
    public void resetInventoryDetail() {

        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (!ActiveProfile.id.equals("1")) {
                        if (getActivity() != null) {
                            if (inventoryButton != null && !RFIDController.mIsInventoryRunning &&
                                    (RFIDController.isBatchModeInventoryRunning == null || !RFIDController.isBatchModeInventoryRunning)) {
                                inventoryButton.setImageResource(android.R.drawable.ic_media_play);
                            }
                            if (invSpinner != null)
                                invSpinner.setEnabled(true);
                            if (batchModeInventoryList != null && batchModeInventoryList.getVisibility() == View.VISIBLE) {
                                listView.setAdapter(adapter);
                                batchModeInventoryList.setText("");
                                batchModeInventoryList.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });


    }

    @Override
    public void batchModeEventReceived() {

        batchModeEventReceived = true;
        if (inventoryButton != null) {
            inventoryButton.setImageResource(R.drawable.ic_play_stop);
        }
        if (invSpinner != null) {
            invSpinner.setSelection(0);
            invSpinner.setEnabled(false);
        }

        if (listView != null/* && batchModeInventoryList != null*/) {
           /* adapter.clear();
            adapter.notifyDataSetChanged();*/
            listView.setEmptyView(batchModeInventoryList);
            batchModeInventoryList.setVisibility(View.VISIBLE);

            batchModeInventoryList.setText(R.string.batch_mode_inventory_title);
        }
    }
}
