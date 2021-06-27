package com.zebra.rfidreader.demo.application;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.media.ToneGenerator;
import android.util.ArrayMap;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.Events.BatteryData;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFModeTable;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RegulatoryConfig;
import com.zebra.rfid.api3.StartTrigger;
import com.zebra.rfid.api3.StopTrigger;
import com.zebra.rfid.api3.TagStorageSettings;
import com.zebra.rfid.api3.UNIQUE_TAG_REPORT_SETTING;
import com.zebra.rfidreader.demo.common.MaxLimitArrayList;
import com.zebra.rfidreader.demo.common.PreFilters;
import com.zebra.rfidreader.demo.home.MainActivity;
import com.zebra.rfidreader.demo.inventory.InventoryListItem;
import com.zebra.rfidreader.demo.locate_tag.multitag_locate.MultiTagLocateListItem;
import com.zebra.rfidreader.demo.settings.ProfileContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by qvfr34 on 12/31/2015.
 */
public class Application extends android.app.Application {

    public static RFIDReader mConnectedReader;

    //Variable to keep track of the unique tags seen
    public static volatile int UNIQUE_TAGS = 0;

    //Variable to keep track of the unique tags when matching tags CSV is enabled. (value=UNIQUE_TAGS+missing tags).
    public static volatile int UNIQUE_TAGS_CSV = 0;

    //variable to keep track of the total tags seen
    public static volatile int TOTAL_TAGS = 0;
    //Arraylist to keeptrack of the tagIDs to act as adapter for autocomplete text views
    public static ArrayList<String> tagIDs;
    //variable to store the tag read rate
    public static volatile int TAG_READ_RATE = 0;
    //Boolean to keep track of whether the inventory is running or not
    public static volatile boolean mIsInventoryRunning;
    public static volatile boolean mInventoryStartPending;
    public static int inventoryMode = 0;
    public static Boolean isBatchModeInventoryRunning;
    public static int memoryBankId = -1;
    public static String accessControlTag;
    public static String locateTag;
    public static String PreFilterTag;
    //Variable to maintain the RR started time to maintain the read rate
    public static volatile long mRRStartedTime;
    public static PreFilters[] preFilters = null;
    public static boolean isAccessCriteriaRead = false;
    public static int preFilterIndex = -1;
    //For Notification
    public static volatile int INTENT_ID = 100;
    public static MainActivity.EventHandler eventHandler;
    public static TreeMap<String, Integer> inventoryList = new TreeMap<String, Integer>();
    public static HashMap<String, String> versionInfo = new HashMap<>(5);

    //Arraylist to keeptrack of the tags read for Inventory
    public static ArrayList<InventoryListItem> tagsReadInventory = new MaxLimitArrayList();

    //Arraylist to store the tags from CSV file
    public static ArrayList<InventoryListItem> tagsListCSV = new MaxLimitArrayList();
    public static ArrayList<InventoryListItem> matchingTagsList = new MaxLimitArrayList();
    public static ArrayList<InventoryListItem> missingTagsList = new MaxLimitArrayList();
    public static ArrayList<InventoryListItem> unknownTagsList = new MaxLimitArrayList();
    public static ArrayList<InventoryListItem> tagsReadForSearch = new MaxLimitArrayList();

    public static volatile boolean TAG_LIST_MATCH_MODE = false;
    public static volatile boolean TAG_LIST_FILE_EXISTS = false;
    public static boolean tagListMatchAutoStop = false;
    public static boolean tagListMatchNotice = false;
    public static TreeMap<String, Integer> tagListMap = new TreeMap<String, Integer>();
    public static int missedTags = 0;
    public static int matchingTags = 0;

    public static boolean isGettingTags;
    public static boolean EXPORT_DATA;
    public static ReaderDevice mConnectedDevice;
    public static BluetoothDevice BTDevice;
    public static boolean isLocatingTag;

    public static String importFileName = "";
    //Variable to multiTagLocate operation
    public static volatile boolean mIsMultiTagLocatingRunning;
    public static boolean multiTagLocateTagListExist=false;
    public static TreeMap<String, MultiTagLocateListItem> multiTagLocateTagListMap = new TreeMap<String, MultiTagLocateListItem>();
    public static ArrayList<MultiTagLocateListItem> multiTagLocateActiveTagItemList = new ArrayList<MultiTagLocateListItem>();
    //Arraylist to keeptrack of the tagIDs to act as adapter for multiTagLocate autocomplete text views
    public static ArrayMap<String, String> multiTagLocateTagMap = new ArrayMap<String, String>();
    public static ArrayList<String> multiTagLocateTagIDs = new ArrayList<String>();
    //
    public static StartTrigger settings_startTrigger;
    public static StopTrigger settings_stopTrigger;
    public static short TagProximityPercent = -1;
    public static TagStorageSettings tagStorageSettings;
    public static int batchMode;
    public static BatteryData BatteryData = null;
    public static DYNAMIC_POWER_OPTIMIZATION dynamicPowerSettings;
    public static boolean is_disconnection_requested;
    public static boolean is_connection_requested;
    //Application Settings
    public static volatile boolean AUTO_DETECT_READERS;
    public static volatile boolean AUTO_RECONNECT_READERS;
    public static volatile boolean NOTIFY_READER_AVAILABLE;
    public static volatile boolean NOTIFY_READER_CONNECTION;
    public static volatile boolean NOTIFY_BATTERY_STATUS;
    //MultiTag Locate Settings
    public static volatile boolean MULTI_TAG_LOCATE_SORT;
    public static int MULTI_TAG_LOCATE_FOUND_PROXI_PERCENT;
    public static String LAST_CONNECTED_READER = "";
    //Beeper
    public static BEEPER_VOLUME beeperVolume = BEEPER_VOLUME.HIGH_BEEP;
    public static BEEPER_VOLUME sledBeeperVolume = BEEPER_VOLUME.HIGH_BEEP;
    // Singulation control
    public static Antennas.SingulationControl singulationControl;
    // regulatory
    public static RegulatoryConfig regulatory;
    public static Boolean regionNotSet = false;
    // antenna
    public static RFModeTable rfModeTable;
    public static Antennas.AntennaRfConfig antennaRfConfig;
    public static int[] antennaPowerLevel;
    public static Readers readers;
    //Variable to keep track of the unique tags seen

    public static volatile boolean TAG_LIST_LOADED = false;

    public static String strBrandID = "AAAA";
    public static int strBrandIDLogo;
    public static int iUpdateLogo = 0;
    public static int iBrandIDLen = 12;
    public static boolean bBrandCheckStarted = false;
    public static BluetoothDevice latestUnPairedBTDevice;
    public static boolean settingsactivityResumed;
    public static ReaderDevice mReaderDisappeared;
    public static ToneGenerator toneGenerator;
    public static Activity contextSettingDetails = null;
    public static String currentFragment = "";   //for MTC export data, when curr frag is rr it should export as previously.
    public static boolean SHOW_CSV_TAG_NAMES = false;
    public static boolean asciiMode = false;
    public static ProfileContent.ProfilesItem ActiveProfile;
    public static String PreFilterTagID;
    public static boolean NON_MATCHING = false;
    public static String RFID_DATAWEDGE_PROFILE_CREATION = "RFID_DATAWEDGE_PROFILE_CREATION";
    public static String RFID_DATAWEDGE_ENABLE_SCANNER= "RFID_DATAWEDGE_ENABLE_SCANNER";
    public static String RFID_DATAWEDGE_DISABLE_SCANNER = "RFID_DATAWEDGE_DISABLE_SCANNER";
    public static UNIQUE_TAG_REPORT_SETTING reportUniquetags = null;
    public static boolean ledState = false;
    public static int beeperspinner_status;
    public static String TAG = "RFIDDEMO";
    public static boolean brandidcheckenabled = false;
    public static String strCurrentImage = "";
    public static boolean bFound = false;
    public static String packageName;
    public static boolean isReaderConnectedThroughBluetooth = false;
    private static boolean activityVisible;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }
    public static String cycleCountProfileData = null;
    public static final String CACHE_TAGLIST_MATCH_MODE_FILE = ".cacheMatchModeTagFile.csv";  // cache file for taglist match mode
    public static final String CACHE_LOCATE_TAG_FILE = ".cacheLocateTagFile.csv";  // cache file for locate tag

    /**
     * Update the tagIds from tagsReadInventory
     */
    public static void updateTagIDs() {
        if (tagsReadInventory == null)
            return;

        if (tagsReadInventory.size() == 0)
            return;

        if (tagIDs == null) {
            tagIDs = new ArrayList<>();
            for (InventoryListItem i : tagsReadInventory) {
                tagIDs.add(i.getTagID());
            }
        } else if (tagIDs.size() != tagsReadInventory.size()) {
            tagIDs.clear();
            for (InventoryListItem i : tagsReadInventory) {
                tagIDs.add(i.getTagID());
            }
        }/*else{
            //Do Nothing. Array is up to date
        }*/
    }

    //clear saved data
    public static void reset() {

        UNIQUE_TAGS = 0;
        UNIQUE_TAGS_CSV = 0;
        TOTAL_TAGS = 0;
        TAG_READ_RATE = 0;
        mRRStartedTime = 0;
        missedTags = 0;
        matchingTags = 0;

        if (tagsReadInventory != null)
            tagsReadInventory.clear();
        if (tagIDs != null)
            tagIDs.clear();

        if (Application.TAG_LIST_MATCH_MODE) {
            Application.matchingTagsList.clear();
            Application.missingTagsList.clear();
            Application.unknownTagsList.clear();
            Application.tagsReadForSearch.clear();
        }

        mIsInventoryRunning = false;
        inventoryMode = 0;
        memoryBankId = -1;
        if (inventoryList != null)
            inventoryList.clear();

        mConnectedDevice = null;

        INTENT_ID = 100;
        antennaPowerLevel = null;

        //Triggers
        settings_startTrigger = null;
        settings_startTrigger = null;

        //Beeper
        beeperVolume = BEEPER_VOLUME.HIGH_BEEP;

        accessControlTag = null;
        isAccessCriteriaRead = false;

        // reader settings
        regulatory = null;
        regionNotSet = false;

        preFilters = null;
        preFilterIndex = -1;
        PreFilterTag = "";
        PreFilterTagID = "";

        settings_startTrigger = null;
        settings_stopTrigger = null;

        if (versionInfo != null)
            versionInfo.clear();

        BatteryData = null;

        isLocatingTag = false;
        mIsMultiTagLocatingRunning = false;
        TagProximityPercent = -1;
        locateTag = null;
        is_disconnection_requested = false;
        is_connection_requested = false;
        readers = null;
    }


}
