package com.jjkeller.kmb.developertools.viewmodel;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jjkeller.kmb.developertools.manager.Services;

import io.reactivex.Observable;

/**
 * View model for reading and displaying kmberrlog.txt file
 */

public class ErrorLogViewModel {

    /**
     * Constructors
     */


    /**
     * Properties
     */


    /**
     * Methods
     */

    @NonNull
    public Observable<String>  readErrorLog() {
        return Observable.create(e -> {
            String result = "";
            String errorLogPath = Services.File().getErrorLogPath();
            if (!TextUtils.isEmpty(errorLogPath)) {
                result = Services.File().getStringFromFile(errorLogPath);
            }
            e.onNext(result);
            e.onComplete();
        });
    }
}