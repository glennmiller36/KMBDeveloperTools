package com.jjkeller.kmb.developertools.model;

import android.support.annotation.NonNull;

import com.jjkeller.kmb.developertools.enumerator.TerminalCommandTypeEnum;

import java.text.DateFormat;
import java.util.Date;

/**
 * BluetoothTerminalModel class containing data about Terminal Commands.
 */

public class BluetoothTerminalModel {

    private TerminalCommandTypeEnum mType;

    @NonNull
    private final String mDateTimeString;

    @NonNull
    private String mCommand;

    @NonNull
    private final String mData;

    private String mErrorMessage;

    public BluetoothTerminalModel(TerminalCommandTypeEnum type, @NonNull String command, @NonNull final String data) {
        mType = type;
        mDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        mCommand = command;
        mData = data;
        mErrorMessage = "";
    }

    public BluetoothTerminalModel(TerminalCommandTypeEnum type, @NonNull String command, @NonNull final String data, final String errorMessage) {
        mType = type;
        mDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        mCommand = command;
        mData = data;
        mErrorMessage = errorMessage;
    }

    public TerminalCommandTypeEnum getType() { return mType; }

    @NonNull
    public String getCommand() {
        return mCommand;
    }

    @NonNull
    public String getDateTimeFormatted() {
        return mDateTimeString;
    }

    @NonNull
    public String getData() {
        return mData;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }
}
