package com.jjkeller.kmb.developertools.manager;

import android.content.Context;
import android.os.Build;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter.DATE_PATTERN;

/**
 * This class provides info about the running Device.
 */

public class DeviceManager implements IDeviceManager {

    public String getDeviceInfo() {
        Context context = KmbApplication.getContext();
        StringBuilder sb = new StringBuilder();

        String newLine = System.getProperty("line.separator");

        sb.append(context.getString(R.string.pref_title_devicemanufacturer) + " = " + android.os.Build.MANUFACTURER);
        sb.append(newLine);
        sb.append(context.getString(R.string.pref_title_devicemodel) + " = " + Build.MODEL);
        sb.append(newLine);
        sb.append(context.getString(R.string.pref_title_devicebrand) + " = " + Build.BRAND);
        sb.append(newLine);
        sb.append(context.getString(R.string.pref_title_deviceandroidversion) + " = " + getDeviceAndroidVersion());
        sb.append(newLine);

        String version = Services.Database().getKmbDatabaseVersion();

        Calendar c = Calendar.getInstance();
        c.set(2000, 0, 1);
        c.add(Calendar.DATE, Integer.parseInt(version));

        SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
        sb.append(context.getString(R.string.database_version) + " = " + version + " (" + df.format(c.getTime()) + ")");
        sb.append(newLine);

        return sb.toString();
    }

    /**
     * Get the device Android version and API level
     */
    private String getDeviceAndroidVersion() {
        int sdk = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;

        return String.format(KmbApplication.getContext().getString(R.string.androidversionformat), versionRelease, String.valueOf(sdk));
    }
}
