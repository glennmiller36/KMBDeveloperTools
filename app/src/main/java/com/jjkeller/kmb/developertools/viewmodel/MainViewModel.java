package com.jjkeller.kmb.developertools.viewmodel;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jjkeller.kmb.developertools.manager.Services;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.reactivex.Observable;

import static com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter.DATE_PATTERN;

/**
 * View model for the Main activity
 */

public class MainViewModel {

    @NonNull
    public Observable<String>  getDatabaseVersion() {
        return Observable.just(getKmbDatabaseVersion());
    }

    private String getKmbDatabaseVersion() {
        String result = "";

        try {
            String version = Services.Database().getKmbDatabaseVersion();
            if (!TextUtils.isEmpty(version)) {
                Calendar c = Calendar.getInstance();
                c.set(2000, 0, 1);
                c.add(Calendar.DATE, Integer.parseInt(version));

                SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
                result = version + " (" + df.format(c.getTime()) + ")";
            }
        }
        catch(Exception ex) {
            // database not compatible with Dev Tools (i.e. no sharedUserID)
            return "";
        }

        return result;
    }
}
