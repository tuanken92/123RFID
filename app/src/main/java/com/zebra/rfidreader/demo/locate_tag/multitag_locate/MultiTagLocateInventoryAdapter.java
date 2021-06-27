package com.zebra.rfidreader.demo.locate_tag.multitag_locate;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Adapter to provide the data for inventory list
 */
public class MultiTagLocateInventoryAdapter extends RecyclerView.Adapter<MultiTagLocateViewHolder> {
    //List to preserve the values when a search takes place
    private ArrayList<MultiTagLocateListItem> multiTagLocateItemList;
    private OnItemClickListner onItemClickListener;

    public interface OnItemClickListner {
        void onItemClick(int position);
    }

    /**
     * Constructor. Handles the initialization
     */
    public MultiTagLocateInventoryAdapter(OnItemClickListner listener) {
        multiTagLocateItemList = Application.multiTagLocateActiveTagItemList;
        onItemClickListener = listener;
    }

    @Override
    public MultiTagLocateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View view = inflater.inflate(R.layout.multi_tag_locate_list_item, viewGroup, false);

        // Return a new holder instance
        MultiTagLocateViewHolder viewHolder = new MultiTagLocateViewHolder(view, onItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MultiTagLocateViewHolder multiTagLocateViewHolder, int position) {
        MultiTagLocateListItem listItem = multiTagLocateItemList.get(position);
        if(RFIDController.asciiMode) {
            SpannableStringBuilder print_tag = new SpannableStringBuilder(listItem.getTagID());
            for(int i =0; i < print_tag.length(); i++) {
                if(print_tag.charAt(i) == ' ') {
                    BackgroundColorSpan bcs = new BackgroundColorSpan(Color.YELLOW);
                    print_tag.setSpan(bcs, i, i+1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            multiTagLocateViewHolder.getTagIDView().setText(print_tag);
        } else {
            multiTagLocateViewHolder.getTagIDView().setText(listItem.getTagID());
        }
        multiTagLocateViewHolder.getReadCountView().setText("" + listItem.getReadCount());
        multiTagLocateViewHolder.getProxiPerenctProgressView().setProgress(listItem.getProximityPercent());
        multiTagLocateViewHolder.getProxiPerenctValueView().setText(listItem.getProximityPercent() + "%");

        if(listItem.getProximityPercent() == 0) {
            multiTagLocateViewHolder.getTagIDView().setTextColor(Color.GRAY);
        } else if(listItem.getProximityPercent() < Application.MULTI_TAG_LOCATE_FOUND_PROXI_PERCENT) {
            multiTagLocateViewHolder.getTagIDView().setTextColor(Color.BLACK);
        } else {
            multiTagLocateViewHolder.getTagIDView().setTextColor(Color.GREEN);
        }
    }

    public synchronized void add(MultiTagLocateListItem object) {
        if (multiTagLocateItemList != null) {
            if (!multiTagLocateItemList.contains(object))
                multiTagLocateItemList.add(object);
        }
    }

    public synchronized void add(String tagID) {
        if(multiTagLocateItemList != null ) {
            if(Application.multiTagLocateTagListMap.get(tagID) != null) {
                Application.multiTagLocateTagListMap.get(tagID).setReadCount(0);
                Application.multiTagLocateTagListMap.get(tagID).setProximityPercent((short)0);
                add(Application.multiTagLocateTagListMap.get(tagID));
            } else {
                MultiTagLocateListItem item = new MultiTagLocateListItem(tagID, "0",0, (short)0);
                add(item);
            }
        }
    }

    public synchronized void clear() {
        if (multiTagLocateItemList != null)
            multiTagLocateItemList.clear();
    }

    public synchronized MultiTagLocateListItem getItem(int position) {
        if (multiTagLocateItemList != null)
            return multiTagLocateItemList.get(position);
        else
            return null;
    }

    public synchronized void remove(String tagID) {
        if(multiTagLocateItemList != null ) {
            MultiTagLocateListItem item = Application.multiTagLocateTagListMap.get(tagID);
            if(item == null)
                item = new MultiTagLocateListItem(tagID,"0", 0, (short)0);
            multiTagLocateItemList.remove(item);
        }
    }

    public synchronized void sortItemList() {
        if (multiTagLocateItemList != null) {
            //Collections.sort(multiTagLocateItemList);
            Collections.sort(multiTagLocateItemList, MultiTagLocateInventoryAdapter.tagItemComparator);
        }
    }

    public static Comparator<MultiTagLocateListItem> tagItemComparator = new Comparator<MultiTagLocateListItem>() {
        @Override
        public int compare(MultiTagLocateListItem lhs, MultiTagLocateListItem rhs) {
            return (lhs.compareTo(rhs) * (-1));
        }
    };

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return multiTagLocateItemList.size();
    }
}
