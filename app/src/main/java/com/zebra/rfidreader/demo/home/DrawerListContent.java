package com.zebra.rfidreader.demo.home;

import com.zebra.rfidreader.demo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to hold the data for Navigation Drawer Items
 */
public class DrawerListContent {
    //An array of sample (Settings) items.
    public static List<DrawerItem> ITEMS = new ArrayList<>();

    //A map of sample (Settings) items, by ID.
    public static Map<String, DrawerItem> ITEM_MAP = new HashMap<>();

    static {
        // Add items.
        //addItem(new DrawerItem("1", "Home", R.drawable.app_icon));
        addItem(new DrawerItem("1", "Rapid Read", R.drawable.dl_rr));
        addItem(new DrawerItem("2", "Inventory", R.drawable.dl_inv));
        addItem(new DrawerItem("3", "Locate Tag", R.drawable.dl_loc));
        addItem(new DrawerItem("4", "Settings", R.drawable.dl_sett));
        //addItem(new DrawerItem("5", "Readers List",R.drawable.dl_rdl));
        addItem(new DrawerItem("5", "Access Control", R.drawable.dl_access));
        addItem(new DrawerItem("6", "Pre Filters", R.drawable.dl_filters));
        addItem(new DrawerItem("7", "Readers List", R.drawable.dl_rdl));
        addItem(new DrawerItem("8", "About", R.drawable.dl_about));
    }

    /**
     * Method to add a new item
     *
     * @param item - Item to be added
     */
    private static void addItem(DrawerItem item) {

        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A Drawer item represents an entry in the navigation drawer.
     */
    public static class DrawerItem {
        public String id;
        public String content;
        public int icon;

        public DrawerItem(String id, String content, int icon_id) {
            this.id = id;
            this.content = content;
            this.icon = icon_id;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
