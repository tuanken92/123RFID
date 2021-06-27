package com.zebra.rfidreader.demo.locate_tag.multitag_locate;

import com.zebra.rfidreader.demo.common.hextoascii;
import com.zebra.rfidreader.demo.rfid.RFIDController;

/**
 * Created by PKF847 on 7/29/2017.
 */

public class MultiTagLocateListItem implements Comparable<MultiTagLocateListItem> {
    //Actual contents of each inventory item
    private String tagID;
    private String rssiValue;
    private int readCount = 0;
    private short proximityPercent = 0;
    private boolean isVisible = true;

    /**
     * Construtor. Handles the initialization.
     *
     * @param tagID          - Tag ID
     * @param readCount          - No of times the tag was encountered
     * @param proximityPercent   -  tag's proximity percent value
     */
    public MultiTagLocateListItem(String tagID, String rssi, int readCount, short proximityPercent) {
        this.tagID = tagID;
        this.rssiValue = rssi;
        this.readCount = readCount;
        this.proximityPercent = proximityPercent;
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
    public int getReadCount() {
        return readCount;
    }

    /**
     * set the count for the tag. Called when the tag is encountered multiple times.
     */
    public void setReadCount(int value) {
        this.readCount = value;
    }

    /**
     * method to get tag id
     *
     * @return tag id
     */
    public String getTagID() {
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
    public void setTagID(String text) {
        this.tagID = text;
    }

    /**
     * method to get proximity percent value
     *
     * @return proximity percent value
     */
    public short getProximityPercent() {
        return proximityPercent;
    }

    /**
     * method to set proximity percent value
     *
     * @param value proximity percent value
     */
    public void setProximityPercent(short value) {
        this.proximityPercent = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final MultiTagLocateListItem other = (MultiTagLocateListItem) obj;
        return !((this.tagID == null) ? (other.tagID != null) : !this.tagID.equals(other.tagID));

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.tagID != null ? this.tagID.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(MultiTagLocateListItem another) {
        if(this.proximityPercent > another.proximityPercent )
            return 1;
        else if (this.proximityPercent < another.proximityPercent)
            return -1;
        else
        {
            if(this.readCount > another.readCount )
                return 1;
            else if (this.readCount < another.readCount)
                return -1;
            else return this.tagID.compareToIgnoreCase(another.tagID);
        }
    }

    public String getRssiValue() {
        return rssiValue;
    }

    public void setRssiValue(String rssiValue) {
        this.rssiValue = rssiValue;
    }
}

