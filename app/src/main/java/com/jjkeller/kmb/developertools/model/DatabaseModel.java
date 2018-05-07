package com.jjkeller.kmb.developertools.model;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

/**
 * DatabaseModel class containing metadata about the database.
 */

public class DatabaseModel {

    @NonNull
    private final String mPath;

    private final int mVersion;

    public DatabaseModel() {
        mPath = "";
        mVersion = 1;
    }

    public DatabaseModel(@NonNull final String path, final int version) {
        mPath = path;
        mVersion = version;
    }

    @NonNull
    public String getPath() {
        return mPath;
    }

    public int getVersion() {
        return mVersion;
    }

    /**
     * The build version value is the number of days since 1/1/2000
     */
    public Date getVersionDate() {
        Calendar c = Calendar.getInstance();
        c.set(2000, 0, 1);  // Month value is 0-based

        // manipulate date
        c.add(Calendar.DATE, mVersion);

        // convert calendar to date
        return c.getTime();
    }

    public boolean isValid() {
        return !mPath.isEmpty();
    }
}
