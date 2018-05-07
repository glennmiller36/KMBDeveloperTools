package com.jjkeller.kmb.developertools.manager;

import java.io.File;
import java.io.IOException;

/**
 * This class provides access to the file system.
 */

public interface IFileManager {
    boolean doesDatabaseExist(String databasePath);

    boolean canReadFile(String path);
    int copyFile(String sourcePath, String destinationPath) throws IOException;
    String getStringFromFile (String sourcePath) throws IOException;
    void saveStringToFile (String sourcePath, String data) throws IOException;
    boolean deleteFile(String sourcePath);
    void deleteDirectoryFiles(String sourcePath);

    File[] getDirectoryFiles(String sourcePath);
    String getExternalStorageDirectory();
    String getDatabaseBackupFileDirectory();
    String getSqlFileDirectory();
    String getTempFileDirectory();

    String getErrorLogPath();
}
