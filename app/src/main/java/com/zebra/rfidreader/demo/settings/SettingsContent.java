package com.zebra.rfidreader.demo.settings;

import com.zebra.rfidreader.demo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.(Added by Pragnesh)
 */
public class SettingsContent {
    /**
     * An array of sample (Settings) items.
     */
    public static List<SettingItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (Settings) items, by ID.
     */
    public static Map<String, SettingItem> ITEM_MAP = new HashMap<>();

    static {
        // Add items.
        addItem(new SettingItem(R.id.readers_list + "", "Readers List"/*,"Available Readers"*/, R.drawable.settings_readers_list));
        addItem(new SettingItem(R.id.application + "", "Application"/*,"Settings"*/, R.drawable.settings_management));
        addItem(new SettingItem(R.id.profiles + "", "Profiles",/*"Set Antenna parameters",*/R.drawable.profiles));
//        addItem(new SettingItem("4", "Antenna",/*"Set Antenna parameters",*/R.drawable.title_antn));
//        addItem(new SettingItem("5", "Singulation Control",/*"Set target & action",*/R.drawable.title_singl));
//        addItem(new SettingItem("6", "Start\\Stop Triggers",/*"Region and channels",*/R.drawable.title_strstp));
//        addItem(new SettingItem("7", "Tag Reporting",/*"Triggers settings",*/R.drawable.title_tags));
        addItem(new SettingItem(R.id.advanced_options + "", "Advanced Reader Options",/*"Tag Settings",*/R.drawable.settings_rfid_accessory));
        addItem(new SettingItem(R.id.regulatory + "", "Regulatory",/*"Host and sled volumes",*/R.drawable.settings_regulatory));
        addItem(new SettingItem(R.id.battery + "", "Battery",/*"Configurations",*/R.drawable.settings_battery));
        addItem(new SettingItem(R.id.beeper + "", "Beeper",/*"Status",*/R.drawable.settings_beeper));
        addItem(new SettingItem(R.id.led + "", "LED ",/*"Status",*/R.drawable.settings_led));
//        addItem(new SettingItem("13", "Save Configuration",/*"Tag Settings",*/R.drawable.title_save));


    }

    private static void addItem(SettingItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A Settings item representing a piece of content.
     */
    public static class SettingItem {
        public String id;
        public String content;
        //public String subcontent;
        public int icon;

        public SettingItem(String id, String content/*,String subcontent*/, int icon_id) {
            this.id = id;
            this.content = content;
            //this.subcontent = subcontent;
            this.icon = icon_id;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
