package com.zebra.rfidreader.demo.settings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.settings.AdvancedOptionItemFragment.OnAdvancedListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnAdvancedListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AdvancedOptionItemAdapter extends RecyclerView.Adapter<AdvancedOptionItemAdapter.ViewHolder> {

    private final List<AdvancedOptionsContent.SettingItem> mValues;
    private final OnAdvancedListFragmentInteractionListener mListener;

    public AdvancedOptionItemAdapter(List<AdvancedOptionsContent.SettingItem> items, OnAdvancedListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_advancedoptionitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.item_Image.setImageResource(mValues.get(position).icon);
        holder.mContentView.setText(mValues.get(position).content);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.OnAdvancedListFragmentInteractionListener(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView item_Image;
        public final TextView mContentView;
        public AdvancedOptionsContent.SettingItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            item_Image = (ImageView) view.findViewById(R.id.item_Image);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
