package com.zebra.rfidreader.demo.common;

import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfidreader.demo.inventory.InventoryListItem;

/**
 * Interfaces for all the status, event and notification handlers regarding different fragments
 *
 * @author qktd34
 */
public class ResponseHandlerInterfaces {
    /**
     * Interface to be implemented by the Fragments to handle the tag Response
     */
    public interface ResponseTagHandler {
        /**
         * method to handle tag data
         *
         * @param inventoryListItem - tag data
         * @param isAddedToList     - true if tag is added to the list.
         *                          - false if tag is updated in the list.
         */
        void handleTagResponse(InventoryListItem inventoryListItem, boolean isAddedToList);
    }

    /**
     * Interface to be implemented by the Fragments to handle the tag Response
     */
    public interface ResponseStatusHandler {
        /**
         * method to handle tag data
         *
         * @param results - status data
         */
        void handleStatusResponse(RFIDResults results);
    }

    /**
     * Interface to be implemented by the Fragments to handle physical trigger events of the reader
     */
    public interface TriggerEventHandler {
        /**
         * method to handle trigger press event
         */
        void triggerPressEventRecieved();

        /**
         * method to handle trigger release event
         */
        void triggerReleaseEventRecieved();
    }

    public interface ReaderDeviceFoundHandler {

        /**
         * This method will be called when reader device got connected.
         *
         * @param device - connected reader device
         */
        void ReaderDeviceConnected(ReaderDevice device);

        /**
         * This method will be called when reader device got disconnected.
         *
         * @param device - disconnected reader device
         */
        void ReaderDeviceDisConnected(ReaderDevice device);

        /**
         * This method will be called when connection process with reader device got failed.
         *
         * @param device - reader device
         */
        void ReaderDeviceConnFailed(ReaderDevice device);
    }

    /**
     * Interface to be implemented by the Fragments to handle batch mode events
     */
    public interface BatchModeEventHandler {
        /**
         * method to handle batch mode event
         */
        void batchModeEventReceived();
    }

    /**
     * Interface to be implemented by the Fragments to handle battery notifications
     */
    public interface BatteryNotificationHandler {
        /**
         * method to handle device battery status data
         *
         * @param level    - battery level
         * @param charging - specifies whether device is charging or not
         * @param cause    - reason for receiving battery notification
         */
        void deviceStatusReceived(int level, boolean charging, String cause);
    }
}
