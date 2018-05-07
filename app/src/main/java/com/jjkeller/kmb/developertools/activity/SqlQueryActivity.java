package com.jjkeller.kmb.developertools.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter;
import com.jjkeller.kmb.developertools.database.DataColumn;
import com.jjkeller.kmb.developertools.database.DataRow;
import com.jjkeller.kmb.developertools.database.DataSet;
import com.jjkeller.kmb.developertools.dialog.EditTextDialog;
import com.jjkeller.kmb.developertools.enumerator.EditModeEnum;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.viewmodel.SqlQueryViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;
import static com.jjkeller.kmb.developertools.enumerator.DataTypeEnum.DATA_TYPE_BOOLEAN;
import static com.jjkeller.kmb.developertools.enumerator.DataTypeEnum.DATA_TYPE_DOUBLE;
import static com.jjkeller.kmb.developertools.enumerator.DataTypeEnum.DATA_TYPE_INTEGER;

/**
 * Activity to execute SQL statements and view results.
 */

public class SqlQueryActivity extends AppCompatActivity {

	public static final int STARTACTIVITY_SQLEDIT = 1;
	public static final String EXTRA_SQLSTATEMENT = "sqlStatement";
	public static final String EXTRA_EDITMODEENUM = "editModeEnum";

	private SqlQueryViewModel mViewModel = new SqlQueryViewModel();
	private SqlQueryAdapter mAdapter;
	private String mLastFileOpenFileName = "";

	private CoordinatorLayout mCoordinatorLayout;
	private EditText mSqlText;
	private LinearLayout mSqlQueryContainer;
	private LinearLayout mHeaderLayout;
	private Toolbar mToolbarResults;
	private LinearLayout mSelectAllLayout;
	private CheckBox mSelectAllCheckBox;
	private MenuItem mDeleteRecordButton;
	private MenuItem mEditRecordButton;
	private MenuItem mCopyRecordButton;
	private MenuItem mRecordCount;
	private MenuItem mFullScreenButton;
	private MenuItem mExitFullScreenButton;
	private ListView mListView;
	private int mRowsSelectedCount = 0;
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Services.Theme().getThemeResourceId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sqlquery);

		setupViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_sqlquery, menu);

		// set icon color of menu
		Services.Theme().setMenuIconColor(menu, Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent));

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean canReadDatabase = Services.File().canReadFile(Services.Database().getKmbDatabasePath());

		menu.getItem(0).setEnabled(canReadDatabase);    // Execute
		menu.getItem(1).setEnabled(canReadDatabase);    // Clear
		menu.getItem(2).setEnabled(canReadDatabase);    // Open SQL Script
		menu.getItem(3).setEnabled(canReadDatabase);    // Save SQL Script

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menuExecute:
				onClickMenuExecute();
				return true;
			case R.id.menuClear:
				onClickMenuClear();
				return true;
			case R.id.menuOpenSqlScript:
				onClickMenuOpenSqlScript();
				return true;
			case R.id.menuSaveSqlScript:
				onClickMenuSaveSqlScript();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == STARTACTIVITY_SQLEDIT) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				onClickMenuExecute();
			}
		}
	}

	/**
	 * Define handles to the child views.
	 */
	private void setupViews() {
		SqlQueryActivity self = this;

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// use menu for Results header -- running on phones will collapse the menus without overlapping
		mToolbarResults = (Toolbar) findViewById(R.id.toolbarResults);
		mToolbarResults.inflateMenu(R.menu.menu_sqlquery_results);

		// set icon color of menu
		Services.Theme().setMenuIconColor(mToolbarResults.getMenu(), Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent));

		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
		mSqlQueryContainer = (LinearLayout) findViewById(R.id.llSqlQueryContainer);
		mRecordCount = (MenuItem) mToolbarResults.getMenu().findItem(R.id.menuRecordCount);
		mHeaderLayout = (LinearLayout) findViewById(R.id.headerContainer);
		mSelectAllLayout = (LinearLayout) findViewById(R.id.selectAllContainer);
		mSelectAllCheckBox = (CheckBox) findViewById(R.id.headerSelected);

		mEditRecordButton = (MenuItem) mToolbarResults.getMenu().findItem(R.id.menuEdit);
		mEditRecordButton.setVisible(false);
		mEditRecordButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onClickMenuEdit();
				return true;
			}
		});

		mDeleteRecordButton = (MenuItem) mToolbarResults.getMenu().findItem(R.id.menuDelete);
		mDeleteRecordButton.setVisible(false);
		mDeleteRecordButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onClickMenuDelete();
				return true;
			}
		});

		mCopyRecordButton = (MenuItem) mToolbarResults.getMenu().findItem(R.id.menuCopy);
		mCopyRecordButton.setVisible(false);
		mCopyRecordButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onClickMenuCopy();
				return true;
			}
		});

		mFullScreenButton = (MenuItem) mToolbarResults.getMenu().findItem(R.id.menuFullScreen);
		mFullScreenButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onClickMenuFullScreen();
				return true;
			}
		});

		mExitFullScreenButton = (MenuItem) mToolbarResults.getMenu().findItem(R.id.menuExitFullScreen);
		mExitFullScreenButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onClickMenuExitFullScreen();
				return true;
			}
		});

		mListView = (ListView) findViewById(R.id.listView);
		mSqlText = (EditText) findViewById(R.id.editSqlStmt);
		mSqlText.setText(Services.Preferences().getLastExecutedSqlQuery());
		positionCursorAtEndOfText();
	}

	private void onClickMenuExecute() {
		String sql = mSqlText.getText().toString().trim();

		if (sql.isEmpty())
			return;        // no SQL entered - nothing to execute

		mRecordCount.setTitle("");
		mSelectAllCheckBox.setChecked(false);
		clearFocusAndDismissSIP();

		mProgressDialog = ProgressDialog.show(SqlQueryActivity.this, "", getString(R.string.executing_query));
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();

		mViewModel.executeQuery(sql)
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::executeQueryComplete, this::handleError);
	}

	private void onClickMenuClear() {
		mSqlText.setText("");
	}

	/**
	 * Allow user to choose which stored SQL file to open.
	 */
	private void onClickMenuOpenSqlScript() {
		List<String> fileList = new ArrayList<String>();

		File[] sqlFiles = Services.File().getDirectoryFiles(Services.File().getSqlFileDirectory());

		// sort files by Name ASC
		Arrays.sort(sqlFiles, new Comparator<File>() {
			@Override
			public int compare(File object1, File object2) {
				return object1.getName().compareTo(object2.getName());
			}
		});

		for (int i = 0; i < sqlFiles.length; i++) {
			fileList.add(sqlFiles[i].getName());
		}

		// auto add basic templates
		fileList.add(0, getString(R.string.sql_select_name));
		fileList.add(1, getString(R.string.sql_insert_name));
		fileList.add(2, getString(R.string.sql_update_name));
		fileList.add(3, getString(R.string.sql_delete_name));

		SqlQueryActivity self = this;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.open_file));
		builder.setItems(fileList.toArray(new String[fileList.size()]), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				mLastFileOpenFileName = "";
				switch (which) {
					case 0:
						mSqlText.setText(getString(R.string.sql_select_template));
						break;
					case 1:
						mSqlText.setText(getString(R.string.sql_insert_template));
						break;
					case 2:
						mSqlText.setText(getString(R.string.sql_update_template));
						break;
					case 3:
						mSqlText.setText(getString(R.string.sql_delete_template));
						break;
					default:
						File file = sqlFiles[which - 4];

						try {
							String content = Services.File().getStringFromFile(file.getPath());
							mSqlText.setText(content.trim());
							mLastFileOpenFileName = file.getName();
						} catch (IOException e) {
							e.printStackTrace();

							AlertDialog.Builder builder = new AlertDialog.Builder(self);
							builder.setTitle(getString(R.string.error).toUpperCase())
									.setMessage(e.getMessage())
									.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									})
									.show();
						}
				}

				positionCursorAtEndOfText();
			}
		});
		builder.show();
	}

	/**
	 * Save current script to .sql file.
	 */
	private void onClickMenuSaveSqlScript() {
		SqlQueryActivity self = this;

		// Prompt user for SaveAs file name
		EditTextDialog dialog = new EditTextDialog(this, getString(R.string.save_script), mLastFileOpenFileName, getString(R.string.save));
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dlg) {
				String result = dialog.getTextInput();
				if (!TextUtils.isEmpty(result)) {
					if (!result.toLowerCase().endsWith(".sql")) {
						result += ".sql";
					}

					try {
						Services.File().saveStringToFile(Services.File().getSqlFileDirectory() + result, mSqlText.getText().toString().trim());
					} catch (IOException e) {
						e.printStackTrace();

						AlertDialog.Builder builderError = new AlertDialog.Builder(self);
						builderError.setTitle(getString(R.string.error).toUpperCase())
								.setMessage(e.getMessage())
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								})
								.show();
					}
				}
			}
		});
		dialog.show();
	}

	private void onClickMenuEdit() {
		clearFocusAndDismissSIP();

		Intent intent = new Intent(getApplicationContext(), SqlEditActivity.class);
		intent.putExtra(EXTRA_EDITMODEENUM, EditModeEnum.EDIT);
		intent.putExtra(EXTRA_SQLSTATEMENT, mAdapter.getDataSet().generateSelectedRowsFilterStatement());

		startActivityForResult(intent, STARTACTIVITY_SQLEDIT);
	}

	private void onClickMenuDelete() {
		clearFocusAndDismissSIP();

		AlertDialog.Builder alert = new AlertDialog.Builder(SqlQueryActivity.this);
		alert.setTitle(getString(R.string.confirm_deletion));
		alert.setMessage(getString(R.string.delete_selected_records));
		alert.setPositiveButton(R.string.delete, (dialog, which) -> {
			mProgressDialog = ProgressDialog.show(SqlQueryActivity.this, "", getString(R.string.progress_deleting));
			mProgressDialog.show();

			String primaryKeyColumnName = mAdapter.getDataSet().getPrimaryKeys().get(0).getColumnName();

			List<Integer> keysToDelete = new ArrayList<Integer>();
			for (int position = 0; position < mAdapter.getCount(); position++) {
				DataRow dataRow = (DataRow) mAdapter.getItem(position);
				if (dataRow.getIsRowSelected()) {
					keysToDelete.add((Integer) dataRow.get(primaryKeyColumnName));
				}
			}

			mViewModel.deleteRecord(mAdapter.getDataSet().getTables().get(0), keysToDelete)
					.subscribeOn(Schedulers.newThread())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(this::deleteRecordComplete, this::handleError);
		});
		alert.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
		alert.show();
	}

	private void onClickMenuCopy() {
		clearFocusAndDismissSIP();

		Intent intent = new Intent(getApplicationContext(), SqlEditActivity.class);
		intent.putExtra(EXTRA_EDITMODEENUM, EditModeEnum.COPY);
		intent.putExtra(EXTRA_SQLSTATEMENT, mAdapter.getDataSet().generateSelectedRowsFilterStatement());

		startActivityForResult(intent, STARTACTIVITY_SQLEDIT);
	}

	private void onClickMenuFullScreen() {
		clearFocusAndDismissSIP();

		mFullScreenButton.setVisible(false);
		mExitFullScreenButton.setVisible(true);

		mSqlQueryContainer.setVisibility(GONE);
	}

	private void onClickMenuExitFullScreen() {
		clearFocusAndDismissSIP();

		mExitFullScreenButton.setVisible(false);
		mFullScreenButton.setVisible(true);

		mSqlQueryContainer.setVisibility(View.VISIBLE);
	}

	/**
	 * Successful Async fetch of backup file list.
	 */
	private void executeQueryComplete(@NonNull final DataSet results) {
		// store last executed query string
		Services.Preferences().setLastExecutedSqlQuery(mSqlText.getText().toString().trim());

		// remove header views from previous query results -- but keep Select All checkbox
		for (int i = mHeaderLayout.getChildCount() - 1; i >= 0; i--) {
			View currentChild = mHeaderLayout.getChildAt(i);
			if (currentChild instanceof TextView) {
				mHeaderLayout.removeView(currentChild);
			}
		}

		int textColor = Services.Theme().getThemeAttribute(getTheme(), android.R.attr.textColorPrimary);

		// add Select All CheckBox only if row has a unique identifier 'Key'
		mSelectAllLayout.setVisibility(results.getPrimaryKeys().size() == 1 ? View.VISIBLE : GONE);
		mSelectAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				for (int position = 0; position < mAdapter.getCount(); position++) {
					DataRow dataRow = (DataRow) mAdapter.getItem(position);
					dataRow.setIsRowSelected(isChecked);
				}

				mAdapter.notifyDataSetChanged();

				mRowsSelectedCount = isChecked ? mAdapter.getCount() : 0;

				mDeleteRecordButton.setVisible(mRowsSelectedCount > 0 ? true : false);
				mEditRecordButton.setVisible(mRowsSelectedCount == 1 ? true : false);
				mCopyRecordButton.setVisible(mRowsSelectedCount == 1 ? true : false);
			}
		});

		SqlQueryActivity self = this;

		// add column headers
		for (DataColumn column : results.getColumns()) {
			TextView tv = new TextView(KmbApplication.getContext());

			int width = SqlQueryAdapter.COLUMNN_WIDTH_STRING;
			if (column.getDataType() == DATA_TYPE_BOOLEAN || column.getDataType() == DATA_TYPE_DOUBLE || column.getDataType() == DATA_TYPE_INTEGER) {
				width = SqlQueryAdapter.COLUMNN_WIDTH_NUMBER;
			}

			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
			tv.setLayoutParams(layoutParams);

			TextViewCompat.setTextAppearance(tv, android.R.style.TextAppearance_Medium);
			tv.setTextColor(textColor);
			tv.setGravity(Gravity.CENTER_VERTICAL);
			tv.setSingleLine(true);
			tv.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			tv.setPadding(SqlQueryAdapter.COLUMN_PADDING_LEFT, SqlQueryAdapter.COLUMN_PADDING_TOP, SqlQueryAdapter.COLUMN_PADDING_RIGHT, SqlQueryAdapter.COLUMN_PADDING_BOTTOM);
			tv.setText(column.getColumnName());
			tv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// on Header click - copy column name to the clipboard so it can be pasted into Query syntax
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Copied Text", column.getColumnName());
					clipboard.setPrimaryClip(clip);

					// Toast column name
					Toast.makeText(self, column.getColumnName(), Toast.LENGTH_LONG).show();
				}
			});

			mHeaderLayout.addView(tv);
		}

		mRowsSelectedCount = 0;

		mDeleteRecordButton.setVisible(false);
		mEditRecordButton.setVisible(false);
		mCopyRecordButton.setVisible(false);

		mRecordCount.setTitle(String.format(getString(R.string.row_count), results.getRows().size()));
		mProgressDialog.dismiss();

		mAdapter = new SqlQueryAdapter(results, new SqlQueryAdapter.ISqlQueryAdapterListener() {
			@Override
			public void selectedOnCheckedChanged(final int position, final boolean isChecked) {
				DataRow dataRow = (DataRow) mAdapter.getItem(position);
				dataRow.setIsRowSelected(isChecked);

				mRowsSelectedCount += isChecked ? 1 : -1;

				mDeleteRecordButton.setVisible(mRowsSelectedCount > 0 ? true : false);
				mEditRecordButton.setVisible(mRowsSelectedCount == 1 ? true : false);
				mCopyRecordButton.setVisible(mRowsSelectedCount == 1 ? true : false);
			}
		});

		mListView.setAdapter(mAdapter);
		mListView.setVisibility(View.VISIBLE);
	}

	/**
	 * Successful Async delete of selected records.
	 */
	private void deleteRecordComplete(@NonNull final Integer results) {
		if (results > 0) {
			for (int position = mAdapter.getCount() - 1; position >= 0; position--) {
				DataRow dataRow = (DataRow) mAdapter.getItem(position);
				if (dataRow.getIsRowSelected()) {
					mAdapter.getDataSet().getRows().remove(position);
				}
			}
		}

		mAdapter.notifyDataSetChanged();

		mRowsSelectedCount = 0;

		mDeleteRecordButton.setVisible(mRowsSelectedCount > 0 ? true : false);
		mEditRecordButton.setVisible(mRowsSelectedCount == 1 ? true : false);
		mCopyRecordButton.setVisible(mRowsSelectedCount == 1 ? true : false);

		mRecordCount.setTitle(String.format(getString(R.string.row_count), mAdapter.getCount()));
		mProgressDialog.dismiss();

		Snackbar.make(mCoordinatorLayout, getText(R.string.delete_successful), Snackbar.LENGTH_LONG)
				.setAction("Action", null).show();
	}

	/**
	 * Handle generic Throwable error.
	 */
	private void handleError(@NonNull final Throwable e) {
		mRecordCount.setTitle(getString(R.string.query_completed_with_errors));
		mProgressDialog.dismiss();

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

	/**
	 *  Put the cursor at the end of the text
	 */
	private void positionCursorAtEndOfText() {
		String text = mSqlText.getText().toString();
		if(!TextUtils.isEmpty(text)) {
			int textLength = text.length();
			mSqlText.setSelection(textLength, textLength);
		}
	}
}