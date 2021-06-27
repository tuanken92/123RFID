package com.zebra.rfidreader.demo.common;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.inventory.InventoryListItem;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.MOVED_TO;
import static com.zebra.rfidreader.demo.rfid.RFIDController.TAG;

public class MatchModeFileLoader {

    private static MatchModeFileLoader m_instance;
    private TagListObserver m_observer;
    private Intent intent;
    private String s;
    // taglist support
    protected TagListDataImportTask tagListDataImportTask;

    private static File cacheMatchModeTagFile = null;

    public MatchModeFileLoader(Context context) {
    }

    public static MatchModeFileLoader getInstance(Context context) {
        if (m_instance == null)
            m_instance = new MatchModeFileLoader(context);
        cacheMatchModeTagFile = new File(context.getCacheDir().getAbsolutePath(), Application.CACHE_TAGLIST_MATCH_MODE_FILE);
        return m_instance;
    }

    private class TagListObserver extends FileObserver {
        public TagListObserver(String path, int i) {
            super(path, i);
        }

        @Override
        public void onEvent(int event, @Nullable String path) {
            Log.d(TAG, "observer " + event);
            if (!RFIDController.mIsInventoryRunning && Application.TAG_LIST_MATCH_MODE) {
                if (event == DELETE) {
                    if (!cacheMatchModeTagFile.exists()) {
                        Application.TAG_LIST_MATCH_MODE = false;
                        Application.TAG_LIST_FILE_EXISTS = false;
                    }
                } else if (event == CREATE && !Application.TAG_LIST_MATCH_MODE)
                    LoadMatchModeCSV();
                else if (event == MOVED_TO) {
                    LoadMatchModeCSV();
                }
            }
        }
    }

    public void LoadMatchModeCSV() {
        if (m_observer == null) {
            m_observer = new TagListObserver(cacheMatchModeTagFile.getAbsolutePath(), CREATE | DELETE | MOVED_TO);
            m_observer.startWatching();
        }
        if (cacheMatchModeTagFile.exists()) {
            Application.tagListMap.clear();
            Application.tagsListCSV.clear();
            //RFIDController.TAG_LIST_MATCH_MODE = true;
            Application.TAG_LIST_FILE_EXISTS = true;
            LoadCSV();
        } else {
            Application.TAG_LIST_FILE_EXISTS = false;
            Application.TAG_LIST_MATCH_MODE = false;
        }
    }

    private void LoadCSV() {
        Log.d(TAG, "Loading CSV");
        tagListDataImportTask = new TagListDataImportTask();
        tagListDataImportTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public boolean isImportTaskRunning() {
        if (tagListDataImportTask != null && !tagListDataImportTask.isCancelled())
            return true;
        return false;
    }

    public static final String UTF8_BOM = "\uFEFF";

    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s.trim();
    }

    public class TagListDataImportTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
//            Toast.makeText(getApplicationContext(), "Importing tag match data...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Importing tag match data");
        }


        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                //Constants.logAsMessage(TYPE_DEBUG, TAG, "TagListDataImportTask");
                BufferedReader br = null;
                String line = "";
                String cvsSplitBy = ",";
                if (cacheMatchModeTagFile.exists()) {
                    br = new BufferedReader(new FileReader(cacheMatchModeTagFile));
                    int count = 0;
                    Log.d(TAG, "tag match data br");
                    // create regex pattern
                    String hexPattern = "^\\p{XDigit}+$";  // Match for Hex data
                    String asciiPattern = "^\\p{ASCII}+$";  // Match for Hex data
                    while ((line = br.readLine()) != null) {
                        // use comma as separator
                        String[] row = line.split(cvsSplitBy);
                        if (row.length != 0 && !row[0].isEmpty()) {
                            //matches("^.*[^a-zA-Z0-9 ].*$") returns true if there is any char other than alpha and nos
                            String id = removeUTF8BOM(row[0]);
                            if ((RFIDController.asciiMode && id.matches(asciiPattern) || id.matches(hexPattern)) ) {
                                if(RFIDController.asciiMode) {
                                    StringBuilder strb = new StringBuilder(id);
                                    if(strb.charAt(0) != '\'') strb.insert(0, '\'');
                                    if(strb.charAt(strb.length()-1) != '\'') strb.append('\'');
                                    id = strb.toString();
                                }
                                Application.tagListMap.put(id, count);
                                InventoryListItem inv = (new InventoryListItem(id, 0, null, null, null, null, null, null));
                                if (row.length >= 2)
                                    inv.setTagDetails(row[1]);
                                inv.setTagStatus("MISS");
                                Application.tagsListCSV.add(inv);
                                count++;
                            }
                        }
                        //Log.d(TAG,"#"+line);
                    }
                    if (Application.tagsListCSV.size() == 0) {
                        Application.TAG_LIST_MATCH_MODE = false;
                    }
                }
                return true;
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
                Application.TAG_LIST_MATCH_MODE = false;
                return false;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            } finally {
            }
        }


        @Override
        protected void onPostExecute(Boolean result) {
            cancel(true);
//            if (result)
//                Toast.makeText(getApplicationContext(), "tag match data has been imported", Toast.LENGTH_SHORT).show();
//            else
//                Toast.makeText(getApplicationContext(), "Failed to import tag match data", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Importing tag match data done");
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
    }

}
