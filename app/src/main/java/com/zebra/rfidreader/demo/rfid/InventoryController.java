package com.zebra.rfidreader.demo.rfid;

import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.BATCH_MODE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.inventory.InventoryListItem;

import java.util.ArrayList;

public class InventoryController {

    protected InventoryController() {

    }


    public void inventoryWithMemoryBank(String memoryBankID, RfidListeners rfidListeners) {

        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected()) {
            TagAccess tagAccess = new TagAccess();
            TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
            //Set the param values
            readAccessParams.setCount(0);
            readAccessParams.setOffset(0);
            if ("RESERVED".equalsIgnoreCase(memoryBankID))
                readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_RESERVED);
            if ("EPC".equalsIgnoreCase(memoryBankID))
                readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
            if ("TID".equalsIgnoreCase(memoryBankID))
                readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
            if ("USER".equalsIgnoreCase(memoryBankID))
                readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_USER);
            try {
                //Read command with readAccessParams and accessFilter as null to read all the tags
                RFIDController.mConnectedReader.Actions.TagAccess.readEvent(readAccessParams, null, null);
                RFIDController.mIsInventoryRunning = true;
                rfidListeners.onSuccess(null);

            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                rfidListeners.onFailure(e);
            }

        } else
            rfidListeners.onFailure("No Active Connection with Reader");
    }

    public void performInventory(final RfidListeners rfidListeners) {
        new AsyncTask<Void, Void, Boolean>() {

            OperationFailureException exception;
            InvalidUsageException exceptionIN;

            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean isSuccess = true;
                if (RFIDController.reportUniquetags != null && RFIDController.reportUniquetags.getValue() == 1) {
                    RFIDController.mConnectedReader.Actions.purgeTags();
                }
                //Perform inventory
                try {
                    if (RFIDController.brandidcheckenabled) {
                        /* Perform Brandcheck for NXP tags*/
                        if (Application.strBrandID != null && Application.strBrandID.length() > 0) {
                            RFIDController.mConnectedReader.Actions.TagAccess.NXP.performBrandCheck(Application.strBrandID, Application.iBrandIDLen);
                            Application.bBrandCheckStarted = true;
                        } else
                            RFIDController.mConnectedReader.Actions.Inventory.perform();
                    } else
                        RFIDController.mConnectedReader.Actions.Inventory.perform();
                    RFIDController.mIsInventoryRunning = true;
                    rfidListeners.onSuccess(null);
                    Log.d(RFIDController.TAG, "Inventory.perform");
                } catch (InvalidUsageException e) {
                    isSuccess = false;
                    exceptionIN = e;
                    e.printStackTrace();
                } catch (final OperationFailureException e) {
                    isSuccess = false;
                    exception = e;
                    e.printStackTrace();
                }
                return isSuccess;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (exception != null) {
                    if (RFIDController.batchMode != -1) {
                        if (RFIDController.batchMode == BATCH_MODE.ENABLE.getValue()) {
                            RFIDController.isBatchModeInventoryRunning = true;
                        }
                    }
                    rfidListeners.onFailure(exception);

                } else if (exceptionIN != null) {
                    rfidListeners.onFailure(exceptionIN);
                } else
                    rfidListeners.onSuccess(null);


            }
        }.execute();

    }

    public void stopInventory(final RfidListeners rfidListeners) {
        RFIDController.isInventoryAborted = true;
        new AsyncTask<Void, Void, Boolean>() {

            OperationFailureException exception;
            InvalidUsageException exceptionIN;


            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean isSuccess = false;
                try {
                    RFIDController.mConnectedReader.Actions.Inventory.stop();
                    synchronized (RFIDController.isInventoryAborted) {
                        RFIDController.isInventoryAborted.notify();
                    }
                    if (((RFIDController.settings_startTrigger != null &&
                            (RFIDController.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD
                                    || RFIDController.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC)) ||
                            RFIDController.getRepeatTriggers())) {
                    } else
                        isSuccess = true;
                    Log.d(RFIDController.TAG, "Inventory.stop");
                } catch (InvalidUsageException e) {
                    isSuccess = false;
                    exceptionIN = e;
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                    isSuccess = false;
                    exception = e;
                }
                return isSuccess;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (exception != null) {
                    rfidListeners.onFailure(exception);
                } else if (exceptionIN != null) {
                    rfidListeners.onFailure(exceptionIN);
                } else {
                    if (result)
                        rfidListeners.onSuccess(null);
                    else
                        rfidListeners.onFailure((Exception) null);
                }
            }


        }.execute();

    }


    public void updateTagIDs() {
        if (Application.tagsReadInventory == null)
            return;
        if (Application.tagsReadInventory.size() == 0)
            return;
        if (Application.tagIDs == null) {
            Application.tagIDs = new ArrayList<>();
            for (InventoryListItem i : Application.tagsReadInventory) {
                Application.tagIDs.add(i.getTagID());
            }
        } else if (Application.tagIDs.size() != Application.tagsReadInventory.size()) {
            Application.tagIDs.clear();
            for (InventoryListItem i : Application.tagsReadInventory) {
                Application.tagIDs.add(i.getTagID());
            }
        }/*else{
            //Do Nothing. Array is up to date
        }*/
    }


}
