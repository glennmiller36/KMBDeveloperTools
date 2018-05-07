package com.jjkeller.kmb.developertools.database;

import com.google.gson.Gson;
import com.jjkeller.kmb.developertools.enumerator.DataTypeEnum;

/**
 * Represents a column in a DataSet
 */

public class DataColumn {

	private DataTypeEnum mDataType;
	private String mColumnName;
	private Boolean mAllowDbNull = false;
	private Boolean mPrimaryKey = false;
	private String mDatePattern;
	private Boolean mIsAutoIncrement = false;

	/**
	 * Constructors
	 */

	public DataColumn(String columnName, DataTypeEnum dataType) {
		this.mColumnName = columnName;
		this.mDataType = dataType;
	}

	public DataColumn(String columnName, DataTypeEnum dataType, Boolean allowDbNull, Boolean primaryKey) {
		this.mColumnName = columnName;
		this.mDataType = dataType;
		this.mAllowDbNull = allowDbNull;
		this.mPrimaryKey = primaryKey;
	}

	/**
	 * Properties
	 */

	public String getColumnName() { return mColumnName; }
	public DataTypeEnum getDataType() { return mDataType; }
	public Boolean getAllowDbNull() { return mAllowDbNull; }
	public Boolean getIsPrimaryKey() { return mPrimaryKey; }

	public String getDatePattern() { return mDatePattern; }
	public void setDatePattern(String pattern) { mDatePattern = pattern; }

	public Boolean getIsAutoIncrement() { return mIsAutoIncrement; }
	public void setIsAutoIncrement(Boolean isAutoIncrement) { mIsAutoIncrement = isAutoIncrement; }

	/**
	 * Methods
	 */


	/**
	 * Parcelable
	 */

	public static DataColumn fromJson(String s) {
		return new Gson().fromJson(s, DataColumn.class);
	}
	public String toJson() {
		return new Gson().toJson(this);
	}
}
