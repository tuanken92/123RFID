package com.zebra.rfidreader.demo.inventory;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.util.ArrayList;

/**
 * Adapter to provide the data for inventory list
 */
public class ModifiedInventoryAdapter extends ArrayAdapter<InventoryListItem> {
    //List to preserve the values when a search takes place
    public ArrayList<InventoryListItem> originalInventoryList = new ArrayList<>();
    //List to store the searched inventory items
    public ArrayList<InventoryListItem> searchItemsList = new ArrayList<>();
    //private ArrayList<InventoryListItem> listItems;
    private Context context;
    //Implement the filter for searching
    private Filter filter;
    private String tagID;

    /**
     * Constructor. Handles the initialization
     *
     * @param context            - context to be used
     * @param textViewResourceId - layout to be used
     */
    public ModifiedInventoryAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId, Application.tagsReadInventory);

        if (!Application.TAG_LIST_MATCH_MODE) {
            originalInventoryList = Application.tagsReadInventory;
            searchItemsList.addAll(Application.tagsReadInventory);
        }

        this.context = context;
    }

    @Override
    public synchronized void add(InventoryListItem object) {
        if (searchItemsList != null) {
            searchItemsList.add(object);
        }
    }

    @Override
    public synchronized void clear() {
        if (searchItemsList != null)
            searchItemsList.clear();
    }

    @Override
    public synchronized InventoryListItem getItem(int position) {
        if (searchItemsList != null)
            return searchItemsList.get(position);
        else
            return null;
    }

    @Override
    public synchronized int getCount() {
        if (searchItemsList != null)
            return searchItemsList.size();
        else
            return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public synchronized View getView(int position, View convertView, ViewGroup parent) {
        InventoryViewHolder holder = null;
        InventoryListItem listItem = searchItemsList.get(position);

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.inventory_list_item, null);
            //Layouts and Views used for displaying a list item.
            LinearLayout textViewWrap = (LinearLayout) convertView
                    .findViewById(R.id.text_wrap);
            TextView text = (TextView) convertView.findViewById(R.id.tagData);
            TextView count = (TextView) convertView.findViewById(R.id.tagCount);
            TextView rssi = (TextView) convertView.findViewById(R.id.tagRSSI);
            TextView memoryBank = (TextView) convertView.findViewById(R.id.memoryBankTitle);
            TextView memoryBankData = (TextView) convertView.findViewById(R.id.memoryBankData);
            TextView pcView = (TextView) convertView.findViewById(R.id.pc);
            TextView rssiView = (TextView) convertView.findViewById(R.id.rssi);
            TextView phaseView = (TextView) convertView.findViewById(R.id.phase);
            TextView channelView = (TextView) convertView.findViewById(R.id.channel);
            TextView csvTagDetailsView = (TextView) convertView.findViewById(R.id.csvTagDetails);

            holder = new InventoryViewHolder(textViewWrap, text, count, rssi, memoryBank, memoryBankData, pcView, rssiView, phaseView, channelView, csvTagDetailsView);
        } else {
            //The item is already inflated. Use it.
            holder = (InventoryViewHolder) convertView.getTag();
        }

        if (listItem.getBrandIDStatus()) {
            holder.getTextView().setTextColor(Color.BLUE);
        } else
            holder.getTextView().setTextColor(Color.BLACK);

        if (RFIDController.asciiMode && (listItem.getTagID().length() == listItem.getText().length())) {
            holder.getTextView().setTextColor(Color.YELLOW);
        }

        SpannableStringBuilder print_tag = new SpannableStringBuilder(listItem.getText());
        for(int i =0; i < print_tag.length(); i++) {
            if(print_tag.charAt(i) == ' ') {
                BackgroundColorSpan bcs = new BackgroundColorSpan(Color.YELLOW);
                print_tag.setSpan(bcs, i, i+1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        holder.getTextView().setText(print_tag);

        if (Application.TAG_LIST_MATCH_MODE) {

            if (listItem.getCount() == 0)
                holder.getTextView().setTextColor(Color.RED);
            else if (listItem.getCount() >= 0 && Application.tagListMap.containsKey(listItem.getTagID())) {
                holder.getTextView().setTextColor(Color.GREEN);
            } else {
                holder.getTextView().setTextColor(Color.GRAY);
            }
        }

        holder.getCountView().setText("" + listItem.getCount());

        holder.getCustRssiView().setText("" + listItem.getRSSI());

        if (listItem.getRSSI() != null)
            holder.getCustRssiView().setVisibility(View.VISIBLE);
        else {
            holder.getCustRssiView().setText("");
            holder.getCustRssiView().setVisibility(View.GONE);
        }

        if (RFIDController.tagStorageSettings != null) {
            for (TAG_FIELD tag_field : RFIDController.tagStorageSettings.getTagFields()) {
                if (tag_field == TAG_FIELD.PEAK_RSSI) {
                    holder.getCustRssiView().setVisibility(View.VISIBLE);
                }
            }
        }

        if (listItem.getMemoryBankData() == null || (listItem.getMemoryBankData() != null && listItem.getMemoryBankData().isEmpty())) {
            convertView.findViewById(R.id.memoryBankData).setVisibility(View.GONE);
            convertView.findViewById(R.id.memoryBankTitle).setVisibility(View.GONE);
        } else {
            if (listItem.getMemoryBank() != null)
                holder.getMemoryBank().setText(listItem.getMemoryBank().toUpperCase() + " MEMORY");
            holder.getMemoryBankData().setText(listItem.getMemoryBankData());
        }
        LinearLayout dataLayout = (LinearLayout) convertView.findViewById(R.id.dataLinearLayout);
        if (dataLayout != null) {
            if (Application.TAG_LIST_MATCH_MODE) {
                ((LinearLayout) (holder.getPcView()).getParent()).setVisibility(View.GONE);
                ((LinearLayout) (holder.getPhaseView()).getParent()).setVisibility(View.GONE);
                ((LinearLayout) (holder.getChannelView()).getParent()).setVisibility(View.GONE);
                ((LinearLayout) (holder.getRssiView()).getParent()).setVisibility(View.GONE);
                if (listItem.getTagDetails() != null) {
                    holder.getCsvTagDetailsView().setText(listItem.getTagDetails());
                } else
                    ((LinearLayout) (holder.getCsvTagDetailsView()).getParent()).setVisibility(View.GONE);
            } else {
                if (listItem.getPC() != null) {
                    holder.getPcView().setText(listItem.getPC());
                } else
                    ((LinearLayout) (holder.getPcView()).getParent()).setVisibility(View.GONE);
                if (listItem.getPhase() != null) {
                    holder.getPhaseView().setText(listItem.getPhase());
                } else
                    ((LinearLayout) (holder.getPhaseView()).getParent()).setVisibility(View.GONE);
                if (listItem.getChannelIndex() != null) {
                    holder.getChannelView().setText(listItem.getChannelIndex());
                } else
                    ((LinearLayout) (holder.getChannelView()).getParent()).setVisibility(View.GONE);
                if (listItem.getRSSI() != null) {
                    holder.getRssiView().setText(listItem.getRSSI());
                } else
                    ((LinearLayout) (holder.getRssiView()).getParent()).setVisibility(View.GONE);
            }
        }
        LinearLayout tagDetails = (LinearLayout) convertView.findViewById(R.id.tagDetails);
        if (!listItem.isVisible()) {
            tagDetails.setVisibility(View.GONE);
            holder.getTextViewWrap().setBackgroundColor(Color.WHITE);
        } else {
            tagDetails.setVisibility(View.VISIBLE);
            if (Application.TAG_LIST_MATCH_MODE && listItem.getTagDetails() != null)
                ((LinearLayout) (holder.getCsvTagDetailsView()).getParent()).setVisibility(View.VISIBLE);
            holder.getTextViewWrap().setBackgroundColor(0x66444444);
        }
        if (Application.TAG_LIST_MATCH_MODE && RFIDController.SHOW_CSV_TAG_NAMES && !holder.getCsvTagDetailsView().getText().equals("unknown")) {
            CharSequence temp = holder.getTextView().getText();
            String data = holder.getCsvTagDetailsView().getText().toString();

            if (data.trim().length() == 0) {
                data = temp.toString();
            }
            holder.getTextView().setText(data);
            holder.getCsvTagDetailsView().setText(temp);
        }
        convertView.setTag(holder);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new TagIDFilter();

        return filter;
    }

    /**
     * Class to act as a custom filter for handling searches
     */
    private class TagIDFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            String prefix = constraint.toString().toLowerCase();

            if (prefix.length() == 0) {
                results.values = new ArrayList<>();
                if (Application.TAG_LIST_MATCH_MODE) {
                    ((ArrayList) (results.values)).addAll(Application.tagsReadForSearch);
                    results.count = Application.tagsReadForSearch.size();
                } else {
                    ((ArrayList) (results.values)).addAll(originalInventoryList);
                    results.count = originalInventoryList.size();
                }
            } else {
                final ArrayList<InventoryListItem> nlist = new ArrayList<InventoryListItem>();

                if (Application.TAG_LIST_MATCH_MODE) {
                    for (final InventoryListItem inventoryItem : Application.tagsReadForSearch) {
                        final String value = inventoryItem.getText().toString().toLowerCase();

                        if (value.contains(prefix)) {
                            nlist.add(inventoryItem);
                        }
                    }
                } else {
                    for (final InventoryListItem inventoryItem : originalInventoryList) {
                        final String value = inventoryItem.getText().toString().toLowerCase();

                        if (value.contains(prefix)) {
                            nlist.add(inventoryItem);
                        }
                    }
                }
                results.values = nlist;
                results.count = nlist.size();
            }

            return results;
        }

        @Override
        protected synchronized void publishResults(CharSequence charSequence, FilterResults filterResults) {
            notifyDataSetChanged();
            if (!Application.TAG_LIST_MATCH_MODE)
                clear();
            searchItemsList = (ArrayList<InventoryListItem>) filterResults.values;
            notifyDataSetInvalidated();
        }
    }
}
