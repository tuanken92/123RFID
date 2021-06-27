package com.zebra.rfidreader.demo.locate_tag.multitag_locate;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zebra.rfidreader.demo.R;

/**
 * Created by PKF847 on 7/30/2017.
 */

public class MultiTagLocateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    //References for the fields in the inventory list
    private LinearLayout textViewWrap;
    private TextView tagIDView;
    private TextView readCountView;
    private ProgressBar proxiPerenctProgressView;
    private TextView proxiPerenctValueView;
    private MultiTagLocateInventoryAdapter.OnItemClickListner onItemClickListner;

    public MultiTagLocateViewHolder(View itemView, MultiTagLocateInventoryAdapter.OnItemClickListner multiTagItemLongClickListener) {
        super(itemView);
        this.textViewWrap = (LinearLayout) itemView.findViewById(R.id.text_wrap);
        this.tagIDView = (TextView) itemView.findViewById(R.id.tagData);
        this.readCountView = (TextView) itemView.findViewById(R.id.tagCount);
        this.proxiPerenctProgressView = (ProgressBar) itemView.findViewById(R.id.tagProxiPercent_progressBar);
        this.proxiPerenctValueView = (TextView) itemView.findViewById(R.id.tagProxiPerenct);
        onItemClickListner = multiTagItemLongClickListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onItemClickListner.onItemClick(getAdapterPosition());
    }

    public LinearLayout getTextViewWrap() {
        return textViewWrap;
    }

    public TextView getTagIDView() {
        return tagIDView;
    }

    public TextView getReadCountView() {
        return readCountView;
    }

    public ProgressBar getProxiPerenctProgressView() {
        return proxiPerenctProgressView;
    }

    public TextView getProxiPerenctValueView() {
        return proxiPerenctValueView;
    }
}

