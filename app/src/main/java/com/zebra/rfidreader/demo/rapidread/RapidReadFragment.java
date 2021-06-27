package com.zebra.rfidreader.demo.rapidread;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.inventory.InventoryListItem;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.util.concurrent.TimeUnit;

import static com.zebra.rfidreader.demo.application.Application.TAG_LIST_LOADED;
import static com.zebra.rfidreader.demo.rfid.RFIDController.ActiveProfile;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link RapidReadFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle the rapid read operation and UI
 */
public class RapidReadFragment extends Fragment implements ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.BatchModeEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler {
    MatchModeProgressView progressView;
    private TextView tagReadRate;
    private TextView uniqueTags;
    private TextView totalTags;
    private FloatingActionButton inventoryButton;
    private TextView timeText;
    private TextView uniqueTagTitle;
    private TextView totalTagTitle;
    private LinearLayout invtoryData;
    private FrameLayout batchModeRR;
    boolean batchModeEventReceived = false;

    public RapidReadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RapidReadFragment.
     */
    public static RapidReadFragment newInstance() {
        return new RapidReadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rr, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rr, menu);
       /* menu.findItem(R.id.action_clear).setVisible(ActiveProfile.id.equals("1")).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (RFIDController.mIsInventoryRunning) {
                    Toast.makeText(getContext(), "Inventory is running", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    RFIDController.getInstance().clearInventoryData();
                    resetTagsInfo();
                    TAG_LIST_LOADED = false;

                   *//* final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Clear inventory");
                    builder.setMessage("Do you want to clear data?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RFIDController.getInstance().clearInventoryData();
                            resetTagsInfo();
                            TAG_LIST_LOADED = false;

                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    });
                    builder.create().show();
*//*
                    return true;
                }

            }
        });*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setNavigationMode(android.support.v7.app.ActionBar.NAVIGATION_MODE_STANDARD);
        inventoryButton = (FloatingActionButton) mainActivity.findViewById(R.id.rr_inventoryButton);
        uniqueTags = (TextView) mainActivity.findViewById(R.id.uniqueTagContent);
        uniqueTagTitle = (TextView) mainActivity.findViewById(R.id.uniqueTagTitle);
        totalTags = (TextView) mainActivity.findViewById(R.id.totalTagContent);
        totalTagTitle = (TextView) mainActivity.findViewById(R.id.totalTagTitle);
        tagReadRate = (TextView) getActivity().findViewById(R.id.readRateContent);
        batchModeRR = (FrameLayout) getActivity().findViewById(R.id.batchModeRR);
        invtoryData = (LinearLayout) getActivity().findViewById(R.id.inventoryDataLayout);
        if (RFIDController.mIsInventoryRunning) {
            inventoryButton.setImageResource(R.drawable.ic_play_stop);
        } else {
            inventoryButton.setImageResource(android.R.drawable.ic_media_play);
        }
        if (RFIDController.isBatchModeInventoryRunning != null && RFIDController.isBatchModeInventoryRunning) {
            invtoryData.setVisibility(View.GONE);
            batchModeRR.setVisibility(View.VISIBLE);
        } else {
            invtoryData.setVisibility(View.VISIBLE);
            batchModeRR.setVisibility(View.GONE);
        }
        if (RFIDController.mRRStartedTime == 0)
            Application.TAG_READ_RATE = 0;
        else
            Application.TAG_READ_RATE = (int) (Application.TOTAL_TAGS / (RFIDController.mRRStartedTime / (float) 1000));
        tagReadRate.setText(Application.TAG_READ_RATE + Constants.TAGS_SEC);
        timeText = (TextView) getActivity().findViewById(R.id.readTimeContent);
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
        progressView = getActivity().findViewById(R.id.MatchModeView);
        if (Application.TAG_LIST_MATCH_MODE) {
            progressView.setVisibility( View.VISIBLE );
        }else{
            progressView.setVisibility( View.GONE );
        }
        if (Application.missedTags > 9999) {
            //orignal size is 60sp - reduced size 45sp
            uniqueTags.setTextSize(45);
        }
        updateTexts();
        getActivity().findViewById(R.id.tv_prefilter_enabled).setVisibility(
                RFIDController.getInstance().isPrefilterEnabled() ? View.VISIBLE : View.INVISIBLE);
        Button bt_clear = getActivity().findViewById(R.id.bt_clear);
        bt_clear.setVisibility(ActiveProfile.id.equals("1") ? View.VISIBLE : View.INVISIBLE);
        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RFIDController.mIsInventoryRunning) {
                    Toast.makeText(getContext(), "Inventory is running", Toast.LENGTH_SHORT).show();
                } else {
                    RFIDController.getInstance().clearAllInventoryData();
                    resetTagsInfo();
                    TAG_LIST_LOADED = false;

                 /* final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Clear inventory");
                    builder.setMessage("Do you want to clear data?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RFIDController.getInstance().clearInventoryData();
                            resetTagsInfo();
                            TAG_LIST_LOADED = false;

                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();

                    return true;*/
                }

            }
        });
    }

    public void updateTexts() {
        if (Application.TAG_LIST_MATCH_MODE) {
            if (uniqueTags != null && totalTags != null) {
                totalTags.setText(String.valueOf(Application.matchingTags));
                uniqueTags.setText(String.valueOf(Application.missedTags));
            }
            if (totalTagTitle != null && uniqueTagTitle != null) {
                totalTagTitle.setText(R.string.rr_total_tag_title_MM);
                uniqueTagTitle.setText(R.string.rr_unique_tags_title_MM);
            }
            updateProgressView();
        } else {
            if (uniqueTags != null)
                uniqueTags.setText(String.valueOf(Application.UNIQUE_TAGS));
            if (totalTags != null)
                totalTags.setText(String.valueOf(Application.TOTAL_TAGS));
            if (totalTagTitle != null && uniqueTagTitle != null) {
                totalTagTitle.setText(R.string.rr_total_tag_title);
                uniqueTagTitle.setText(R.string.rr_unique_tags_title);
            }
        }
    }

    private void updateProgressView() {
        if (Application.missedTags != 0) {
            progressView.mSweepAngle = 360 * Application.matchingTags / (Application.missedTags + Application.matchingTags);
        } else if (Application.matchingTags != 0 && Application.missedTags == 0 && RFIDController.mIsInventoryRunning ) {
            progressView.bCompleted = true;
        } else {
            progressView.mSweepAngle = 0;
        }
        if (progressView.mSweepAngle >= 360) {
            progressView.mSweepAngle = 0;
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressView.invalidate();
                    progressView.requestLayout();
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * method to reset tags info on the screen before starting inventory operation
     */
    public void resetTagsInfo() {
//        uniqueTags.setText(String.valueOf(RFIDController.UNIQUE_TAGS));
//        totalTags.setText(String.valueOf(RFIDController.TOTAL_TAGS));
        updateTexts();
        progressView.bCompleted = false;
        tagReadRate.setText(Application.TAG_READ_RATE + Constants.TAGS_SEC);
        timeText.setText(Constants.ZERO_TIME);
    }

    /**
     * method to start inventory operation on trigger press event received
     */
    public void triggerPressEventRecieved() {
        if (!RFIDController.mIsInventoryRunning && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.inventoryStartOrStop();
                    }
                }
            });
        }
    }

    /**
     * method to stop inventory operation on trigger release event received
     */
    public void triggerReleaseEventRecieved() {
        if ((RFIDController.mIsInventoryRunning == true) && getActivity() != null) {
            //RFIDController.mInventoryStartPending = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.inventoryStartOrStop();
                    }
                }
            });
        }
    }

    public void handleStatusResponse(final RFIDResults results) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (results.equals(RFIDResults.RFID_BATCHMODE_IN_PROGRESS)) {
                    if (uniqueTags != null) {
                        invtoryData.setVisibility(View.GONE);
                        batchModeRR.setVisibility(View.VISIBLE);
                    }
                } else if (!results.equals(RFIDResults.RFID_API_SUCCESS)) {
                    RFIDController.mIsInventoryRunning = false;
                    if (inventoryButton != null){
                        inventoryButton.setImageResource( android.R.drawable.ic_media_play );
                    }
                    RFIDController.isBatchModeInventoryRunning = false;
                }
            }
        });
    }


    /**
     * method to update inventory details on the screen on operation end summary received
     */
    public void updateInventoryDetails() {
        updateTexts();
        tagReadRate.setText(Application.TAG_READ_RATE + Constants.TAGS_SEC);
    }

    /**
     * method to reset inventory operation status on the screen
     */
    public void resetInventoryDetail() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!ActiveProfile.id.equals("1")) {
                        if (inventoryButton != null && !RFIDController.mIsInventoryRunning &&
                                (RFIDController.isBatchModeInventoryRunning == null || !RFIDController.isBatchModeInventoryRunning)) {
                            inventoryButton.setImageResource(android.R.drawable.ic_media_play);
                        }
                        if (uniqueTags != null) {
                            invtoryData.setVisibility(View.VISIBLE);
                        }
                        if (Application.TAG_LIST_MATCH_MODE)
                            progressView.setVisibility(View.VISIBLE);

                        if (batchModeRR != null) {
                            batchModeRR.setVisibility(View.GONE);
                        }

                        if (Application.TAG_LIST_MATCH_MODE && Application.matchingTags != 0 && Application.missedTags == 0) {
                            progressView.bCompleted = true;
                        }
                    }
                }
            });
    }

    @Override
    public void batchModeEventReceived() {
        batchModeEventReceived = true;
        if (inventoryButton != null)
            inventoryButton.setImageResource(R.drawable.ic_play_stop);
        if (uniqueTags != null) {
            invtoryData.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
        }
        if (batchModeRR != null) {
            batchModeRR.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void handleTagResponse(InventoryListItem inventoryListItem, boolean isAddedToList) {
        updateTexts();
       if (tagReadRate != null) {
            if (RFIDController.mRRStartedTime == 0)
                Application.TAG_READ_RATE = 0;
            else
                Application.TAG_READ_RATE = (int) (Application.TOTAL_TAGS / (RFIDController.mRRStartedTime / (float) 1000));
            tagReadRate.setText(Application.TAG_READ_RATE + Constants.TAGS_SEC);
        }
    }
}
