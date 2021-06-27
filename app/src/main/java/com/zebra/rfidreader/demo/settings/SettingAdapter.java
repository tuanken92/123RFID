package com.zebra.rfidreader.demo.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.rfidreader.demo.R;

import java.util.List;

/**
 * Class to hold the data for Settings UI
 */
public class SettingAdapter extends ArrayAdapter {
    Context mContext;
    List<SettingsContent.SettingItem> mData = null;

    /**
     * Method to initialize the Adapter
     *
     * @param context  - Context to be used
     * @param resource - Resource
     * @param objects  - Data for settings
     */
    public SettingAdapter(Context context, int resource, List<SettingsContent.SettingItem> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.setting_list, parent, false);
        }
        SettingsContent.SettingItem item = mData.get(position);
        //Text for the setting item
        TextView label1 = (TextView) convertView.findViewById(R.id.firstLine);
        label1.setText(item.content);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        //Set icon here
        icon.setImageResource(item.icon);
        return convertView;
    }
}
