package com.zebra.rfidreader.demo.reader_connection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.util.ArrayList;

/**
 * Created by qvfr34 on 2/6/2015.
 */
public class ReaderListAdapter extends ArrayAdapter<ReaderDevice> {
    private final ArrayList<ReaderDevice> readersList;
    private final Context context;
    private final int resourceId;

    public ReaderListAdapter(Context context, int resourceId, ArrayList<ReaderDevice> readersList) {
        super(context, resourceId, readersList);
        this.context = context;
        this.resourceId = resourceId;
        this.readersList = readersList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReaderDevice reader = readersList.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(resourceId, null);
        }
        CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(R.id.reader_checkedtextview);
        checkedTextView.setText(reader.getName() + "\n" + reader.getAddress());

        LinearLayout readerDetail = (LinearLayout) convertView.findViewById(R.id.reader_detail);
        RFIDReader rfidReader = reader.getRFIDReader();
        if (rfidReader != null && rfidReader.isConnected()) {
            checkedTextView.setChecked(true);
            readerDetail.setVisibility(View.VISIBLE);
            if (RFIDController.isBatchModeInventoryRunning != null && RFIDController.isBatchModeInventoryRunning) {
                ((TextView) convertView.findViewById(R.id.tv_model)).setText(context.getResources().getString(R.string.batch_mode_running_title));
                ((TextView) convertView.findViewById(R.id.tv_serial)).setText(context.getResources().getString(R.string.batch_mode_running_title));
            } else if (rfidReader.ReaderCapabilities.getModelName() != null && rfidReader.ReaderCapabilities.getSerialNumber() != null) {
                ((TextView) convertView.findViewById(R.id.tv_model)).setText(rfidReader.ReaderCapabilities.getModelName());
                ((TextView) convertView.findViewById(R.id.tv_serial)).setText(rfidReader.ReaderCapabilities.getSerialNumber());
            }

        } else {
            readerDetail.setVisibility(View.GONE);
            checkedTextView.setChecked(false);
        }
        return convertView;
    }
}
