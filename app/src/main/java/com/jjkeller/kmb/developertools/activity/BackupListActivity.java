package com.jjkeller.kmb.developertools.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.adapter.BackupListAdapter;
import com.jjkeller.kmb.developertools.dialog.DiagnosticsDialog;
import com.jjkeller.kmb.developertools.dialog.EditTextDialog;
import com.jjkeller.kmb.developertools.enumerator.DiagnosticDataEnum;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.model.DatabaseBackupModel;
import com.jjkeller.kmb.developertools.viewmodel.BackupListViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity to manage database backups.
 */

public class BackupListActivity extends AppCompatActivity {

	private BackupListViewModel mViewModel = new BackupListViewModel();
	private BackupListAdapter mAdapter;

	private MenuItem mBackupButton;
	private MenuItem mEmailButton;
	private MenuItem mDeleteButton;

	private CoordinatorLayout mCoordinatorLayout;
	private FrameLayout mProgressContainer;
	private RecyclerView mRecyclerView;
	private TextView mNoRecords;
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Services.Theme().getThemeResourceId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backuplist);

		setupViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_backup_list, menu);

		mBackupButton = (MenuItem) menu.findItem(R.id.menuBackup);
		mEmailButton = (MenuItem) menu.findItem(R.id.menuEmail);
		mDeleteButton = (MenuItem) menu.findItem(R.id.menuDelete);

		subscribeToViewModel();

		// set icon color of menu
		Services.Theme().setMenuIconColor(menu, Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent));

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		boolean canReadDatabaseFile = Services.File().canReadFile(Services.Database().getKmbDatabasePath());

		menu.getItem(0).setEnabled(canReadDatabaseFile);	// Backup Now
		menu.getItem(1).setEnabled(canReadDatabaseFile);	// Email
		menu.getItem(2).setEnabled(canReadDatabaseFile);	// Delete

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menuBackup:
				onClickBackupNow();
				return true;
			case R.id.menuEmail:
				onClickMenuEmail();
				return true;
			case R.id.menuDelete:
				onClickMenuDelete();
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

		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
		mProgressContainer = (FrameLayout) findViewById(R.id.progressContainer);

		// change the spinner color to match the primary theme color
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.getIndeterminateDrawable()
				.setColorFilter(Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent), PorterDuff.Mode.SRC_IN);

		mNoRecords = (TextView) findViewById(R.id.textNoRecords);

		// Get a reference to the RecyclerView, and attach this adapter to it.
		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
	}

	/**
	 * Bind the View and ViewModel.
	 */
	private void subscribeToViewModel() {
		mViewModel.getBackupList()
			.subscribeOn(Schedulers.newThread())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(this::getBackupListComplete, this::handleError);
	}

	private void onClickBackupNow() {
		BackupListActivity self = this;

		// Prompt user for backup file name
		EditTextDialog dialog = new EditTextDialog(this, getString(R.string.backup_file_name), getString(R.string.backup_default_name), getString(R.string.backup_now));
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dlg) {
				String result = dialog.getTextInput();
				if (!TextUtils.isEmpty(result)) {
					mProgressDialog = ProgressDialog.show(BackupListActivity.this, "", getString(R.string.progress_backingup));
					mProgressDialog.show();

					mViewModel.backupDatabase(result)
							.subscribeOn(Schedulers.newThread())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(self::backupDatabaseComplete, self::handleError);
				}
			}
		});
		dialog.show();
	}

	private void onClickMenuEmail() {
		// Prompt user to email attach Database and/or ErrorLog
		DiagnosticsDialog dialog = new DiagnosticsDialog(this, DiagnosticDataEnum.DIAGNOSTIC_EMAIL, true, false);
		dialog.show();
	}

	private void onClickMenuDelete() {
		// Prompt user to delete Database and/or ErrorLog files
		DiagnosticsDialog dialog = new DiagnosticsDialog(this, DiagnosticDataEnum.DIAGNOSTIC_DELETE, true, false);
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dlg) {
				if (!TextUtils.isEmpty(dialog.getFilesDeleted())) {
					mBackupButton.setEnabled(false);
					mEmailButton.setEnabled(false);
					mDeleteButton.setEnabled(false);

					Snackbar.make(mCoordinatorLayout, dialog.getFilesDeleted() + " " + getText(R.string.delete_successful), Snackbar.LENGTH_LONG)
							.setAction("Action", null).show();
				}
			}
		});
		dialog.show();
	}

	/**
	 * Successful Async fetch of backup file list.
	 */
	private void getBackupListComplete(final List<DatabaseBackupModel> backupList) {
		mProgressContainer.setVisibility(View.GONE);

		if (backupList.size() == 0) {
			mNoRecords.setText(this.getText(R.string.no_backup_files_exist));
			mNoRecords.setVisibility(View.VISIBLE);
		}

		BackupListActivity self = this;

		mAdapter = new BackupListAdapter(backupList, new BackupListAdapter.IBackupListAdapterListener() {
			@Override
			public void deleteButtonOnClick(final int position) {

				DatabaseBackupModel model = ((BackupListAdapter) mRecyclerView.getAdapter()).getItem(position);
				String message = String.format(getString(R.string.prompt_delete_backup), model.getName());

				AlertDialog.Builder alert = new AlertDialog.Builder(BackupListActivity.this);
				alert.setMessage(message);
				alert.setPositiveButton(R.string.delete, (dialog, which) -> {
					mProgressDialog = ProgressDialog.show(BackupListActivity.this, "", getString(R.string.progress_deleting));
					mProgressDialog.show();

					mViewModel.deleteBackupDatabase(model.getPath(), position)
						.subscribeOn(Schedulers.newThread())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(self::deleteBackupDatabaseComplete, self::handleError);
				});
				alert.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
				alert.show();
			}

			@Override
			public void restoreButtonOnClick(final int position) {
				DatabaseBackupModel model = ((BackupListAdapter) mRecyclerView.getAdapter()).getItem(position);
				String message = String.format(getString(R.string.prompt_restore_backup), model.getName());

				AlertDialog.Builder alert = new AlertDialog.Builder(BackupListActivity.this);
				alert.setTitle(getString(R.string.confirm_restore));
				alert.setMessage(message);
				alert.setPositiveButton(R.string.restore, (dialog, which) -> {
					mProgressDialog = ProgressDialog.show(BackupListActivity.this, "", getString(R.string.progress_restoring));
					mProgressDialog.show();

					mViewModel.restoreBackupDatabase(model.getPath())
						.subscribeOn(Schedulers.newThread())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(self::restoreBackupDatabaseComplete, self::handleError);
				});
				alert.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
				alert.show();
			}
		});

		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setVisibility(View.VISIBLE);
	}

	/**
	 * Handle generic Throwable error.
	 */
	private void handleError(@NonNull final Throwable e) {
		mProgressContainer.setVisibility(View.GONE);

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}

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

	/**
	 * Successful Async backup KMB database.
	 */
	private void backupDatabaseComplete(DatabaseBackupModel model) {
		mProgressDialog.dismiss();

		mNoRecords.setVisibility(View.GONE);
		mProgressContainer.setVisibility(View.VISIBLE);

		Snackbar.make(mCoordinatorLayout, getText(R.string.backup_successful), Snackbar.LENGTH_LONG)
				.setAction("Action", null).show();

		mViewModel.getBackupList()
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::getBackupListComplete, this::handleError);
	}

	/**
	 * Successful Async delete of a backup file.
	 */
	private void deleteBackupDatabaseComplete(Integer position) {
		mProgressDialog.dismiss();

		if (position > -1) {
			mAdapter.removeItem(position);
			mAdapter.notifyDataSetChanged();

			if (mAdapter.getItemCount() == 0) {
				mNoRecords.setText(this.getText(R.string.no_backup_files_exist));
				mNoRecords.setVisibility(View.VISIBLE);
			}

			Snackbar.make(mCoordinatorLayout, getText(R.string.delete_successful), Snackbar.LENGTH_LONG)
					.setAction("Action", null).show();
		}
	}

	/**
	 * Successful Async restore of a backup file.
	 */
	private void restoreBackupDatabaseComplete(Integer result) {
		mProgressDialog.dismiss();

		Snackbar.make(mCoordinatorLayout, getText(R.string.restore_successful), Snackbar.LENGTH_LONG)
				.setAction("Action", null).show();
	}
}