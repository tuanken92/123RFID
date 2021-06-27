package com.zebra.rfidreader.demo.common;

import android.util.Log;

/**
 * Class which contains pre filter settings
 */
public class PreFilters {
    private static final String TAG = "PreFilters";
    private String tag;
    private String memoryBank;
    private int offset;
    private int bitCount;
    private int action;
    private int target;
    private boolean isFilterEnabled;

    /**
     * Constructor of the {@link com.zebra.rfidreader.demo.common.PreFilters}
     *
     * @param tag             tag id
     * @param memoryBank      memory bank of the tag
     * @param offset          offset of the pre filter
     * @param patternBitCount
     * @param action          action
     * @param target          target
     * @param isFilterEnabled whether filter enabled or not
     */
    public PreFilters(String tag, String memoryBank, int offset, int patternBitCount, int action, int target, boolean isFilterEnabled) {
        this.tag = tag;
        this.memoryBank = memoryBank;
        this.offset = offset;
        this.bitCount = patternBitCount;
        this.action = action;
        this.target = target;
        this.isFilterEnabled = isFilterEnabled;
    }

    public PreFilters(com.zebra.rfid.api3.PreFilters.PreFilter preFilterData) {
        String tag = preFilterData.getStringTagPattern();
        String memBank = preFilterData.getMemoryBank().toString();
        int action = preFilterData.StateAwareAction.getStateAwareAction().getValue();//preFilterData.getFilterAction().ordinal;
        int target = preFilterData.StateAwareAction.getTarget().getValue();
        this.setTag(tag);
        this.setMemoryBank(memBank);
        this.setAction(action);
        this.setTarget(target);
        Log.d(TAG, String.format("Filling prefilter data: %s %s %d %d", tag, memBank, action, target));
    }

    /**
     * method to get tag pattern of the pre filter
     *
     * @return tag pattern
     */
    public String getTag() {
        return tag;
    }

    /**
     * method to set tag pattern of the pre filter
     *
     * @param tag tag pattern
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * method to get  memory bank
     *
     * @return memory bank
     */
    public String getMemoryBank() {
        return memoryBank;
    }

    /**
     * method to set  memory bank
     *
     * @param memoryBank memory bank
     */
    public void setMemoryBank(String memoryBank) {
        this.memoryBank = memoryBank;
    }

    /**
     * method to get  offset
     *
     * @return offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * method to set  offset
     *
     * @param offset offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    // getter and setter for bit count
    public int getBitCount() {
        return bitCount;
    }

    public void setBitCount(int bitCount) {
        this.bitCount = bitCount;
    }

    /**
     * method to get  action
     *
     * @return action
     */
    public int getAction() {
        return action;
    }

    /**
     * method to set  action
     *
     * @param action action
     */
    public void setAction(int action) {
        this.action = action;
    }

    /**
     * method to get  target
     *
     * @return target
     */
    public int getTarget() {
        return target;
    }

    /**
     * method to set  target
     *
     * @param target target
     */
    public void setTarget(int target) {
        this.target = target;
    }

    /**
     * method to know whether pre filter enabled
     *
     * @return true if enabled otherwise it will be false
     */
    public boolean isFilterEnabled() {
        return isFilterEnabled;
    }

    /**
     * method to set whether prefilter enabled
     *
     * @param isFilterEnabled true if enabled otherwise it will be false
     */
    public void setFilterEnabled(boolean isFilterEnabled) {
        this.isFilterEnabled = isFilterEnabled;
    }

    public boolean equals(PreFilters preFilter) {
        if (this.isFilterEnabled == preFilter.isFilterEnabled() && this.tag.equalsIgnoreCase(preFilter.getTag()) && this.memoryBank.equalsIgnoreCase(preFilter.getMemoryBank()) && this.offset == preFilter.getOffset() && this.action == preFilter.getAction() && this.target == preFilter.getTarget())
            return true;
        return false;
    }
}
