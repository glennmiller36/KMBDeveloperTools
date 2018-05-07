package com.jjkeller.kmb.developertools.manager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.database.DataColumn;
import com.jjkeller.kmb.developertools.database.DataRow;
import com.jjkeller.kmb.developertools.database.DataSet;
import com.jjkeller.kmb.developertools.enumerator.DataTypeEnum;
import com.jjkeller.kmb.developertools.model.DatabaseBackupModel;
import com.jjkeller.kmb.developertools.model.DatabaseModel;

import com.github.mnadeem.TableNameParser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class provides access to the database services.
 */

public class DatabaseManager implements IDatabaseManager {
    private static final String DATABASE_PATH_KMB = "/data/data/com.jjkeller.kmb/databases/";
    private static final String DATABASE_PATH_ALK = "/data/data/com.jjkeller.kmb.alk/databases/";
    public static final String DATABASE_NAME = "kmb";

	private static final String SQL_PRAGMA_TABLEINFO = "PRAGMA table_info(%s)";
	private static final String SQL_AUTOINCREMENT_PREFIX = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '";
	private static final String SQL_AUTOINCREMENT_SUFFIX = "' AND sql LIKE '%AUTOINCREMENT%'";

	private String mDatabasePath = "";


    /**
     * Get Metadata about a database like Path and Version.
     */
    @Override
    public DatabaseModel getDatabaseModel(@NonNull String path) {
    	try {
			SQLiteDatabase database = getReadableDatabase();
			if (database != null) {
				DatabaseModel model = new DatabaseModel(database.getPath(), database.getVersion());
				database.close();
				return model;
			}
		}
		catch (SQLiteException ex) { /* no database */ }

		return new DatabaseModel();
	}

	/*----------------------------------------
	 * Database Backup & Restore
	 *----------------------------------------*/

	/**
     * Backup the active KMB database to External Storage
     */
    @Override
    public DatabaseBackupModel backupDatabase(String name) throws IOException {
    	String backupName = name;

    	// substitute [Now()] with the current date/time
    	if (backupName.contains(KmbApplication.getContext().getString(R.string.now_syntax))) {
    		Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			backupName = backupName.replace(KmbApplication.getContext().getString(R.string.now_syntax), formatter.format(date));
		}

		String path = Services.File().getDatabaseBackupFileDirectory();
		Services.File().copyFile(getKmbDatabasePath(), path + backupName);

		File backup = new File(path + backupName);

		return new DatabaseBackupModel(backupName, path, new Date(backup.lastModified()), backup.length());
    }

    /**
     * Restore database from External Storage to the active KMB database
     */
    public int restoreDatabase(@NonNull String sourcePath) throws IOException {
        return Services.File().copyFile(sourcePath, getKmbDatabasePath());
    }

    /**
     * Delete database from External Storage
     */
    public boolean deleteDatabase(@NonNull String sourcePath) {
    	boolean result = Services.File().deleteFile(sourcePath);

    	if (result) {
    		mDatabasePath = "";
		}

		return result;
    }

	/**
	 * Return a list of backup files from External Storage.
	 */
	@Override
	public List<DatabaseBackupModel> getBackupList() {

		List<DatabaseBackupModel> backupList = new ArrayList<>();

		File[] files = Services.File().getDirectoryFiles(Services.File().getDatabaseBackupFileDirectory());
		if (files.length == 0) {
			return backupList;
		}

		for (File file : files) {
			backupList.add(new DatabaseBackupModel(file.getName(), file.getPath(), new Date(file.lastModified()), file.length()));
		}

		// sort collection by BackupDate DESC
		Collections.sort(backupList, (o1, o2) -> {
			return o2.getLastModifiedDate().compareTo(o1.getLastModifiedDate());	// reverse sort order
		});

		return backupList;
	}


	/*----------------------------------------
	 * Execute SQL methods
	 *----------------------------------------*/

	/**
	 * Executes a single statement that returns data.
	 */
	public DataSet executeRawSql(@NonNull String sql) throws SQLiteException {

		String upperCaseSql = sql.trim().toUpperCase();

		if (upperCaseSql.startsWith("SELECT")) {
			return executeSelect(sql);
		}
		else if (upperCaseSql.startsWith("INSERT")) {
			return executeInsert(sql);
		}
		else if (upperCaseSql.startsWith("UPDATE")) {
			return executeUpdate(sql);
		}
		else if (upperCaseSql.startsWith("DELETE")) {
			return executeDelete(sql);
		}
		else if (upperCaseSql.startsWith("PRAGMA")) {
			return executeSelect(sql);
		}

		throw new SQLiteException(KmbApplication.getContext().getString(R.string.unsupported_sql_command));
	}

	/**
	 * Executes a SQL SELECT statement that returns data.
	 */
	private DataSet executeSelect(@NonNull String sql) throws SQLiteException {

		HashMap<String, DataColumn> tableInfoColumns = new HashMap<String, DataColumn>();

		TableNameParser parser = new TableNameParser(sql);
		if (parser.tables().size() == 1) {
			// execute PRAGMA table_info to get the real column data types - not just the sqlite storage class
			tableInfoColumns = executePragmaTableInfo(parser.tables().iterator().next());
		}

		DataSet dataSet = new DataSet();

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(sql.trim(), null);
		if (cursor != null) {
			try {

				// populate tables
				for (String tableName : parser.tables()) {
					dataSet.getTables().add(tableName);
				}

				// populate columns
				for (String columnName : cursor.getColumnNames()) {
					// if querying a single table, column should be in PRAGMA table_info columnd
					if (tableInfoColumns.containsKey(columnName)) {
						DataColumn column = tableInfoColumns.get(columnName);
						dataSet.getColumns().add(column);

						if (column.getIsPrimaryKey()) {
							dataSet.getPrimaryKeys().add(column);
						}
					} else {
						// querying multiple columns - use the sqlite storage class
						dataSet.getColumns().add(new DataColumn(columnName, DataTypeEnum.DATA_TYPE_STRING));
					}
				}

				while (cursor.moveToNext()) {

					DataRow row = new DataRow();

					for (DataColumn column : dataSet.getColumns()) {
						int columnIndex = cursor.getColumnIndex(column.getColumnName());

						if (cursor.getType(columnIndex) == Cursor.FIELD_TYPE_NULL) {
							row.set(column.getColumnName(), null);
						} else {
							switch (column.getDataType()) {

								case DATA_TYPE_NULL:        // Sqlite storage class
									row.set(column.getColumnName(), null);
									break;

								case DATA_TYPE_BOOLEAN:
									row.set(column.getColumnName(), "1".equals(cursor.getString(columnIndex)));
									break;

								case DATA_TYPE_DATE:
								case DATA_TYPE_DATETIME:
									String dateString = cursor.getString(columnIndex);

									if (!TextUtils.isEmpty(dateString)) {
										String pattern = determineDatePattern(dateString);
										SimpleDateFormat format = new SimpleDateFormat(pattern);

										column.setDatePattern(pattern);

										try {
											Date date = format.parse(dateString);
											row.set(column.getColumnName(), date);
										} catch (ParseException e) {
											e.printStackTrace();
										}
									}

									break;

								case DATA_TYPE_INTEGER:    // Sqlite storage class
									row.set(column.getColumnName(), cursor.getInt(columnIndex));
									break;

								case DATA_TYPE_DOUBLE:     // Sqlite storage class
									row.set(column.getColumnName(), cursor.getDouble(columnIndex));
									break;

								case DATA_TYPE_GUID:
									try {
										UUID guid = UUID.fromString(cursor.getString(columnIndex));
										row.set(column.getColumnName(), guid);
									} catch (IllegalArgumentException e) {
										e.printStackTrace();
									}
									break;

								case DATA_TYPE_BLOB:        // Sqlite storage class
									row.set(column.getColumnName(), cursor.getBlob(columnIndex));
									break;

								default:
									row.set(column.getColumnName(), cursor.getString(columnIndex));
									break;

							}
						}
					}

					dataSet.getRows().add(row);
				}
			} finally {
				cursor.close();
			}
		}

		return dataSet;
	}

	/**
	 * Executes a single INSERT statement in a transaction and returns the row ID of the last row inserted.
	 */
	private DataSet executeInsert(@NonNull String sql) throws SQLiteException {

		DataSet dataSet = new DataSet();

		DataColumn column = new DataColumn(KmbApplication.getContext().getString(R.string.inserted_row_id), DataTypeEnum.DATA_TYPE_INTEGER);
		dataSet.getColumns().add(column);

		SQLiteDatabase db = getWritableDatabase();

		try {
			SQLiteStatement statement = db.compileStatement(sql);
			long rowID = statement.executeInsert();

			DataRow row = new DataRow();
			row.set(KmbApplication.getContext().getString(R.string.inserted_row_id), String.valueOf(rowID));

			dataSet.getRows().add(row);
		} finally {
			db.close();
		}

		return dataSet;
	}

	/**
	 * Executes a single UPDATE statement in a transaction and returns the row ID of the last row inserted.
	 */
	private DataSet executeUpdate(@NonNull String sql) throws SQLiteException {

		DataSet dataSet = new DataSet();

		DataColumn column = new DataColumn(KmbApplication.getContext().getString(R.string.rows_updated), DataTypeEnum.DATA_TYPE_INTEGER);
		dataSet.getColumns().add(column);

		SQLiteDatabase db = getWritableDatabase();

		try {
			SQLiteStatement statement = db.compileStatement(sql);
			int rowsAffected = statement.executeUpdateDelete();

			DataRow row = new DataRow();
			row.set(KmbApplication.getContext().getString(R.string.rows_updated), String.valueOf(rowsAffected));

			dataSet.getRows().add(row);
		} finally {
			db.close();
		}

		return dataSet;
	}

	/**
	 * Executes a single DELETE statement in a transaction and returns the row ID of the last row inserted.
	 */
	private DataSet executeDelete(@NonNull String sql) throws SQLiteException {

		DataSet dataSet = new DataSet();

		DataColumn column = new DataColumn(KmbApplication.getContext().getString(R.string.rows_deleted), DataTypeEnum.DATA_TYPE_INTEGER);
		dataSet.getColumns().add(column);

		SQLiteDatabase db = getWritableDatabase();

		try {
			SQLiteStatement statement = db.compileStatement(sql);
			int rowsAffected = statement.executeUpdateDelete();

			DataRow row = new DataRow();
			row.set(KmbApplication.getContext().getString(R.string.rows_deleted), String.valueOf(rowsAffected));

			dataSet.getRows().add(row);
		} finally {
			db.close();
		}

		return dataSet;
	}

	/**
	 * Executes a PRAGRA statement to get column info about a table.
	 */
	private HashMap<String, DataColumn> executePragmaTableInfo(@NonNull String tableName) throws SQLiteException {

		HashMap<String, DataColumn> columns = new HashMap<String, DataColumn>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(String.format(SQL_PRAGMA_TABLEINFO, tableName.trim()), null);
		if (cursor != null) {
			try {
				while (cursor.moveToNext()) {
					String name = cursor.getString(cursor.getColumnIndex("name"));
					String type = cursor.getString(cursor.getColumnIndex("type"));
					Boolean allowDbNull = cursor.getString(cursor.getColumnIndex("notnull")).equals("0");    // "1" or "0" values
					Boolean isPrimarykKey = cursor.getString(cursor.getColumnIndex("pk")).equals("1");                // "1" or "0" values

					DataTypeEnum dataType = convertStringToDataTypeEnum(type);

					if (isPrimarykKey) {
						allowDbNull = false;
					}

					if (dataType == DataTypeEnum.DATA_TYPE_BOOLEAN) {
						allowDbNull = true;    // two-stage checkbox is either True of False - never Null
					}

					columns.put(name, new DataColumn(name, dataType, allowDbNull, isPrimarykKey));
				}
			} finally {
				cursor.close();
			}
		}

		Iterator<Map.Entry<String, DataColumn>> it = columns.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DataColumn> pair = it.next();

			DataColumn column = pair.getValue();
			if (column.getIsPrimaryKey() && column.getDataType() == DataTypeEnum.DATA_TYPE_INTEGER) {
				Boolean isAutoIncrement = isPrimaryIntegerKeyAutoIncrement(tableName);
				column.setIsAutoIncrement(isAutoIncrement);
				break;
			}
		}

		return columns;
	}

	/**
	 * Determine if a Primary Key, Integer data type column is AutoIncrement.
	 */
	private Boolean isPrimaryIntegerKeyAutoIncrement(String tableName) throws SQLiteException {

		try {
			SQLiteDatabase db = getReadableDatabase();

			String sql = SQL_AUTOINCREMENT_PREFIX + tableName.trim() + SQL_AUTOINCREMENT_SUFFIX;

			Cursor cursor = db.rawQuery(sql, null);
			if (cursor != null) {
				try {
					while (cursor.moveToNext()) {
						// If the count came out as non-zero, the table has an autoincrement primary key column.
						// If the count came out as zero, the table is either empty and has never contained data, or does not have an autoincrement primary key 				}
						return cursor.getInt(cursor.getColumnIndex("1")) != 0;
					}
				} finally {
					cursor.close();
				}
			}
		}
		catch (SQLiteException ex) { /* no database */ }

		return false;
	}


	/*----------------------------------------
	 * CRUD Operations
	 *----------------------------------------*/
	public Integer deleteRecord(String tableName, List<Integer> keysToDelete) {
		Integer rowsAffected = 0;

		try {
			SQLiteDatabase db = getWritableDatabase();

			try {
				String sql = String.format(KmbApplication.getContext().getString(R.string.sql_delete), tableName, android.text.TextUtils.join(",", keysToDelete));

				SQLiteStatement statement = db.compileStatement(sql);
				rowsAffected = statement.executeUpdateDelete();
			} finally {
				db.close();
			}
		}
		catch (SQLiteException ex) { /* no database */ }

		return rowsAffected;
	}


	/*----------------------------------------
	 * Helper methods
	 *----------------------------------------*/


	/**
	 * Determine the database path to use. Depending on the KMB build variant, it could be kmb or alk.
	 */
	public String getKmbDatabasePath() {
		if (mDatabasePath.isEmpty()) {
			if (Services.File().doesDatabaseExist(DATABASE_PATH_KMB + DATABASE_NAME)) {
				mDatabasePath = DATABASE_PATH_KMB + DATABASE_NAME;
			} else if (Services.File().doesDatabaseExist(DATABASE_PATH_ALK + DATABASE_NAME)) {
				mDatabasePath = DATABASE_PATH_ALK + DATABASE_NAME;
			}
		}

		return mDatabasePath;
	}

	public String getKmbDatabaseVersion() throws SQLiteException {
		if (!getKmbDatabasePath().isEmpty()) {
			DataSet dataSet = Services.Database().executeRawSql("PRAGMA user_version");
			if (!dataSet.getRows().isEmpty()) {
				return (String) dataSet.getRowAt(0).get("user_version");
			}
		}

		return null;
	}

	private SQLiteDatabase getReadableDatabase() throws SQLiteException {
		return SQLiteDatabase.openDatabase(getKmbDatabasePath(), null, SQLiteDatabase.OPEN_READONLY);
	}

	private SQLiteDatabase getWritableDatabase() throws SQLiteException {
		return SQLiteDatabase.openDatabase(getKmbDatabasePath(), null, SQLiteDatabase.OPEN_READWRITE);
	}

	/**
	 * Convert PRAGMA table_info type value to a supported DataType enum
	 */
	private DataTypeEnum convertStringToDataTypeEnum(String type) {

		switch (type.toUpperCase()) {
			case "BOOL":      			/* FIELD_TYPE_INTEGER */
			case "BOOLEAN":     		/* FIELD_TYPE_INTEGER */
				return DataTypeEnum.DATA_TYPE_BOOLEAN;

			case "BIGINT":     			/* FIELD_TYPE_INTEGER */
			case "INT":         		/* FIELD_TYPE_INTEGER */
			case "INT64":       		/* FIELD_TYPE_INTEGER */
			case "INTEGER":    			/* FIELD_TYPE_INTEGER */
			case "LARGEINT":    		/* FIELD_TYPE_INTEGER */
			case "SMALLINT":    		/* FIELD_TYPE_INTEGER */
			case "TINYINT":     		/* FIELD_TYPE_INTEGER */
			case "WORD":        		/* FIELD_TYPE_INTEGER */
				return DataTypeEnum.DATA_TYPE_INTEGER;

			case "CURRENCY":    		/* FIELD_TYPE_FLOAT */
			case "DEC":         		/* FIELD_TYPE_FLOAT */
			case "DECIMAL":     		/* FIELD_TYPE_FLOAT */
			case "DOUBLE":      		/* FIELD_TYPE_FLOAT */
			case "DOUBLE PRECISION": 	/* FIELD_TYPE_FLOAT */
			case "FLOAT":     			/* FIELD_TYPE_FLOAT */
			case "MONEY":       		/* FIELD_TYPE_FLOAT */
			case "NUMBER":      		/* FIELD_TYPE_FLOAT */
			case "NUMERIC":      		/* FIELD_TYPE_FLOAT */
			case "REAL":        		/* FIELD_TYPE_FLOAT */
			case "SMALLMONEY":  		/* FIELD_TYPE_FLOAT */
				return DataTypeEnum.DATA_TYPE_DOUBLE;

			case "BINARY":      		/* FIELD_TYPE_BLOB */
			case "BLOB":        		/* FIELD_TYPE_BLOB */
			case "BLOB_TEXT":    		/* FIELD_TYPE_BLOB */
			case "CLOB":        		/* FIELD_TYPE_BLOB */
			case "GRAPHIC":     		/* FIELD_TYPE_BLOB */
			case "IMAGE":       		/* FIELD_TYPE_BLOB */
			case "PHOTO":       		/* FIELD_TYPE_BLOB */
			case "PICTURE":     		/* FIELD_TYPE_BLOB */
			case "RAW":         		/* FIELD_TYPE_BLOB */
			case "VARBINARY":   		/* FIELD_TYPE_BLOB */
				return DataTypeEnum.DATA_TYPE_BLOB;

			case "DATE":        		/* FIELD_TYPE_STRING */
			case "DATETEXT":    		/* FIELD_TYPE_STRING */
				return DataTypeEnum.DATA_TYPE_DATE;

			case "DATETIME":    		/* FIELD_TYPE_STRING */
				return DataTypeEnum.DATA_TYPE_DATETIME;

			case "GUID":        		/* FIELD_TYPE_BLOB */
			case "UNIQUEIDENTIFIER":    /* FIELD_TYPE_BLOB */
				return DataTypeEnum.DATA_TYPE_GUID;

			default:
				// "CHAR":        		/* FIELD_TYPE_STRING */
				// "MEMO":        		/* FIELD_TYPE_STRING */
				// "NCHAR":       		/* FIELD_TYPE_STRING */
				// "NTEXT":       		/* FIELD_TYPE_STRING */
				// "NVARCHAR":    		/* FIELD_TYPE_STRING */
				// "NVARCHAR2":   		/* FIELD_TYPE_STRING */
				// "TEXT":        		/* FIELD_TYPE_STRING */
				// "TIME":        		/* FIELD_TYPE_STRING */
				// "TIMESTAMP":   		/* FIELD_TYPE_STRING */
				// "VARCHAR":     		/* FIELD_TYPE_STRING */
				// "VARCHAR2":    		/* FIELD_TYPE_STRING */
				return DataTypeEnum.DATA_TYPE_STRING;
		}
	}

	private String determineDatePattern(String dateString) {
		String pattern = dateString.replaceAll("[0-9]","");	// remove all numeric values from string

		if (pattern.equals("-- ::"))
			return "yyyy-MM-dd HH:mm:ss";
		else if (pattern.equals("--"))
			return "yyyy-MM-dd";
		else if (pattern.equals("// ::"))
			return "dd/MM/yyyy HH:mm:ss";
		else if (pattern.equals("//"))
			return "dd/MM/yyyy";
		else
			return "yyyy-MM-dd HH:mm:ss";
	}
}
