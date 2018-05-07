package com.jjkeller.kmb.developertools.model;

import android.support.annotation.NonNull;

/**
 * TerminalCommandModel class containing data about a Bluetooth Command.
 */

public class TerminalCommandModel {

    @NonNull
    private int mId;

    private boolean mIsFavorite;

    @NonNull
    private String mCommand;

    @NonNull
    private String mCommandDescription;


    /**
     * Constructors
     */

    public TerminalCommandModel(@NonNull final int id, @NonNull final String command, @NonNull final String description) {
        mId = id;
        mCommand = command;
        mCommandDescription = description;
    }


    /**
     * Properties
     */

    public int getId() {
        return mId;
    }

    public boolean getIsFavorite() {
        return mIsFavorite;
    }

    public void setIsFavorite(boolean isFavorite) { mIsFavorite = isFavorite; }

    @NonNull
    public String getCommand() {
        return mCommand;
    }

    @NonNull
    public String getCommandDescription() {
        return mCommandDescription;
    }


    /**
     * Methods
     */

    @Override
    public String toString() {
        return mCommand;
    }
}
