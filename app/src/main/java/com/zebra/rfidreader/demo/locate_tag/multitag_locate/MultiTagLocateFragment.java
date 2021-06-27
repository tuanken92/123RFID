package com.zebra.rfidreader.demo.locate_tag.multitag_locate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.rfid.RFIDController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import static android.app.Activity.RESULT_OK;

/**
 * Created by PKF847 on 7/31/2017.
 */

public class MultiTagLocateFragment extends Fragment implements ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler,
        View.OnClickListener {
    private static final int LOCATE_TAG_CSV_IMPORT = 0;
    private MultiTagLocateInventoryAdapter tagListAdapter;

    private LinearLayout tagItemDataLayout;
    private AutoCompleteTextView tagItemView;
    private Button addItemButton;
    private Button deleteItemButton;
    private FloatingActionButton locateButton;
    private FloatingActionButton resetButton;
    private FloatingActionButton btnImportTagList;
    private RecyclerView listView;
    private ArrayAdapter<String> searchTagListAdapter;
    File cacheLocateTagfile = null;

    private MultiTagLocateInventoryAdapter.OnItemClickListner onItemClickListener = new MultiTagLocateInventoryAdapter.OnItemClickListner() {
        @Override
        public void onItemClick(int position) {
            if (!Application.mIsMultiTagLocatingRunning) {
                tagItemView.setText(tagListAdapter.getItem(position).getTagID());
                //Application.locateTag = tagListAdapter.getItem(position).getTagID();
            }
        }
    };

    public MultiTagLocateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InventoryFragment.
     */
    public static MultiTagLocateFragment newInstance() {
        return new MultiTagLocateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multitag_locate, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (tagItemView.getText().toString() != null && Application.multiTagLocateTagListMap.containsKey(tagItemView.getText().toString()))
            Application.locateTag = tagItemView.getText().toString();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //((ActionBarActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.dl_loc);

        tagItemDataLayout = (LinearLayout) getActivity().findViewById(R.id.multiTagLocateDataLayout);
        tagItemView = (AutoCompleteTextView) getActivity().findViewById(R.id.multiTagLocate_epc);
        addItemButton = (Button) getActivity().findViewById(R.id.multiTagLocateAddItemButton);
        deleteItemButton = (Button) getActivity().findViewById(R.id.multiTagLocateDeleteItemButton);
        locateButton = (FloatingActionButton) getActivity().findViewById(R.id.multiTagLocateButton);
        resetButton = (FloatingActionButton) getActivity().findViewById(R.id.multiTagLocateResetButton);
        btnImportTagList = (FloatingActionButton) getActivity().findViewById(R.id.multi_tag_locate_import);
        btnImportTagList.setOnClickListener(this);
        listView = (RecyclerView) getActivity().findViewById(R.id.inventoryList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchTagListAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.multiTagLocateTagIDs);
        tagItemView.setAdapter(searchTagListAdapter);

        if (Application.locateTag != null && Application.multiTagLocateTagListMap.containsKey(Application.locateTag))
            tagItemView.setText(Application.locateTag);

        if (Application.mIsMultiTagLocatingRunning) {
            locateButton.setImageResource(R.drawable.ic_play_stop);
            enableGUIComponents(false);
        } else {
            locateButton.setImageResource(android.R.drawable.ic_media_play);
            enableGUIComponents(true);
        }
        cacheLocateTagfile = new File(getActivity().getCacheDir().getAbsolutePath(), Application.CACHE_LOCATE_TAG_FILE);
        if(cacheLocateTagfile.exists()) {
            multiTagLocatPreImportList(cacheLocateTagfile);
        } else {
            updateTagItemList();
        }
    }

    public void enableGUIComponents(boolean flag) {
        //tagItemDataLayout.setEnabled(flag);
        //tagItemView.setEnabled(flag);
        addItemButton.setEnabled(flag);
        deleteItemButton.setEnabled(flag);
        resetButton.setEnabled(flag);
        btnImportTagList.setEnabled(flag);
    }

    /**
     * method to reset multitag locationing status to default on reader disconnection or resetbutton click event
     */
    public void resetMultiTagLocateDetail(boolean isDeviceDisconnected) {
        if (getActivity() != null) {
            Application.mIsMultiTagLocatingRunning = false;
            locateButton.setImageResource(android.R.drawable.ic_media_play);
            //enableGUIComponents(true);

            if (!isDeviceDisconnected) { //called because of RESET button event
                if (Application.multiTagLocateTagListExist) {
                    Application.multiTagLocateActiveTagItemList.clear();
                    //Application.multiTagLocateTagIDs.clear();

                    for (String tagID : Application.multiTagLocateTagListMap.keySet()) {
                        Application.multiTagLocateTagListMap.get(tagID).setReadCount(0);
                        Application.multiTagLocateTagListMap.get(tagID).setProximityPercent((short) 0);
                    }

                    Application.multiTagLocateActiveTagItemList = new ArrayList<MultiTagLocateListItem>(Application.multiTagLocateTagListMap.values());
                    try {
                        RFIDController.mConnectedReader.Actions.MultiTagLocate.purgeItemList();
                        RFIDController.mConnectedReader.Actions.MultiTagLocate.importItemList(Application.multiTagLocateTagMap);
                    } catch (InvalidUsageException e) {
                        ((MainActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, e.getInfo());
                    } catch (OperationFailureException e) {
                        ((MainActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, e.getVendorMessage());
                    }

                    //Application.multiTagLocateTagIDs = new ArrayList<String>(Application.multiTagLocateTagListMap.keySet());
                    //searchTagListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, Application.multiTagLocateTagIDs);
                    //tagItemView.setAdapter(searchTagListAdapter);
                    tagItemView.setText("");
                    listView.setAdapter(null);
                    updateTagItemList();
                }
            }
        }
    }

    @Override
    public void triggerPressEventRecieved() {
        if (!Application.mIsMultiTagLocatingRunning)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    locateButton.setImageResource(android.R.drawable.ic_media_play);
                    ((MainActivity) getActivity()).multiTagLocateStartOrStop(locateButton);
                }
            });
    }

    @Override
    public void triggerReleaseEventRecieved() {
        if (Application.mIsMultiTagLocatingRunning)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    locateButton.setImageResource(R.drawable.ic_play_stop);
                    ((MainActivity) getActivity()).multiTagLocateStartOrStop(locateButton);
                }
            });
    }

    @Override
    public void handleStatusResponse(final RFIDResults results) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!results.equals(RFIDResults.RFID_API_SUCCESS)) {
                    Application.mIsMultiTagLocatingRunning = false;
                    if (locateButton != null) {
                        locateButton.setImageResource(android.R.drawable.ic_media_play);
                    }
                    //enableGUIComponents(true);
                }
            }
        });
    }

    public void updateTagItemList() {
        if(listView.getAdapter() == null) {
            tagListAdapter = new MultiTagLocateInventoryAdapter(onItemClickListener);
            listView.setAdapter(tagListAdapter);
        }
        if (Application.MULTI_TAG_LOCATE_SORT) {
            tagListAdapter.sortItemList();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tagListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void multiTagLocatPreImportList(File uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            FutureTask importTask = new FutureTask(new MultiTagLocateTagListDataImportTask(uri), "Import task complete");
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorService.execute(importTask);
            while (true) {
                if (importTask.isDone()) {
                    listView.setAdapter(null);
                    updateTagItemList();
                    executorService.shutdown();
                    break;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multi_tag_locate_import:
                if(!Application.mIsMultiTagLocatingRunning) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/*");
                    startActivityForResult(Intent.createChooser(intent, "ChooseFile to upload"), LOCATE_TAG_CSV_IMPORT);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.multiTag_locate_error_operation_running), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        if (resultCode == RESULT_OK && requestCode == LOCATE_TAG_CSV_IMPORT) {
            if(data == null) return;
            Uri uri = data.getData();
            importLocateTagList(String.valueOf(uri));
        }
    }

    private void importLocateTagList(String locateCsvFile) {
        Uri locateCsvUri = Uri.parse(locateCsvFile);
        try {
            if(cacheLocateTagfile.exists()) {
                cacheLocateTagfile.delete();
            }
            InputStream in = getActivity().getContentResolver().openInputStream(locateCsvUri);
            OutputStream out = new FileOutputStream(cacheLocateTagfile);
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
        if(cacheLocateTagfile.exists()) {
            multiTagLocatPreImportList(cacheLocateTagfile);
            Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.status_failure_message, Toast.LENGTH_SHORT).show();
        }
    }
}
