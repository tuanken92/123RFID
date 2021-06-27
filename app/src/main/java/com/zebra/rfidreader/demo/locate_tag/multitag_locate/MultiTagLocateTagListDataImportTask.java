package com.zebra.rfidreader.demo.locate_tag.multitag_locate;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.asciitohex;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.zebra.rfidreader.demo.rfid.RFIDController.mConnectedReader;

public class MultiTagLocateTagListDataImportTask implements Runnable {

    public static final String UTF8_BOM = "\uFEFF";
    // Debugging
    protected static String TAG = MultiTagLocateTagListDataImportTask.class.getName();
    File multiTagLocateTagListFile = null;

    MultiTagLocateTagListDataImportTask(File locateCsvFile) {
        multiTagLocateTagListFile = locateCsvFile;
    }

    @Override
    public void run() {
        try {
            Constants.logAsMessage(Constants.TYPE_DEBUG, TAG, "MultiTagLocateTagListDataImportTask" );
            Application.multiTagLocateTagListMap.clear();
            Application.multiTagLocateActiveTagItemList.clear();
            Application.multiTagLocateTagIDs.clear();
			Application.multiTagLocateTagMap.clear();
            Application.multiTagLocateTagListExist = false;

            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";
            if (multiTagLocateTagListFile.exists()) {
                br = new BufferedReader(new FileReader(multiTagLocateTagListFile));
                int count=0;
                while ((line = br.readLine()) != null) {
                    // use comma as separator
                    String[] row = line.split(cvsSplitBy);
                    String id = removeUTF8BOM(row[0]);
                    String rssiValue = null;
                    if(row.length > 1) {
                        rssiValue = removeUTF8BOM(row[1]);
                    }
                    //matches("^.*[^a-zA-Z0-9 ].*$") returns true if there is any char other than alpha and nos
                    if(!id.isEmpty()) {  // Match for Hex data
                        String tagID = id;
                        if(RFIDController.asciiMode && tagID.matches("^\\p{ASCII}+$") )
                        {
                            StringBuilder strb = new StringBuilder(tagID);
                            strb.insert(0, '\'');
                            strb.append('\'');
                            tagID = strb.toString();
                            MultiTagLocateListItem inv = new MultiTagLocateListItem(tagID, rssiValue, 0, (short) 0);
                            if(!Application.multiTagLocateTagListMap.containsKey(tagID)) {
                                Application.multiTagLocateTagListMap.put(tagID, inv);
                                Application.multiTagLocateTagMap.put(asciitohex.convert(tagID).toUpperCase(), inv.getRssiValue());
                            }
                        } else if(id.matches("^\\p{XDigit}+$") && !Application.multiTagLocateTagListMap.containsKey(id)){
                            MultiTagLocateListItem inv = new MultiTagLocateListItem(tagID, rssiValue, 0, (short) 0);
                            Application.multiTagLocateTagListMap.put(tagID, inv);
                            Application.multiTagLocateTagMap.put(tagID, inv.getRssiValue());
                        }
                    }
                }
                if(Application.multiTagLocateTagListMap.size() > 0)
                {
                    Application.multiTagLocateActiveTagItemList = new ArrayList<MultiTagLocateListItem>(Application.multiTagLocateTagListMap.values());
                    Application.multiTagLocateTagIDs = new ArrayList<String>(Application.multiTagLocateTagListMap.keySet());
                    Application.multiTagLocateTagListExist = true;
                    multiTagLocatPreImportTagList();
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
        }
    }

    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
                s = s.substring(1);
            }
        s = s.replaceAll("^\"|\"$", "");
        return s.trim();
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

    public static void multiTagLocatPreImportTagList() throws InvalidUsageException, OperationFailureException {
        if (mConnectedReader != null && mConnectedReader.isConnected()) {
            if(Application.multiTagLocateTagListExist) {
                mConnectedReader.Actions.MultiTagLocate.purgeItemList();
                mConnectedReader.Actions.MultiTagLocate.importItemList(Application.multiTagLocateTagMap);
            }
        }
    }
}
