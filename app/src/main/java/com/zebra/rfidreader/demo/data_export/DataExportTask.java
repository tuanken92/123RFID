package com.zebra.rfidreader.demo.data_export;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.inventory.InventoryListItem;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.zebra.rfidreader.demo.rfid.RFIDController.ActiveProfile;
import static com.zebra.rfidreader.demo.rfid.RFIDController.asciiMode;
import com.zebra.rfidreader.demo.common.hextoascii;

/**
 * class to export inventory Data in CSV format
 */
public class DataExportTask extends AsyncTask<Void, Void, Boolean> {

    private static final String FILE_EXTENSION = ".csv";
    private static final String HEADERS = "TAG ID,COUNT";
    private static final String INVENTORY_SUMMARY = "INVENTORY SUMMARY";
    private static final String UNIQUE_COUNT = "UNIQUE COUNT:";
    private static final String TOTAL_COUNT = "TOTAL COUNT:";
    private static final String MATCH_COUNT = "MATCH COUNT:";
    private static final String MISS_COUNT = "MISS COUNT:";
    private static final String UNKNOWN_COUNT = "UNKNOWN COUNT:";
    private static final String CYCLE_COUNT = "CYCLE COUNT:";
    private static final String SEPARATOR = ",";
    private static final String TAG = "DataExportTask";
    private static final String READ_TIME = "READ TIME:";
    private final ArrayList<InventoryListItem> inventoryList;
    private final Context context;
    private final String connectedReader;
    private final int totalTags;
    private final int uniqueTags;
    private final String readTime;
    private FileOutputStream fos;
    private Toast myToast;
    private Toast toast = null;

    public DataExportTask(Context context, ArrayList<InventoryListItem> inventoryList, String connectedReader, int totalTags, int uniqueTags, long rrStartedTime) {
        this.inventoryList = new ArrayList<>(inventoryList);
        this.context = context;
        this.connectedReader = connectedReader;
        this.totalTags = totalTags;
        this.uniqueTags = uniqueTags;
        this.readTime = getReadTime(rrStartedTime);
    }

    private String getReadTime(long rrStartedTime) {

        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(rrStartedTime),
                TimeUnit.MILLISECONDS.toMinutes(rrStartedTime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(rrStartedTime) % TimeUnit.MINUTES.toSeconds(1));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Toast.makeText(context, "Exporting inventory data...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String filename = "";
        try {
            if (isExternalStorageWritable()) {
                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/inventory");
                dir.mkdirs();
                File file = new File(dir, getFilename());
                filename = file.getAbsolutePath();
                if (!file.exists())
                    file.createNewFile();
                fos = new FileOutputStream(file,true);
                addInventorySummary();
                addHeaders();
                exportData(inventoryList);
            } else {
                Log.e(TAG, "External storage not writable");
                return false;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        MediaScannerConnection.scanFile(context, new String[]{filename}, null, null);

        return true;
    }

    private void addInventorySummary() throws IOException {
        fos.write((INVENTORY_SUMMARY + "\n").getBytes());

        if (Application.TAG_LIST_MATCH_MODE && (RFIDController.currentFragment != null && !RFIDController.currentFragment.equals("RapidReadFragment"))) {
            fos.write(MATCH_COUNT.getBytes());
            fos.write((SEPARATOR + Application.matchingTags + "\n").getBytes());
            fos.write(MISS_COUNT.getBytes());
            fos.write((SEPARATOR + Application.missedTags + "\n").getBytes());
            fos.write(UNKNOWN_COUNT.getBytes());
            fos.write((SEPARATOR + (Application.UNIQUE_TAGS - Application.matchingTags) + "\n").getBytes());
        } else if (Application.TAG_LIST_MATCH_MODE && RFIDController.currentFragment.equals("RapidReadFragment")) {
            fos.write(UNIQUE_COUNT.getBytes());
            fos.write((SEPARATOR + Application.UNIQUE_TAGS + "\n").getBytes());
            fos.write(TOTAL_COUNT.getBytes());
            fos.write((SEPARATOR + Application.TOTAL_TAGS + "\n").getBytes());
        } else {
            fos.write(UNIQUE_COUNT.getBytes());
            fos.write((SEPARATOR + uniqueTags + "\n").getBytes());
            fos.write(TOTAL_COUNT.getBytes());
            fos.write((SEPARATOR + totalTags + "\n").getBytes());
        }
        fos.write(READ_TIME.getBytes());
        fos.write((SEPARATOR + readTime + "\n\n").getBytes());
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {


            /*Toast mToastToShow;
            // Set the toast and duration
            int toastDurationInMilliSeconds = 1000;
            mToastToShow = Toast.makeText(context, "Inventory Data has been exported", Toast.LENGTH_SHORT);

            // Set the countdown to display the toast
            CountDownTimer toastCountDown;
            toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 5) {
                public void onTick(long millisUntilFinished) {
                    mToastToShow.show();
                }
                public void onFinish() {
                    mToastToShow.cancel();
                }
            };

            // Show the toast and starts the countdown
            mToastToShow.show();
            toastCountDown.start();**/

            Toast.makeText( context, "Inventory Data has been exported", Toast.LENGTH_SHORT ).show();

        }else {
            myToast = Toast.makeText( context, "Failed to export inventory data", Toast.LENGTH_SHORT );
            myToast.show();
        }
    }

    /**
     * method to add headers to the file
     */
    private void addHeaders() throws IOException {
        if (RFIDController.tagStorageSettings != null) {
            StringBuilder sbHeaders = new StringBuilder();
            ArrayList<TAG_FIELD> fields = new ArrayList();
            for (TAG_FIELD tag_field : RFIDController.tagStorageSettings.getTagFields())
                fields.add(tag_field);
            if (fields.contains(TAG_FIELD.PEAK_RSSI))
                sbHeaders.append(SEPARATOR + "RSSI");
            if (fields.contains(TAG_FIELD.PHASE_INFO))
                sbHeaders.append(SEPARATOR + "PHASE");
            if (fields.contains(TAG_FIELD.PC))
                sbHeaders.append(SEPARATOR + "PC");
            if (fields.contains(TAG_FIELD.CHANNEL_INDEX))
                sbHeaders.append(SEPARATOR + "CHANNEL INDEX");

            fos.write((HEADERS + sbHeaders + "\n").getBytes());
        } else
            fos.write((HEADERS + "\n").getBytes());
    }

    /**
     * method to export data to file in csv format
     *
     * @param inventoryList inventory data to be export
     */
    private void exportData(ArrayList<InventoryListItem> inventoryList) throws IOException {

        if (Application.TAG_LIST_MATCH_MODE && (RFIDController.currentFragment.equals("RapidReadFragment") || RFIDController.currentFragment.equals(""))) {
            for (InventoryListItem item : inventoryList) {
                if(asciiMode) {
                    fos.write((hextoascii.convert(item.getText()) + SEPARATOR + item.getCount() + SEPARATOR + item.getTagStatus() + "\n").getBytes());
                } else {
                    fos.write((item.getText() + SEPARATOR + item.getCount() + SEPARATOR + item.getTagStatus() + "\n").getBytes());
                }
            }
        } else {
            for (InventoryListItem item : inventoryList) {
                StringBuilder stringBuilder = new StringBuilder();
                if(asciiMode) {
                    stringBuilder.append(hextoascii.convert(item.getText()) + SEPARATOR + item.getCount());
                } else {
                    stringBuilder.append(item.getText() + SEPARATOR + item.getCount());
                }
                if (item.getRSSI() != null)
                    stringBuilder.append(SEPARATOR + item.getRSSI());
                if (item.getPhase() != null)
                    stringBuilder.append(SEPARATOR + item.getPhase());
                if (item.getPC() != null)
                    stringBuilder.append(SEPARATOR + item.getPC());
                if (item.getChannelIndex() != null)
                    stringBuilder.append(SEPARATOR + item.getChannelIndex());
                stringBuilder.append("\n");
                fos.write(stringBuilder.toString().getBytes());
            }
        }
    }

    /**
     * method to know whether file existed are not
     */
    public boolean fileExistance(String fname) {
        File file = context.getFileStreamPath(fname);
        return file.exists();
    }

    /**
     * Checks if external storage is available for read and write
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * get filename
     */
    private String getFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");
        if(ActiveProfile.id.equals("1")) {
            if(Application.cycleCountProfileData != null) {
                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/inventory");
                File file = new File(dir, Application.cycleCountProfileData);
                if(file.exists()) {
                    file.delete();
                }
            }
            Application.cycleCountProfileData = connectedReader + "_" + sdf.format(new Date()) + FILE_EXTENSION;
            return Application.cycleCountProfileData;
        }
        return connectedReader + "_" + sdf.format(new Date()) + FILE_EXTENSION;
    }
}
