package com.jjkeller.kmb.developertools.database;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Represents a row of data in a DataSet.
 */

public class DataRow {

	@NonNull
	private HashMap<String, Object> mItems = new HashMap<String, Object>();

	private boolean mIsRowSelected;

	/**
	 * Constructors
	 */

	public DataRow() { }


	/**
	 * Properties
	 */

	/**
	 * Gets the data stored in the column specified by name.
	 */
	public Object get(String key) {
		if (TextUtils.isEmpty(key) || mItems.isEmpty()) {
			return null;
		}

		return mItems.get(key);
	}

	/**
	 * Sets the data stored in the column specified by name.
	 */
	public Object set(String key, Object value) {
		if (TextUtils.isEmpty(key)) {
			return null;
		}

		return mItems.put(key, value);
	}

	public boolean getIsRowSelected() { return mIsRowSelected; }
	public void setIsRowSelected(boolean isRowSelected) { mIsRowSelected = isRowSelected; }


	/**
	 * Methods
	 */


	/**
	 * Parcelable
	 */
	public static DataRow fromJson(String s) {
		return new Gson().fromJson(s, DataRow.class);
	}
	public String toJson() {
		return new Gson().toJson(this);
	}
}
