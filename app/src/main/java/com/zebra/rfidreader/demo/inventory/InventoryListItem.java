package com.zebra.rfidreader.demo.inventory;

import com.zebra.rfidreader.demo.common.hextoascii;
import com.zebra.rfidreader.demo.rfid.RFIDController;

/**
 * Class which contains the data to be displayed in the inventory list
 */
public class InventoryListItem {
    public boolean brandIDfound;
    //Actual contents of each inventory item
    private String tagID;
    private int count = 1;
    private String memoryBank;
    private String memoryBankData;
    private String RSSI;
    private String PC;
    private String phase;
    private String channelIndex;
    private boolean isVisible;
    private String tagDetails;
    private String tagStatus;


    /**
     * Construtor. Handles the initialization.
     *
     * @param tagID          - Tag ID
     * @param count          - No of times the tag was encountered
     * @param memoryBank     - memory bank
     * @param memoryBankData - memoryBankData associated // * @param EPCId - EPCID of the tag
     * @param RSSI           - RSSI value of the tag
     * @param phase          - Phase of the tag
     * @param channelIndex   - ChannelIndex
     * @param PC             - PC value for the tag
     */
    public InventoryListItem(String tagID, int count, String memoryBank, String memoryBankData, String RSSI, String phase, String channelIndex, String PC) {
        this.tagID = tagID;
        this.count = count;
        this.memoryBank = memoryBank;
        this.memoryBankData = memoryBankData;
        this.PC = PC;
        this.channelIndex = channelIndex;
        this.phase = phase;
        this.RSSI = RSSI;
        this.isVisible = false;
        if (memoryBankData != null && !memoryBankData.isEmpty()) {
            memoryBankData.replace("\n", "");
        }

    }

    /**
     * get tag's memory bank type
     *
     * @return memory bank of the tag
     */
    public String getMemoryBank() {
        return memoryBank;
    }

    /**
     * set memory bank
     *
     * @param memoryBank memory bank
     */
    public void setMemoryBank(String memoryBank) {
        this.memoryBank = memoryBank;
    }

    /**
     * method which will tell whether tag details are visible
     *
     * @return true if tag details are showing currently or false if tag details are hidden
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * set visibility of the tag details
     *
     * @param visibility true for showing tag details false for hiding tag details
     */
    public void setVisible(boolean visibility) {
        this.isVisible = visibility;
    }

    /**
     * method to get tag read count
     *
     * @return tag read count
     */
    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final InventoryListItem other = (InventoryListItem) obj;
        return !((this.tagID == null) ? (other.tagID != null) : !this.tagID.equals(other.tagID));

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.tagID != null ? this.tagID.hashCode() : 0);
        return hash;
    }

    /**
     * Increment the count for the tag. Called when the tag is encountered multiple times.
     */
    public void incrementCount() {
        count++;
    }

    /**
     * Increment the count for the tag. Called when the tag is encountered multiple times.
     */
    public void incrementCountWithTagSeenCount(int tagseencount) {
        count += tagseencount;
    }

    /**
     * method to get tag id
     *
     * @return tag id
     */
    public String getText() {
        if (RFIDController.asciiMode)
            return hextoascii.convert(tagID);
        else
            return tagID;
    }

    /**
     * method to set tag id
     *
     * @param text tag id
     */
    public void setText(String text) {
        this.tagID = text;
    }

    /**
     * method to get RSSI value of tag
     *
     * @return RSSI of tag
     */
    public String getRSSI() {
        return RSSI;
    }

    /**
     * method to set RSSI value of tag
     *
     * @param RSSI RSSI of tag
     */
    public void setRSSI(String RSSI) {
        this.RSSI = RSSI;
    }

    /**
     * method to get PC value of tag
     *
     * @return PC of tag
     */
    public String getPC() {
        return PC;
    }

    /**
     * method to set PC value of tag
     *
     * @param PC PC of tag
     */
    public void setPC(String PC) {
        this.PC = PC;
    }

    /**
     * method to get phse value of tag
     *
     * @return phse of tag
     */
    public String getPhase() {
        return phase;
    }

    /**
     * method to set phsae value of tag
     *
     * @param phase phase of tag
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * method to get Channel index value of tag
     *
     * @return Channel index of tag
     */
    public String getChannelIndex() {
        return channelIndex;
    }

    /**
     * method to set Channel index value of tag
     *
     * @param channelIndex channel index of tag
     */
    public void setChannelIndex(String channelIndex) {
        this.channelIndex = channelIndex;
    }

    /**
     * method to get memory bank data of tag
     *
     * @return memory bank data of tag
     */
    public String getMemoryBankData() {
        if (RFIDController.asciiMode) {
            return hextoascii.convert(memoryBankData);
        } else {
            return memoryBankData;
        }
    }

    /**
     * method to set memory bank data of tag
     *
     * @param memoryBankData memory bank data of tag
     */
    public void setMemoryBankData(String memoryBankData) {
        this.memoryBankData = memoryBankData;
    }

    /**
     * method to get tag id
     *
     * @return id of tag
     */
    public String getTagID() {
        return tagID;
    }

    public String getTagDetails() {
        return tagDetails;
    }

    public void setTagDetails(String tagDetails) {
        this.tagDetails = tagDetails;
    }

    public String getTagStatus() {
        return tagStatus;
    }

    public void setTagStatus(String tagStatus) {
        this.tagStatus = tagStatus;
    }

    public boolean getBrandIDStatus() {
        //Log.i("INVENTORYLISTITEM", "get brandid status" + brandIDfound);
        return brandIDfound;
    }

    public void setBrandIDStatus(boolean brandIDfound) {
        //Log.i("INVENTORYLISTITEM", "set brandid status" + brandIDfound);
        this.brandIDfound = brandIDfound;
    }
}

