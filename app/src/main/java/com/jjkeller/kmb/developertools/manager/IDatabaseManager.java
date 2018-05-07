package com.jjkeller.kmb.developertools.manager;

import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import com.jjkeller.kmb.developertools.database.DataSet;
import com.jjkeller.kmb.developertools.model.DatabaseBackupModel;
import com.jjkeller.kmb.developertools.model.DatabaseModel;

import java.io.IOException;
import java.util.List;

/**
 * This class provides access to the database services.
 */

public interface IDatabaseManager {
    DatabaseModel getDatabaseModel(@NonNull String path);

    /*----------------------------------------
	 * Database Backup & Restore
	 *----------------------------------------*/
    DatabaseBackupModel backupDatabase(String name) throws IOException;
    int restoreDatabase(@NonNull String sourcePath) throws IOException;
    boolean deleteDatabase(@NonNull String path);
    List<DatabaseBackupModel> getBackupList();

    /*----------------------------------------
	 * Execute SQL
	 *----------------------------------------*/
    DataSet executeRawSql(@NonNull String sql) throws SQLiteException;

    /*----------------------------------------
	 * CRUD Operations
	 *----------------------------------------*/
    Integer deleteRecord(String tableName, List<Integer> keysToDelete);

    String getKmbDatabasePath();
    String getKmbDatabaseVersion();
}
