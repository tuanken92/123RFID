package com.zebra.rfidreader.demo.settings;

import com.zebra.rfidreader.demo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class AdvancedOptionsContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<AdvancedOptionsContent.SettingItem> ITEMS = new ArrayList<>();
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, AdvancedOptionsContent.SettingItem> ITEM_MAP = new HashMap<>();
    public static int DPO_ITEM_INDEX = 5;

    static {
        addItem(new AdvancedOptionsContent.SettingItem(R.id.antenna + "", "Antenna",/*"Set Antenna parameters",*/R.drawable.settings_antenna));
        addItem(new AdvancedOptionsContent.SettingItem(R.id.singulation_control + "", "Singulation Control",/*"Set target & action",*/R.drawable.settings_singulation_control));
        addItem(new AdvancedOptionsContent.SettingItem(R.id.start_stop_triggers + "", "Start\\Stop Triggers",/*"Region and channels",*/R.drawable.settings_start_stop_triggers));
        addItem(new AdvancedOptionsContent.SettingItem(R.id.tag_reporting + "", "Tag Reporting",/*"Triggers settings",*/R.drawable.settings_tag_reporting));
        addItem(new AdvancedOptionsContent.SettingItem(R.id.save_configuration + "", "Save configuration",/*"Version information",*/R.drawable.settings_save_configuration));
        addItem(new AdvancedOptionsContent.SettingItem(R.id.power_management + "", "Power Management",/*"Version information",*/R.drawable.title_dpo_disabled));
    }

    private static void addItem(AdvancedOptionsContent.SettingItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }


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
