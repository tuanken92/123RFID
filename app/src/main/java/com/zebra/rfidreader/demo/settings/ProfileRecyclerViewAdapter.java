package com.zebra.rfidreader.demo.settings;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.LinkProfileUtil;
import com.zebra.rfidreader.demo.settings.ProfileContent.ProfilesItem;

import java.util.ArrayList;
import java.util.List;

import static com.zebra.rfidreader.demo.rfid.RFIDController.ActiveProfile;
import static com.zebra.rfidreader.demo.rfid.RFIDController.antennaRfConfig;
import static com.zebra.rfidreader.demo.rfid.RFIDController.isLocatingTag;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mConnectedReader;
import static com.zebra.rfidreader.demo.rfid.RFIDController.mIsInventoryRunning;
import static com.zebra.rfidreader.demo.rfid.RFIDController.singulationControl;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ProfilesItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ViewHolder> {

    private final List<ProfilesItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    public ArrayAdapter<String> mLinkProfileAdapter = null;
    public ArrayAdapter<CharSequence> mSessionAdapter;
    public ArrayList<ViewHolder> mViewHolderAdapter = new ArrayList<>();
    public boolean isUserProfileEnable = false;
    LinkProfileUtil linkProfileUtil;
    private int[] powerLevels;
    private boolean LoadingProfile = false;

    public ProfileRecyclerViewAdapter(List<ProfilesItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //
        mViewHolderAdapter.add(position, holder);
        //
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).content);
        holder.enableUI(holder.mItem.bUIEnabled);
        if (mIsInventoryRunning || isLocatingTag)
            holder.contentSwitch.setEnabled(false);
        else
            holder.contentSwitch.setEnabled(true);
        //
        if (ActiveProfile.id.equals(holder.mItem.id))
            holder.mContentView.setTextColor(0xFFFF7043);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadProfile(holder, position);
            }
        });

        linkProfileUtil = LinkProfileUtil.getInstance();

        holder.contentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {

                    String powerLevelStr = String.valueOf(holder.mtextPower.getText());
                    holder.mItem.powerLevel = powerLevelStr.length() == 0 ? 0 : new Integer(powerLevelStr);
                    //holder.mItem.powerLevel = new Integer(String.valueOf(holder.mtextPower.getText()));
                    if (holder.mContentView.getText().equals("User Defined")) {
                        if (linkProfileUtil.getSelectedLinkProfilePosition(ActiveProfile.LinkProfileIndex) != holder.mLinkProfileSpinner.getSelectedItemPosition())
                            holder.mItem.LinkProfileIndex = linkProfileUtil.getSimpleProfileModeIndex(holder.mLinkProfileSpinner.getSelectedItemPosition());
                    } else
                        holder.mItem.LinkProfileIndex = linkProfileUtil.getSimpleProfileModeIndex(holder.mLinkProfileSpinner.getSelectedItemPosition());
                    holder.mItem.SessionIndex = holder.mSession.getSelectedItemPosition();
                    holder.mItem.DPO_On = holder.mDynamicPower.isChecked();
                    holder.mItem.isOn = isChecked;

                    if (null != mListener && !LoadingProfile) {
                        mListener.onProfileFragmentSwitchInteraction(holder.mItem, isChecked);
                    }

                    //
                    if (isChecked) {
                        for (int index = 0; index < mValues.size(); index++) {
                            if (index != position)
                                mValues.get(index).isOn = false;
                        }
                        holder.mContentView.setTextColor(0xFFFF7043);
                        // hide all
                        for (ViewHolder holder1 : mViewHolderAdapter) {
                            if (!ActiveProfile.id.equals(holder1.mItem.id))
                                holder1.mContentView.setTextColor(Color.BLACK);
                        }
                    } else
                        holder.mContentView.setTextColor(Color.BLACK);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    private void LoadProfile(ViewHolder holder, int position) {
        // get util instance
        linkProfileUtil = LinkProfileUtil.getInstance();
        // show details
        holder.mtextViewDetails.setText(holder.mItem.details);
        // get current visibility
        int visibility = holder.mView.findViewById(R.id.profileSettings).getVisibility();
        // hide all
        for (ViewHolder holder1 : mViewHolderAdapter) {
            holder1.mView.findViewById(R.id.profileSettings).setVisibility(View.GONE);
            holder1.mView.findViewById(R.id.contentSwitch).setVisibility(View.GONE);
            if (!ActiveProfile.id.equals(holder1.mItem.id))
                holder1.mContentView.setTextColor(Color.BLACK);
        }
        LoadingProfile = false;
        // show current one
        if (visibility == View.GONE) {
            holder.mView.findViewById(R.id.profileSettings).setVisibility(View.VISIBLE);
            holder.mView.findViewById(R.id.contentSwitch).setVisibility(View.VISIBLE);
            //
            holder.mLinkProfileSpinner.setAdapter(mLinkProfileAdapter);
            holder.mSession.setAdapter(mSessionAdapter);

            if (mConnectedReader != null && mConnectedReader.isConnected() && mConnectedReader.Config.Antennas != null && holder.mContentView.getText().equals("User Defined")) {
                powerLevels = mConnectedReader.ReaderCapabilities.getTransmitPowerLevelValues();
                if (antennaRfConfig != null) {
                    holder.mtextPower.setText(String.valueOf(ActiveProfile.powerLevel));
                    holder.mLinkProfileSpinner.setSelection(linkProfileUtil.getSelectedLinkProfilePosition(ActiveProfile.LinkProfileIndex));
                }
                if (singulationControl != null)
                    holder.mSession.setSelection(ActiveProfile.SessionIndex);
                holder.mDynamicPower.setChecked(ActiveProfile.DPO_On);
            } else {
                holder.mtextPower.setText(new Integer(mValues.get(position).powerLevel).toString());
                holder.mSession.setSelection(mValues.get(position).SessionIndex);
                holder.mLinkProfileSpinner.setSelection(linkProfileUtil.getSelectedLinkProfilePosition(mValues.get(position).LinkProfileIndex));
                holder.mDynamicPower.setChecked(mValues.get(position).DPO_On);
            }
            if (ActiveProfile.id.equals(holder.mItem.id)) {
                LoadingProfile = true;
                holder.contentSwitch.setChecked(true);
            } else
                holder.contentSwitch.setChecked(mValues.get(position).isOn);
            if (mValues.get(position).isOn)
                holder.mContentView.setTextColor(0xFFFF7043);
        } else {
            holder.mView.findViewById(R.id.profileSettings).setVisibility(View.GONE);
            holder.mView.findViewById(R.id.contentSwitch).setVisibility(View.GONE);
        }
        if (holder.mContentView.getText().equals("User Defined")) {
            isUserProfileEnable = true;
        } else {
            isUserProfileEnable = false;
        }

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onProfileFragmentSwitchInteraction(ProfilesItem item, boolean isChecked);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public ProfilesItem mItem;
        public TextView mtextViewDetails;
        public EditText mtextPower;
        public Spinner mLinkProfileSpinner;
        public Spinner mSession;
        public CheckBox mDynamicPower;
        public SwitchCompat contentSwitch;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);
            mtextViewDetails = view.findViewById(R.id.contentDetails);
            mtextPower = view.findViewById(R.id.powerLevelProfile);
            mLinkProfileSpinner = view.findViewById(R.id.linkProfile);
            mSession = view.findViewById(R.id.session);
            mDynamicPower = view.findViewById(R.id.dynamicPower);
            contentSwitch = view.findViewById(R.id.contentSwitch);
        }

        public void enableUI(boolean enabled) {
            mtextPower.setEnabled(enabled);
            mLinkProfileSpinner.setEnabled(enabled);
            mSession.setEnabled(enabled);
            mDynamicPower.setEnabled(enabled);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

    }

}
