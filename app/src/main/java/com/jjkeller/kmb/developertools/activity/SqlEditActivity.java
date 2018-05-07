package com.jjkeller.kmb.developertools.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.database.DataColumn;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.viewmodel.DataColumnValidation;
import com.jjkeller.kmb.developertools.database.DataSet;
import com.jjkeller.kmb.developertools.enumerator.DataTypeEnum;
import com.jjkeller.kmb.developertools.enumerator.EditModeEnum;
import com.jjkeller.kmb.developertools.viewmodel.SqlEditViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter.DATE_PATTERN;
import static com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter.TIME_PATTERN_12HOUR;

public class SqlEditActivity extends AppCompatActivity {
	public static final String EXTRA_COLUMNNAME = "columnName";
	public static final String DATE_SUFFIX = "_date";
	public static final String TIME_SUFFIX = "_time";

	private SqlEditViewModel mViewModel = new SqlEditViewModel();

	private HashMap<String, View> mEditControls = new HashMap<String, View>();
	private HashMap<String, String> mOrientationChangeSavedValues = new HashMap<String, String>();

	private TableLayout mFieldsTableLayout;
	private LinearLayout mProgressContainer;
	private ProgressDialog mSavingDialog;
	private int mDividerColor;
	private int mColorButtonNormal;

	public HashMap<String, View> getEditControls() { return mEditControls; }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Services.Theme().getThemeResourceId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sqledit);

		String sqlStatement = "";

		Intent intent = getIntent();
		if (null != intent) {
			mViewModel.setEditMode((EditModeEnum) intent.getSerializableExtra(SqlQueryActivity.EXTRA_EDITMODEENUM));
			sqlStatement = intent.getStringExtra(SqlQueryActivity.EXTRA_SQLSTATEMENT);
		}

		if (savedInstanceState != null) {
			for (String key : savedInstanceState.keySet()) {
				Object value = savedInstanceState.get(key);
				mOrientationChangeSavedValues.put(key, String.valueOf(value));
			}
		}

		setupViews();

		subscribeToViewModel(sqlStatement);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		for (Map.Entry<String, View> entry : mEditControls.entrySet()) {
			String key = entry.getKey();
			Object view = entry.getValue();

			if (view instanceof CheckBox) {
				outState.putString(key, ((CheckBox) view).isChecked() ? "true" : "false");
			}
			else if (view instanceof Button) {  // Date or Time buttons
				outState.putString(key, ((Button) view).getText().toString());
			}
			else if (view instanceof EditText) {
				outState.putString(key, ((EditText) view).getText().toString());
			}
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_sqledit, menu);

		// set icon color of menu
		Services.Theme().setMenuIconColor(menu, Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent));

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		boolean canReadDatabase = Services.File().canReadFile(Services.Database().getKmbDatabasePath());

		menu.getItem(0).setEnabled(canReadDatabase);	// Save

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menuSave:
				onClickMenuSave();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Define handles to the child views.
	 */
	private void setupViews() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel_white);
		toolbar.getNavigationIcon().setColorFilter(Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);

		if (mViewModel.getEditMode() == EditModeEnum.COPY) {
			getSupportActionBar().setTitle(getString(R.string.copy));
		}
		else {
			getSupportActionBar().setTitle(getString(R.string.edit));
		}


		// change the spinner color to match the primary theme color
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.getIndeterminateDrawable()
				.setColorFilter(Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent), PorterDuff.Mode.SRC_IN);

		mFieldsTableLayout = (TableLayout) findViewById(R.id.tableFieldsContainer);
		mProgressContainer = (LinearLayout) findViewById(R.id.llProgressContainer);

		mDividerColor = Services.Theme().getThemeAttribute(getTheme(), android.R.attr.divider);
		mColorButtonNormal = Services.Theme().getThemeAttribute(getTheme(), R.attr.colorButtonNormal);
	}

	/**
	 * Bind the View and ViewModel.
	 */
	private void subscribeToViewModel(String sql) {
		mViewModel.executeQuery(sql)
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::executeQueryComplete, this::handleError);
	}

	/**
	 * Successful Async fetch of dataset filtered to the record to Edit.
	 */
	private void executeQueryComplete(@NonNull final DataSet results) {
		mViewModel.setDataSet(results);

		// Build UI views for each column.
		createColumnChildViews();
	}

	/**
	 * Handle generic Throwable error.
	 */
	private void handleError(@NonNull final Throwable e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.error).toUpperCase())
				.setMessage(e.getMessage())
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
	}

	private void onClickMenuSave() {
		clearFocusAndDismissSIP();
		onSave();
	}

	/**
	 * Validate and Save data
	 */
	private void onSave() {
		mSavingDialog = new ProgressDialog(this);
		mSavingDialog.setMessage(getString(R.string.saving));
		mSavingDialog.setCancelable(false);
		mSavingDialog.show();

		List<DataColumnValidation> validationList = new ArrayList<>();

		// Create DataColumnValidation for each column/editControl
		for (DataColumn column : mViewModel.getDataSet().getColumns()) {

			Object originalValue = mViewModel.getDataSet().getRowAt(0).get(column.getColumnName());

			if (column.getDataType() == DataTypeEnum.DATA_TYPE_DATE) {
				Button dateButton = (Button) mEditControls.get(column.getColumnName() + DATE_SUFFIX);

				validationList.add(new DataColumnValidation(column, originalValue, dateButton));
			}
			else if (column.getDataType() == DataTypeEnum.DATA_TYPE_DATETIME) {
				Button dateButton = (Button) mEditControls.get(column.getColumnName() + DATE_SUFFIX);
				Button timeButton = (Button) mEditControls.get(column.getColumnName() + TIME_SUFFIX);

				validationList.add(new DataColumnValidation(column, originalValue, dateButton, timeButton));
			}
			else {
				View control = mEditControls.get(column.getColumnName());
				validationList.add(new DataColumnValidation(column, originalValue, control));
			}
		}

		// call Validate for each DateColumnValidation -- return false if error exists
		boolean hasBrokenRules = false;
		for (DataColumnValidation validator : validationList) {
			if (!validator.validate()) {
				hasBrokenRules = true;
			}
		}

		if (hasBrokenRules) {
			mSavingDialog.dismiss();

			android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.error).toUpperCase())
					.setMessage(getString(R.string.fixerrors))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							return;
						}
					})
					.show();
		}
		else {
			String sql = generateSaveSqlStatement(validationList);
			if (sql == null) {	// null means no values have changed -- nothing to update
				mSavingDialog.dismiss();
				finish();
			}
			else {
				mViewModel.executeQuery(sql)
						.subscribeOn(Schedulers.newThread())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::executeUpdateComplete, this::handleError);
			}
		}
	}

	/**
	 * Successful Async execution of Insert/Update SQL statement
	 */
	private void executeUpdateComplete(@NonNull final DataSet results) {
		mSavingDialog.dismiss();
		setResult(RESULT_OK);	// tell calling startActivityForResult that Save was successful
		finish();
	}

	private String generateSaveSqlStatement(List<DataColumnValidation> validationList) {

		if (mViewModel.getEditMode() == EditModeEnum.EDIT) {
			List<String> sets = new ArrayList<String>();

			for (DataColumnValidation validator : validationList) {
				// each validator will create it's own 'column = value' statement if the value has changed
				validator.generateUpdateSets(sets);
			}

			if (sets.isEmpty()) {
				return null;	// no values changed
			}

			String setClause = TextUtils.join(",", sets);

			mViewModel.getDataSet().getRowAt(0).setIsRowSelected(true);
			String whereClause = mViewModel.getDataSet().buildSelectedWhereClause();

			// UPDATE table_name
			// SET column1 = value1, column2 = value2...., columnN = valueN
			// WHERE [condition];
			return String.format(getString(R.string.update_statement), mViewModel.getDataSet().getTables().get(0), setClause, whereClause);
		}
		else {
			// CLONE = Insert
			List<String> columns = new ArrayList<String>();
			List<String> values = new ArrayList<String>();

			for (DataColumnValidation validator : validationList) {
				validator.generateInsertValues(columns, values);
			}

			String columnsClause = TextUtils.join(",", columns);
			String valuesClause = TextUtils.join(",", values);

			mViewModel.getDataSet().getRowAt(0).setIsRowSelected(true);

			// INSERT INTO table_name (column1, column2, column3, ...)
			// VALUES (value1, value2, value3, ...);
			return String.format(getString(R.string.insert_statement), mViewModel.getDataSet().getTables().get(0), columnsClause, valuesClause);
		}
	}

	/**
	 * Build UI views for each column.
	 */
	private void createColumnChildViews() {
		for (DataColumn column : mViewModel.getDataSet().getColumns()) {
			TableRow row = createTableRow();

			row.addView(createFieldLabel(column));

			View view = createEditView(column);
			if (view != null) {
				row.addView(view);
			}

			mFieldsTableLayout.addView(row);
		}

		mFieldsTableLayout.setVisibility(View.VISIBLE);

		mProgressContainer.setVisibility(View.GONE);
	}

	/**
	 * Create a new TableRow to represent the Label and Control combination of a DataColumn
	 */
	private TableRow createTableRow() {
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		tr.setWeightSum(1); // total row weight

		return tr;
	}

	private TextView createFieldLabel(DataColumn column) {
		TextView tv = new TextView(this);
		TextViewCompat.setTextAppearance(tv, android.R.style.TextAppearance_Medium);
		tv.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		tv.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, .3f));
		tv.setMaxWidth(300);
		tv.setPadding(0, 20, 40, 20);
		tv.setSingleLine(true);
		tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
		tv.setTextColor(mDividerColor);

		String columnName = column.getColumnName();

		if (!column.getAllowDbNull()) {
			Spannable spanText = new SpannableString("* " + columnName);
			spanText.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, 0);
			tv.setText(spanText);
		}
		else {
			tv.setText(columnName);
		}

		SqlEditActivity self = this;

		tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// word wrap entire column name
				tv.setSingleLine(false);
			}
		});


		return tv;
	}

	private View createEditView(DataColumn column) {

		View view = null;

		switch (column.getDataType()) {
			case DATA_TYPE_BOOLEAN:
				view = createCheckBox(column);
				break;

			case DATA_TYPE_DOUBLE:
			case DATA_TYPE_INTEGER:
				if (column.getIsAutoIncrement()) {
					view = createAutoIncrement(column, 0.5f);
				}
				else {
					view = createEditText(column, 0.5f);
				}
				break;

			case DATA_TYPE_DATE:
				view = createDateClearButtons(column);
				break;

			case DATA_TYPE_DATETIME:
				view = createDateTimeButtons(column);
				break;

			case DATA_TYPE_NULL:
				break;

			case DATA_TYPE_BLOB:
				view = createBlob(column);
				break;

			default:
				// DATA_TYPE_GUID
				// DATA_TYPE_STRING
				view = createEditText(column, 1.0f);
				break;
		}

		return view;
	}

	private TextView createAutoIncrement(DataColumn column, float initWeight) {
		TextView tv = new TextView(this);
		TextViewCompat.setTextAppearance(tv, android.R.style.TextAppearance_Medium);
		tv.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, initWeight));

		String text = "";
		if (mViewModel.getEditMode() == EditModeEnum.COPY) {
			text = getString(R.string.autoincrement);
		}
		else {
			text = String.valueOf(mViewModel.getDataSet().getRowAt(0).get(column.getColumnName()));
		}

		tv.setText(text);

		return tv;
	}

	private CheckBox createCheckBox(DataColumn column) {
		// views only get theme applied when inflated from xml - so load appropriate theme attributes get applied
		CheckBox cb = (CheckBox) View.inflate(this, R.layout.theme_checkbox, null);
		cb.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));

		Boolean checked = false;
		if (mOrientationChangeSavedValues.isEmpty()) {
			checked = (Boolean) mViewModel.getDataSet().getRowAt(0).get(column.getColumnName()) == true;
		}
		else {
			checked = mOrientationChangeSavedValues.get(column.getColumnName()).equalsIgnoreCase("true") ? true : false;
		}

		cb.setChecked(checked);

		mEditControls.put(column.getColumnName(), cb);

		return cb;
	}

	private EditText createEditText(DataColumn column, float initWeight) {

		EditText et = new EditText(this);
		et.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, initWeight));

		if (column.getDataType() == DataTypeEnum.DATA_TYPE_INTEGER) {
			et.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		else if (column.getDataType() == DataTypeEnum.DATA_TYPE_DOUBLE) {
			et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}

		String text = "";
		if (mOrientationChangeSavedValues.isEmpty()) {
			Object obj = mViewModel.getDataSet().getRowAt(0).get(column.getColumnName());
			if (obj != null) {
				text = String.valueOf(obj);
			}
		}
		else {
			text = mOrientationChangeSavedValues.get(column.getColumnName());
		}

		et.setText(text);

		mEditControls.put(column.getColumnName(), et);

		return et;
	}

	private Button createDateButton(DataColumn column) {
		Button btnDate = new Button(this);

		String text = "";
		if (mOrientationChangeSavedValues.isEmpty()) {
			Object obj = mViewModel.getDataSet().getRowAt(0).get(column.getColumnName());
			if (obj != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
				text = dateFormat.format((Date) obj);
			}
		}
		else {
			text = mOrientationChangeSavedValues.get(column.getColumnName() + DATE_SUFFIX);
		}

		btnDate.setText(text);
		btnDate.setBackgroundColor(mColorButtonNormal);
		btnDate.setPadding(10, 0, 10, 0);

		mEditControls.put(column.getColumnName() + DATE_SUFFIX, btnDate);

		btnDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();

				Bundle bundle = new Bundle();
				bundle.putString(EXTRA_COLUMNNAME, column.getColumnName() + DATE_SUFFIX);
				newFragment.setArguments(bundle);

				newFragment.show(getSupportFragmentManager(), "timePicker");
			}
		});

		return btnDate;
	}

	private Button createTimeButton(DataColumn column) {
		Button btnTime = new Button(this);

		String text = "";
		if (mOrientationChangeSavedValues.isEmpty()) {
			Object obj = mViewModel.getDataSet().getRowAt(0).get(column.getColumnName());
			if (obj != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN_12HOUR);
				text = dateFormat.format((Date) obj);
			}
		}
		else {
			text = mOrientationChangeSavedValues.get(column.getColumnName() + TIME_SUFFIX);
		}

		btnTime.setText(text);
		btnTime.setBackgroundColor(mColorButtonNormal);
		btnTime.setPadding(10, 0, 10, 0);

		mEditControls.put(column.getColumnName() + TIME_SUFFIX, btnTime);

		btnTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new TimePickerFragment();

				Bundle bundle = new Bundle();
				bundle.putString(EXTRA_COLUMNNAME, column.getColumnName() + TIME_SUFFIX);
				newFragment.setArguments(bundle);

				newFragment.show(getSupportFragmentManager(), "timePicker");
			}
		});

		return btnTime;
	}

	private LinearLayout createDateClearButtons(DataColumn column) {
		LinearLayout ll = new LinearLayout(this);
		ll.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.addView(createDateButton(column));

		if (column.getAllowDbNull()) {
			ll.addView(createDateTimeClearButton(column));
		}

		return ll;
	}

	private LinearLayout createDateTimeButtons(DataColumn column) {
		LinearLayout ll = new LinearLayout(this);
		ll.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.addView(createDateButton(column));

		Button btnTime = createTimeButton(column);

		// add spacing between Date and Time buttons
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);
		params.setMargins(10, 0, 0, 0);
		btnTime.setLayoutParams(params);

		ll.addView(btnTime);

		if (column.getAllowDbNull()) {
			ll.addView(createDateTimeClearButton(column));
		}

		return ll;
	}

	private ImageButton createDateTimeClearButton(DataColumn column) {
		ImageButton btnClear = new ImageButton(this);
		btnClear.setImageResource( R.drawable.ic_close_gray);
		btnClear.setBackgroundColor(Color.TRANSPARENT);
		btnClear.setColorFilter(Color.argb(255, 211, 211, 211));

		btnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				View button = getEditControl(column.getColumnName() + DATE_SUFFIX);
				if (button != null && button instanceof Button) {
					Button btnDate = (Button) button;
					btnDate.setText("");
				}

				button = getEditControl(column.getColumnName() + TIME_SUFFIX);
				if (button != null && button instanceof Button) {
					Button btnTime = (Button) button;
					btnTime.setText("");
				}
			}
		});

		return btnClear;
	}

	private TextView createBlob(DataColumn column) {
		TextView tv = new TextView(this);
		tv.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
		TextViewCompat.setTextAppearance(tv, android.R.style.TextAppearance_Medium);

		String text = getString(R.string.blob_string);
		tv.setText(text);

		return tv;
	}

	private View getEditControl(String columnName) {
		if (!TextUtils.isEmpty(columnName)) {
			if (mEditControls != null) {
				return mEditControls.get(columnName);
			}
		}

		return null;
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		private SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_PATTERN);
		private String mColumnName;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// handle com.jjkeller.kmb.developertools.fragment arguments
			Bundle arguments = getArguments();
			if(arguments != null) {
				mColumnName = arguments.getString(EXTRA_COLUMNNAME);
			}

			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			View button = ((SqlEditActivity) getActivity()).getEditControl(mColumnName);
			if (button != null && button instanceof Button) {
				Button btnDate = (Button) button;

				try {
					c.setTime(mDateFormat.parse(btnDate.getText().toString()));

					year = c.get(Calendar.YEAR);
					month = c.get(Calendar.MONTH);
					day = c.get(Calendar.DAY_OF_MONTH);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			View button = ((SqlEditActivity) getActivity()).getEditControl(mColumnName);
			if (button != null && button instanceof Button) {
				Button btnDate = (Button) button;

				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(0);
				cal.set(year, month, day);

				btnDate.setError(null);
				btnDate.setText(mDateFormat.format(cal.getTime()));
			}
		}
	}

	public static class TimePickerFragment extends DialogFragment
			implements MyTimePickerDialog.OnTimeSetListener {

		private SimpleDateFormat mTimeFormat_12hour = new SimpleDateFormat(TIME_PATTERN_12HOUR);
		private String mColumnName;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			// handle com.jjkeller.kmb.developertools.fragment arguments
			Bundle arguments = getArguments();
			if(arguments != null) {
				mColumnName = arguments.getString(EXTRA_COLUMNNAME);
			}

			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			int second = c.get(Calendar.SECOND);

			View button = ((SqlEditActivity) getActivity()).getEditControl(mColumnName);
			if (button != null && button instanceof Button) {
				Button btnTime = (Button) button;

				try {
					c.setTime(mTimeFormat_12hour.parse(btnTime.getText().toString()));

					hour = c.get(Calendar.HOUR_OF_DAY);
					minute = c.get(Calendar.MINUTE);
					second = c.get(Calendar.SECOND);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			// Create a new instance of TimePickerDialog and return it
			return new MyTimePickerDialog(getActivity(), this, hour, minute, second,false);
		}

		@Override
		public void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
			View button = ((SqlEditActivity) getActivity()).getEditControl(mColumnName);
			if (button != null && button instanceof Button) {
				Button btnTime = (Button) button;
				String status = getString(R.string.am);

				if(hourOfDay > 11)
				{
					// If the hour is greater than or equal to 12
					// Then the current AM PM status is PM
					status = getString(R.string.pm);
				}

				// Initialize a new variable to hold 12 hour format hour value
				int hour_of_12_hour_format;

				if (hourOfDay == 0) {
					hour_of_12_hour_format = 12;
				}
				else if(hourOfDay > 12){

					// If the hour is greater than or equal to 12
					// Then we subtract 12 from the hour to make it 12 hour format time
					hour_of_12_hour_format = hourOfDay - 12;
				}
				else {
					hour_of_12_hour_format = hourOfDay;
				}

				btnTime.setError(null);
				btnTime.setText((hour_of_12_hour_format < 10 ? "0" : "") + hour_of_12_hour_format + ":" + (minute < 10 ? "0" : "") + minute + ":" + (seconds < 10 ? "0" : "") + seconds + " " + status);
			}
		}
	}

	/*----------------------------------------
	 * Helper methods
	 *----------------------------------------*/

	/**
	 * Clear focus from active control and dismiss the keyboard if showing.
	 */
	private void clearFocusAndDismissSIP() {
		View current = getCurrentFocus();
		if (current != null) {
			current.clearFocus();

			// hide keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
		}
	}
}
