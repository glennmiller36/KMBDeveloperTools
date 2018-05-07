package com.jjkeller.kmb.developertools.viewmodel;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.enumerator.TerminalCommandTypeEnum;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.model.BluetoothTerminalModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static com.jjkeller.kmb.developertools.manager.DatabaseManager.DATABASE_NAME;

/**
 * View model to Email terminal results.
 */

public class BluetoothTerminalViewModel {

    public static final String FILE_TERMINALLOG = "terminallog.txt";
    private final String SYSTEM_NEWLINE = System.getProperty("line.separator");
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
    public Observable<ArrayList<Uri>>  copyTerminalFileToTempDirectory(List<BluetoothTerminalModel> list) {
        return Observable.create(e -> {
            ArrayList<Uri> uris = new ArrayList<Uri>();
            StringBuilder sb = new StringBuilder();

            String newline = System.getProperty("line.separator");

            // convert list of transmitted and received data into a nicely formatted string
            for (BluetoothTerminalModel model : list) {
                if (model.getType() == TerminalCommandTypeEnum.COMMAND) {
                    sb.append(model.getDateTimeFormatted() + "  " + model.getCommand() + newline);
                    sb.append(formatDataWithLeadingSpaces(model.getData(), model.getDateTimeFormatted().length() + 2) + newline);
                }
                else {
                    sb.append(model.getDateTimeFormatted() + "  " + model.getData() + newline + newline);
                }
            }

            String tempPath = Services.File().getTempFileDirectory();
            if (TextUtils.isEmpty(tempPath)) {
                throw new Exception(KmbApplication.getContext().getString(R.string.temp_directory_does_no_exist));
            }

            try {
                File file = new File(tempPath + FILE_TERMINALLOG);
                FileOutputStream fileOutput = new FileOutputStream(file);
                OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutput);
                outputStreamWriter.write(sb.toString());
                outputStreamWriter.flush();
                fileOutput.getFD().sync();
                outputStreamWriter.close();

                uris.add(FileProvider.getUriForFile(KmbApplication.getContext(), "com.jjkeller.kmb.developertools.files", new File(tempPath, FILE_TERMINALLOG)));
            }
            catch (IOException ex) {
                Log.e("Exception", "File write failed: " + ex.toString());
            }
            e.onNext(uris);
            e.onComplete();
        });
    }


    /*----------------------------------------
	 * Helper methods
	 *----------------------------------------*/

    /**
     * Format lines of data into formatted string by prefixing with leading spaces so everything lines up so it can be easily read.
     */
    private String formatDataWithLeadingSpaces(String data, int numberLeadingSpaces) {
        String leadingSpaces = "";

        for (int i = 0; i < numberLeadingSpaces; i++) {
            leadingSpaces += " ";
        }

        String lines[] = data.split(SYSTEM_NEWLINE);

        if (lines.length == 1) {
            return leadingSpaces + data + SYSTEM_NEWLINE;
        }

        String results = "";

        for (String line : lines) {
            results += leadingSpaces + line + SYSTEM_NEWLINE;
        }

        return results;
    }
}
