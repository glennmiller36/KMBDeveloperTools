package com.jjkeller.kmb.developertools.viewmodel;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.database.DataColumn;
import com.jjkeller.kmb.developertools.enumerator.DataTypeEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter.DATETIME_PATTERN;
import static com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter.DATE_PATTERN;

/**
 * Check the validity and correctness of requested DataColumn Updates.
 */

public class DataColumnValidation {
	private DataColumn mDataColumn;
	private Object mOriginalValue;
	private Object mUpdatedValue = null;
	private View mUiControl1;
	private View mUiControl2;

	/**
	 * Constructors
	 */

	public DataColumnValidation(DataColumn dataColumn, Object originalValue, View uiControl) {
		this.mDataColumn = dataColumn;
		this.mOriginalValue = originalValue;
		this.mUiControl1 = uiControl;
		this.mUiControl2 = null;
	}

	public DataColumnValidation(DataColumn dataColumn, Object originalValue, View uiControl1, View uiControl2) {
		this.mDataColumn = dataColumn;
		this.mOriginalValue = originalValue;
		this.mUiControl1 = uiControl1;
		this.mUiControl2 = uiControl2;
	}

	/**
	 * Properties
	 */


	/**
	 * Methods
	 */

	public boolean validate() {
		if (mDataColumn.getIsAutoIncrement()) {
			return true; // don't validate AutoIncrement columns -- UI control is readonly TextView
		}

		switch (mDataColumn.getDataType()) {
			case DATA_TYPE_NULL:
				return true;

			case DATA_TYPE_BOOLEAN:
				return validateBoolean();

			case DATA_TYPE_DATE:
				return validateDate();

			case DATA_TYPE_DATETIME:
				return validateDateTime();

			case DATA_TYPE_INTEGER:
				return validateInteger();

			case DATA_TYPE_DOUBLE:
				return validateDouble();

			case DATA_TYPE_BLOB:
				return true;	// don't validate Blob columns -- UI control is readonly TextView

			case DATA_TYPE_GUID:
				return validateGuid();

			case DATA_TYPE_STRING:
			default:
				return validateString();
		}
	}

	private boolean validateBoolean() {
		CheckBox checkBox = (CheckBox) mUiControl1;

		if (checkBox != null) {
			mUpdatedValue = checkBox.isChecked();
		}

		return true;
	}

	private boolean validateDate() {
		Button dateButton = (Button) mUiControl1;

		if (dateButton != null) {

			String dateText = dateButton.getText().toString();

			if (!mDataColumn.getAllowDbNull()) {
				dateButton.setError(TextUtils.isEmpty(dateText) ? KmbApplication.getContext().getText(R.string.required) : null);

				if (dateButton.getError() != null) {
					return false; // Required data missing
				}
			}

			if (!TextUtils.isEmpty(dateText)) {
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
				try {
					mUpdatedValue = sdf.parse(dateText);
				} catch (ParseException ex) {
					dateButton.setError(KmbApplication.getContext().getText(R.string.invalid));
					return false;    // Invalid value
				}
			}
		}

		return true;
	}

	private boolean validateDateTime() {
		Button dateButton = (Button) mUiControl1;
		Button timeButton = (Button) mUiControl2;

		if (dateButton != null && timeButton != null) {

			String dateText = dateButton.getText().toString();
			String timeText = timeButton.getText().toString();

			boolean isDateEmpty = TextUtils.isEmpty(dateText);
			boolean isTimeEmpty = TextUtils.isEmpty(timeText);

			if (!mDataColumn.getAllowDbNull()) {
				dateButton.setError(isDateEmpty ? KmbApplication.getContext().getText(R.string.required) : null);
				timeButton.setError(isTimeEmpty ? KmbApplication.getContext().getText(R.string.required) : null);

				if (dateButton.getError() != null || timeButton.getError() != null) {
					return false; // Required data missing
				}
			}

			if (!isDateEmpty && !isTimeEmpty) {
				SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_PATTERN);
				try {
					mUpdatedValue = sdf.parse(dateText + " " + timeText);
				} catch (ParseException ex) {
					dateButton.setError(KmbApplication.getContext().getText(R.string.invalid));
					timeButton.setError(KmbApplication.getContext().getText(R.string.invalid));
					return false;    // Invalid value
				}
			}
			else if (!isDateEmpty || !isTimeEmpty) {
				dateButton.setError(KmbApplication.getContext().getText(R.string.invalid));
				timeButton.setError(KmbApplication.getContext().getText(R.string.invalid));
				return false;    // Invalid value - need BOTH parts
			}
		}

		return true;
	}

	private boolean validateInteger() {
		EditText editText = (EditText) mUiControl1;

		if (editText != null) {
			String text = editText.getText().toString();

			if (!mDataColumn.getAllowDbNull()) {
				editText.setError(TextUtils.isEmpty(text) ? KmbApplication.getContext().getText(R.string.required) : null);

				if (editText.getError() != null) {
					return false; // Required data missing
				}
			}

			if (!TextUtils.isEmpty(text)) {
				try {
					mUpdatedValue = Integer.parseInt(text);
				} catch (NumberFormatException ex) {
					editText.setError(KmbApplication.getContext().getText(R.string.invalid));
					return false;	// Invalid value
				}
			}
		}

		return true;
	}

	private boolean validateDouble() {
		EditText editText = (EditText) mUiControl1;

		if (editText != null) {
			String text = editText.getText().toString();

			if (!mDataColumn.getAllowDbNull()) {
				editText.setError(TextUtils.isEmpty(text) ? KmbApplication.getContext().getText(R.string.required) : null);

				if (editText.getError() != null) {
					return false; // Required data missing
				}
			}

			if (!TextUtils.isEmpty(text)) {
				try {
					mUpdatedValue = Double.parseDouble(text);
				} catch (NumberFormatException ex) {
					editText.setError(KmbApplication.getContext().getText(R.string.invalid));
					return false;	// Invalid value
				}
			}
		}

		return true;
	}

	private boolean validateGuid() {
		EditText editText = (EditText) mUiControl1;

		if (editText != null) {
			String text = editText.getText().toString();

			if (!mDataColumn.getAllowDbNull()) {
				editText.setError(TextUtils.isEmpty(text) ? KmbApplication.getContext().getText(R.string.required) : null);

				if (editText.getError() != null) {
					return false; // Required data missing
				}
			}

			if (!TextUtils.isEmpty(text)) {
				try {
					mUpdatedValue = UUID.fromString(text);
				} catch (IllegalArgumentException e) {
					editText.setError(KmbApplication.getContext().getText(R.string.invalid));
					return false;	// Invalid value
				}
			}
		}

		return true;
	}

	private boolean validateString() {
		EditText editText = (EditText) mUiControl1;

		if (editText != null) {
			String text = editText.getText().toString();

			if (!mDataColumn.getAllowDbNull()) {
				editText.setError(TextUtils.isEmpty(text) ? KmbApplication.getContext().getText(R.string.required) : null);

				if (editText.getError() != null) {
					return false; // Required data missing
				}
			}

			if (!TextUtils.isEmpty(text)) {
				mUpdatedValue = text;
			}
		}

		return true;
	}

	/**
	 * Define 'column = value' notation for UPDATE statement but only for columns that have changed.
	 */
	public void generateUpdateSets(List<String> sets) {
		if (mDataColumn.getDataType() == DataTypeEnum.DATA_TYPE_BLOB) {
			return;	// can't update Blob
		}

		if (mDataColumn.getIsAutoIncrement()) {
			return; // don't update AutoIncrement columns
		}

		if (mOriginalValue == null && mUpdatedValue == null) {
			return;	// no change -- both NULL
		}

		if (mUpdatedValue == null) {
			sets.add(mDataColumn.getColumnName() + " = " + "NULL");
			return;	// value NULL'ed
		}

		if (mUpdatedValue instanceof Boolean) {
			Boolean updated = (Boolean) mUpdatedValue;

			if (mOriginalValue instanceof Boolean) {
				Boolean original = (Boolean) mOriginalValue;
				if (original.compareTo(updated) == 0) {
					return;	// no change
				}
			}

			sets.add(mDataColumn.getColumnName() + " = " + (updated ? 1 : 0));
		}
		else if (mUpdatedValue instanceof Date) {
			Date updated = (Date) mUpdatedValue;

			if (mOriginalValue instanceof Date) {
				Date original = (Date) mOriginalValue;
				if (original.compareTo(updated) == 0) {
					return;	// no change
				}
			}

			SimpleDateFormat sdf = new SimpleDateFormat(mDataColumn.getDatePattern() != null ? mDataColumn.getDatePattern() : DATETIME_PATTERN);
			sets.add(mDataColumn.getColumnName() + " = '" + sdf.format(updated) + "'");
		}
		else if (mUpdatedValue instanceof Integer) {
			Integer updated = (Integer) mUpdatedValue;

			if (mOriginalValue instanceof Integer) {
				Integer original = (Integer) mOriginalValue;
				if (original.compareTo(updated) == 0) {
					return;	// no change
				}
			}

			sets.add(mDataColumn.getColumnName() + " = " + updated.toString());
		}
		else if (mUpdatedValue instanceof Double) {
			Double updated = (Double) mUpdatedValue;

			if (mOriginalValue instanceof Double) {
				Double original = (Double) mOriginalValue;
				if (original.compareTo(updated) == 0) {
					return;	// no change
				}
			}

			sets.add(mDataColumn.getColumnName() + " = " + updated.toString());
		}
		else if (mUpdatedValue instanceof UUID) {
			UUID updated = (UUID) mUpdatedValue;

			if (mOriginalValue instanceof UUID) {
				UUID original = (UUID) mOriginalValue;
				if (original.compareTo(updated) == 0) {
					return;	// no change
				}
			}

			sets.add(mDataColumn.getColumnName() + " = '" + updated.toString() + "'");
		}
		else if (mUpdatedValue instanceof String) {
			String updated = (String) mUpdatedValue;

			if (mOriginalValue instanceof String) {
				String original = (String) mOriginalValue;
				if (original.compareTo(updated) == 0) {
					return;	// no change
				}
			}

			if (!TextUtils.isEmpty(updated)) {
				// Escape the apostrophe (i.e. double-up the single quote character) in your SQL
				updated = updated.replace("'", "''");
			}

			sets.add(mDataColumn.getColumnName() + " = '" + updated + "'");
		}
	}

	/**
	 * Define separate column list and value list for INSERT statement
	 */
	public void generateInsertValues(List<String> columns, List<String> values) {
		if (mDataColumn.getDataType() == DataTypeEnum.DATA_TYPE_BLOB) {
			return;	// can't update Blob
		}

		if (mDataColumn.getIsAutoIncrement()) {
			return; // don't update AutoIncrement columns
		}

		if (mUpdatedValue == null) {
			columns.add(mDataColumn.getColumnName());
			values.add("NULL");
			return;	// value NULL'ed
		}

		if (mUpdatedValue instanceof Boolean) {
			Boolean updated = (Boolean) mUpdatedValue;

			columns.add(mDataColumn.getColumnName());
			values.add(updated ? "1" : "0");
		}
		else if (mUpdatedValue instanceof Date) {
			Date updated = (Date) mUpdatedValue;

			columns.add(mDataColumn.getColumnName());

			SimpleDateFormat sdf = new SimpleDateFormat(mDataColumn.getDatePattern() != null ? mDataColumn.getDatePattern() : DATETIME_PATTERN);
			values.add("'" + sdf.format(updated) + "'");
		}
		else if (mUpdatedValue instanceof Integer) {
			Integer updated = (Integer) mUpdatedValue;

			columns.add(mDataColumn.getColumnName());
			values.add(updated.toString());
		}
		else if (mUpdatedValue instanceof Double) {
			Double updated = (Double) mUpdatedValue;

			columns.add(mDataColumn.getColumnName());
			values.add(updated.toString());
		}
		else if (mUpdatedValue instanceof UUID) {
			UUID updated = (UUID) mUpdatedValue;

			columns.add(mDataColumn.getColumnName());
			values.add("'" + updated.toString() + "'");
		}
		else if (mUpdatedValue instanceof String) {
			String updated = (String) mUpdatedValue;

			if (!TextUtils.isEmpty(updated)) {
				// Escape the apostrophe (i.e. double-up the single quote character) in your SQL
				updated = updated.replace("'", "''");
			}

			columns.add(mDataColumn.getColumnName());
			values.add("'" + updated + "'");
		}
	}
}
