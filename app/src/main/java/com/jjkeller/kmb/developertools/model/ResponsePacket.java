package com.jjkeller.kmb.developertools.model;

/**
 * ResponsePacket class containing metadata about bytes returned from Bluetooth device.
 */

public class ResponsePacket {

    private String mData = "";
    private int mNextRecordId = 0;

    public String getData() {
        return mData;
    }

    public void setData(String data) { mData = data; }

    public int getNextRecordId() {
        return mNextRecordId;
    }

    public void setNextRecordId(int recordId) { mNextRecordId = recordId; }
}
