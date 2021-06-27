package com.zebra.rfidreader.demo.rfid;

import android.os.AsyncTask;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.LOCK_DATA_FIELD;
import com.zebra.rfid.api3.LOCK_PRIVILEGE;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfidreader.demo.common.LinkProfileUtil;
import com.zebra.rfidreader.demo.common.asciitohex;

public class AccessOperationController {


    protected AccessOperationController() {
    }

    public static MEMORY_BANK getAccessRWMemoryBank(String bankItem) {
        if ("RESV".equalsIgnoreCase(bankItem) || bankItem.contains("PASSWORD"))
            return MEMORY_BANK.MEMORY_BANK_RESERVED;
        else if ("EPC".equalsIgnoreCase(bankItem) || bankItem.contains("PC"))
            return MEMORY_BANK.MEMORY_BANK_EPC;
        else if ("TID".equalsIgnoreCase(bankItem))
            return MEMORY_BANK.MEMORY_BANK_TID;
        else if ("USER".equalsIgnoreCase(bankItem))
            return MEMORY_BANK.MEMORY_BANK_USER;
        return MEMORY_BANK.MEMORY_BANK_EPC;
    }

    public void accessOperationsRead(final String tagValue, String offsetText, String lengthText, String accessRwPassword, String bankItem, final RfidListeners rfidListeners) {
        RFIDController.accessControlTag = tagValue;
        RFIDController.isAccessCriteriaRead = true;
        TagAccess tagAccess = new TagAccess();
        final TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
        try {
            readAccessParams.setAccessPassword(Long.decode("0X" + accessRwPassword));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            rfidListeners.onFailure("Password field is empty!");
        }
        readAccessParams.setCount(Integer.parseInt(lengthText));
        readAccessParams.setMemoryBank(getAccessRWMemoryBank(bankItem));
        readAccessParams.setOffset(Integer.parseInt(offsetText));
        new AsyncTask<Void, Void, TagData>() {
            private InvalidUsageException invalidUsageException;
            private OperationFailureException operationFailureException;

            @Override
            protected TagData doInBackground(Void... voids) {
                try {
                    setAccessProfile(true);
                    final TagData tagData = RFIDController.mConnectedReader.Actions.TagAccess.readWait(tagValue, readAccessParams, null, true);
                    return tagData;
                } catch (InvalidUsageException e) {
                    invalidUsageException = e;
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    operationFailureException = e;
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(TagData tagData) {
                if (invalidUsageException != null) {
                    rfidListeners.onFailure(invalidUsageException);

                } else if (operationFailureException != null) {
                    rfidListeners.onFailure(operationFailureException);
                } else
                    rfidListeners.onSuccess(tagData);
            }
        }.execute();

    }

    public void accessOperationsWrite(final String tagValue, String offsetText, String lengthText, String accessRWData, String accessRwPassword,
                                      String bankItem, final RfidListeners rfidListeners) {
        RFIDController.isAccessCriteriaRead = true;
        TagAccess tagAccess = new TagAccess();
        final TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
        try {
            writeAccessParams.setAccessPassword(Long.decode("0X" + accessRwPassword));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            rfidListeners.onFailure("Password field is empty!");
        }
        writeAccessParams.setMemoryBank(getAccessRWMemoryBank(bankItem));
        writeAccessParams.setOffset(Integer.parseInt(offsetText));
        if (RFIDController.asciiMode == true) {
            accessRWData = asciitohex.convert(accessRWData);
            writeAccessParams.setWriteData(accessRWData);
            writeAccessParams.setWriteDataLength(accessRWData.length() / 4);
        } else {
            writeAccessParams.setWriteData(accessRWData);
            writeAccessParams.setWriteDataLength(accessRWData.length() / 4);
        }
        new AsyncTask<Void, Void, Boolean>() {
            private InvalidUsageException invalidUsageException;
            private OperationFailureException operationFailureException;
            private Boolean bResult = false;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    setAccessProfile(true);
                    RFIDController.mConnectedReader.Actions.TagAccess.writeWait(tagValue, writeAccessParams, null, null, true, false);
                    bResult = true;
                } catch (InvalidUsageException e) {
                    invalidUsageException = e;
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    operationFailureException = e;
                    e.printStackTrace();
                }
                return bResult;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result) {
                    if (invalidUsageException != null) {
                        rfidListeners.onFailure(invalidUsageException);

                    } else if (operationFailureException != null) {
                        rfidListeners.onFailure(operationFailureException);
                    }
                } else
                    rfidListeners.onSuccess(null);
            }
        }.execute();

    }

    public void accessOperationLock(final String tagId, String accessRwPassword, LOCK_DATA_FIELD lockDataField, LOCK_PRIVILEGE lockPrivilege, final RfidListeners rfidListeners) {
        RFIDController.accessControlTag = tagId;
        RFIDController.isAccessCriteriaRead = true;
        //Set the param values
        TagAccess tagAccess = new TagAccess();
        final TagAccess.LockAccessParams lockAccessParams = tagAccess.new LockAccessParams();
        if (lockDataField != null)
            lockAccessParams.setLockPrivilege(lockDataField, lockPrivilege);
        try {
            lockAccessParams.setAccessPassword(Long.decode("0X" + accessRwPassword));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            rfidListeners.onFailure("Password field is empty!");
        }
        new AsyncTask<Void, Void, Boolean>() {
            private InvalidUsageException invalidUsageException;
            private OperationFailureException operationFailureException;
            private Boolean bResult = false;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    setAccessProfile(true);
                    RFIDController.mConnectedReader.Actions.TagAccess.lockWait(tagId, lockAccessParams, null, true);
                    bResult = true;
                } catch (InvalidUsageException e) {
                    invalidUsageException = e;
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    operationFailureException = e;
                    e.printStackTrace();
                }
                return bResult;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result) {
                    if (invalidUsageException != null) {
                        rfidListeners.onFailure(invalidUsageException);
                    } else if (operationFailureException != null) {
                        rfidListeners.onFailure(operationFailureException);
                    }
                } else
                    rfidListeners.onSuccess(null);
            }
        }.execute();
    }

    public void accessOperationsKill(final String tagId, String accessRWpassword, final RfidListeners rfidListeners) {
        RFIDController.accessControlTag = tagId;
        RFIDController.isAccessCriteriaRead = true;
        //Set the param values
        TagAccess tagAccess = new TagAccess();
        final TagAccess.KillAccessParams killAccessParams = tagAccess.new KillAccessParams();
        try {
            killAccessParams.setKillPassword(Long.decode("0X" + accessRWpassword));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            rfidListeners.onFailure("Password field is empty!");
        }
        new AsyncTask<Void, Void, Boolean>() {
            private InvalidUsageException invalidUsageException;
            private OperationFailureException operationFailureException;
            private Boolean bResult = false;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    setAccessProfile(true);
                    RFIDController.mConnectedReader.Actions.TagAccess.killWait(tagId, killAccessParams, null, true);
                    bResult = true;
                } catch (InvalidUsageException e) {
                    invalidUsageException = e;
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    operationFailureException = e;
                    e.printStackTrace();
                }
                return bResult;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result) {
                    if (invalidUsageException != null) {
                        rfidListeners.onFailure(invalidUsageException);

                    } else if (operationFailureException != null) {
                        rfidListeners.onFailure(operationFailureException);
                    }
                } else
                    rfidListeners.onSuccess(null);
            }
        }.execute();

    }

    public void setAccessProfile(boolean bSet) {
        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected() && RFIDController.mConnectedReader.isCapabilitiesReceived()
                && !RFIDController.mIsInventoryRunning && !RFIDController.isLocatingTag) {
            Antennas.AntennaRfConfig antennaRfConfigLocal;
            try {
                if (bSet && RFIDController.antennaRfConfig.getrfModeTableIndex() != 0) {
                    antennaRfConfigLocal = RFIDController.antennaRfConfig;
                    // use of default profile for access operation
                    antennaRfConfigLocal.setrfModeTableIndex(0);
                    RFIDController.mConnectedReader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfigLocal);
                    RFIDController.antennaRfConfig = antennaRfConfigLocal;
                } else if (!bSet && RFIDController.antennaRfConfig.getrfModeTableIndex() != LinkProfileUtil.getInstance().getSimpleProfileModeIndex(RFIDController.ActiveProfile.LinkProfileIndex)) {
                    antennaRfConfigLocal = RFIDController.antennaRfConfig;
                    antennaRfConfigLocal.setrfModeTableIndex(LinkProfileUtil.getInstance().getSimpleProfileModeIndex(RFIDController.ActiveProfile.LinkProfileIndex));
                    RFIDController.mConnectedReader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfigLocal);
                    RFIDController.antennaRfConfig = antennaRfConfigLocal;
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }


}
