package com.jjkeller.kmb.developertools;

import android.app.Application;
import android.content.Context;

/**
 * Extend application to allow access to ApplicationContext anywhere in the app
 */

public class KmbApplication extends Application {
    private static KmbApplication instance;

    public static Context getContext(){
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}