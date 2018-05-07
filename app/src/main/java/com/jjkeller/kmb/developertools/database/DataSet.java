package com.jjkeller.kmb.developertools.database;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an im-memory cache of data.
 */

public class DataSet {

	@NonNull
	private List<String> mTables = new ArrayList<>();

	@NonNull
	private List<DataRow> mRows = new ArrayList<>();

	@NonNull
	private List<DataColumn> mColumns = new ArrayList<>();

	@NonNull
	private List<DataColumn> mPrimaryKeys = new ArrayList<>();


	/**
	 * Constructors
	 */


	/**
	 * Properties
	 */

	public List<String> getTables() { return mTables; }

	public List<DataRow> getRows() { return mRows; }

	public DataRow getRowAt(int index) { return mRows.get(index); }

	public List<DataColumn> getColumns() { return mColumns; }

	public List<DataColumn> getPrimaryKeys() { return mPrimaryKeys; }


	/**
	 * Methods
	 */

	public String generateSelectedRowsFilterStatement() {
		List<String> stringList = new ArrayList<>();

		for (DataColumn column : mColumns) {
			stringList.add(column.getColumnName());
		}

		String commaSeparatedColumns = android.text.TextUtils.join(",", stringList);
		String whereClause = buildSelectedWhereClause();
		return String.format(KmbApplication.getContext().getString(R.string.select_statement), commaSeparatedColumns, mTables.get(0), whereClause);
	}

	/**
	 * Create WHERE clause based on primary keys of the selected rows
	 */
	public String buildSelectedWhereClause() {
		List<String> selectedRows = new ArrayList<>();

		for (DataRow row : mRows) {
			if (row.getIsRowSelected()) {
				for (DataColumn keyColumn : mPrimaryKeys) {
					Object obj = row.get(keyColumn.getColumnName());

					if (obj instanceof Boolean) {
						selectedRows.add(keyColumn.getColumnName() + " = " + ((boolean)obj ? "1" : "0"));
					}
					else if (obj instanceof Integer) {
						selectedRows.add(keyColumn.getColumnName() + " = " + ((Integer) obj).intValue());
					}
					else if (obj instanceof Double) {
						selectedRows.add(keyColumn.getColumnName() + " = " + ((Integer) obj).doubleValue());
					}
					else {
						selectedRows.add(keyColumn.getColumnName() + " = '" + obj.toString() + "'");
					}
				}
			}
		}

		return TextUtils.join(" AND ", selectedRows);
	}
}
