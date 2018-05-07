package com.jjkeller.kmb.developertools.viewmodel;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.manager.Services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;

import static com.jjkeller.kmb.developertools.manager.DatabaseManager.DATABASE_NAME;
import static com.jjkeller.kmb.developertools.manager.FileManager.FILE_KMBERRLOG;

/**
 * View model to Email or Delete the KMB Database and/or kmberrlog.txt file
 */

public class DiagnosticsViewModel {

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
    public Observable<Boolean>  deleteDiagnosticFiles(boolean deleteDatabase, boolean deleteErrorLog) {
        return Observable.create(e -> {
            if (deleteDatabase) {
                String kmbDatabasePath = Services.Database().getKmbDatabasePath();
                if (TextUtils.isEmpty(kmbDatabasePath)) {
                    throw new Exception(String.format(KmbApplication.getContext().getString(R.string.file_does_no_exist), KmbApplication.getContext().getString(R.string.kmb_database)));
                }

                boolean deleted = Services.Database().deleteDatabase(kmbDatabasePath);
                if (!deleted) {
                   throw new Exception(String.format(KmbApplication.getContext().getString(R.string.file_not_deleted), kmbDatabasePath));
                }
            }

            if (deleteErrorLog) {
                String errorLogPath = Services.File().getErrorLogPath();
                if (TextUtils.isEmpty(errorLogPath)) {
                    throw new Exception(String.format(KmbApplication.getContext().getString(R.string.file_does_no_exist), KmbApplication.getContext().getString(R.string.kmb_error_log)));
                }

                boolean deleted = Services.File().deleteFile(errorLogPath);
                if (!deleted) {
                    throw new Exception(String.format(KmbApplication.getContext().getString(R.string.file_not_deleted), errorLogPath));
                }
            }

            e.onNext(true);
            e.onComplete();
        });
    }

    /**
     *  You must copy the file to the external directory (aka SD Card). It's because the email application cannot access your data directory
     *  (in the same way that you can't access other app's data directory)
     */
    @NonNull
    public Observable<ArrayList<Uri>>  copyDiagnosticFilesToTempDirectory(boolean copyDatabase, boolean copyErrorLog) {
        return Observable.create(e -> {
            ArrayList<Uri> uris = new ArrayList<Uri>();

            String tempPath = Services.File().getTempFileDirectory();
            if (TextUtils.isEmpty(tempPath)) {
                throw new Exception(KmbApplication.getContext().getString(R.string.temp_directory_does_no_exist));
            }

            // clear temp directory
            Services.File().deleteDirectoryFiles(tempPath);

            if (copyDatabase) {
                String kmbDatabasePath = Services.Database().getKmbDatabasePath();
                if (TextUtils.isEmpty(kmbDatabasePath)) {
                    throw new Exception(String.format(KmbApplication.getContext().getString(R.string.file_does_no_exist), KmbApplication.getContext().getString(R.string.kmb_database)));
                }

                try {
                    Services.File().copyFile(kmbDatabasePath, tempPath + DATABASE_NAME);
                    uris.add(FileProvider.getUriForFile(KmbApplication.getContext(), "com.jjkeller.kmb.developertools.files", new File(tempPath, DATABASE_NAME)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }


            if (copyErrorLog) {
                String errorLogPath = Services.File().getErrorLogPath();
                if (TextUtils.isEmpty(errorLogPath)) {
                    throw new Exception(String.format(KmbApplication.getContext().getString(R.string.file_does_no_exist), KmbApplication.getContext().getString(R.string.kmb_error_log)));
                }

                try {
                    Services.File().copyFile(errorLogPath, tempPath + FILE_KMBERRLOG);
                    uris.add(FileProvider.getUriForFile(KmbApplication.getContext(), "com.jjkeller.kmb.developertools.files", new File(tempPath, FILE_KMBERRLOG)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw ex;
                }

            }

            e.onNext(uris);
            e.onComplete();
        });
    }
}
