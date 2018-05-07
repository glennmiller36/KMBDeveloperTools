package com.jjkeller.kmb.developertools.enumerator;

/**
 * Enums of possible data column types
 */

public enum DataTypeEnum {
	DATA_TYPE_NULL,			// Sqlite storage class
	DATA_TYPE_BOOLEAN,
	DATA_TYPE_DATE,
	DATA_TYPE_DATETIME,
	DATA_TYPE_INTEGER,		// Sqlite storage class
	DATA_TYPE_DOUBLE,		// Sqlite storage class (really FLOAT but truncates decimals)
	DATA_TYPE_GUID,
	DATA_TYPE_STRING,		// Sqlite storage class
	DATA_TYPE_BLOB			// Sqlite storage class
}