package com.jjkeller.kmb.developertools.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.enumerator.DiagnosticDataEnum;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.viewmodel.DiagnosticsViewModel;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Re-usable dialog to prompt the user to Email or Delete the KMB Database and kmberrlog.txt file.
 */

public class DiagnosticsDialog extends Dialog implements
		android.view.View.OnClickListener, CompoundButton.OnCheckedChangeListener {
	private DiagnosticsViewModel mViewModel = new DiagnosticsViewModel();

	private TextView mTitle;
	private Button mPositiveButton;
	private Button mNegativeButton;
	private DiagnosticDataEnum mDiagnosticDataEnum;
	private CheckBox mCheckDatabase;
	private CheckBox mCheckErrorLog;
	private LinearLayout mProgressBarContainer;
	private ProgressBar mProgressBar;

	private boolean mDatabaseInitialCheckState;
	private boolean mErrorLogInitialCheckState;
	private String mFilesDeleted;

	public String getFilesDeleted() { return mFilesDeleted; }

	public DiagnosticsDialog(Activity activity, DiagnosticDataEnum diagnosticDataEnum, boolean databaseChecked, boolean errorLogChecked) {
		super(activity, Services.Theme().getDialogThemeResourceId());

		mDatabaseInitialCheckState = databaseChecked;
		mErrorLogInitialCheckState = errorLogChecked;
		mDiagnosticDataEnum = diagnosticDataEnum;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_diagnostics);

		mTitle = (TextView) findViewById(R.id.textTitle);
		mPositiveButton = (Button) findViewById(R.id.buttonPositive);
		mNegativeButton = (Button) findViewById(R.id.buttonNegative);
		mCheckDatabase = (CheckBox) findViewById(R.id.checkDatabase);
		mCheckErrorLog = (CheckBox) findViewById(R.id.checkErrorLog);

		mPositiveButton.setOnClickListener(this);
		mNegativeButton.setOnClickListener(this);

		mCheckDatabase.setChecked(mDatabaseInitialCheckState);
		mCheckErrorLog.setChecked(mErrorLogInitialCheckState);
		mCheckDatabase.setOnCheckedChangeListener(this);
		mCheckErrorLog.setOnCheckedChangeListener(this);

		switch (mDiagnosticDataEnum) {
			case DIAGNOSTIC_DELETE:
				mTitle.setText(getContext().getString(R.string.delete_files));
				mPositiveButton.setText(getContext().getString(R.string.delete));
				break;
			case DIAGNOSTIC_EMAIL:
				mTitle.setText(getContext().getString(R.string.attach_files));
				mPositiveButton.setText(getContext().getString(R.string.email));
				break;
		}

		mProgressBarContainer = (LinearLayout) findViewById((R.id.progressBarContainer));
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

		// change the spinner color to match the primary theme color
		mProgressBar.getIndeterminateDrawable().setColorFilter(Services.Theme().getThemeAttribute(getContext().getTheme(), R.attr.colorAccent), PorterDuff.Mode.SRC_IN);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mPositiveButton.setEnabled(mCheckDatabase.isChecked() || mCheckErrorLog.isChecked());
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.buttonPositive:

				// disable the ability to dismiss the dialog until process is complete
				configureDialogCancelable(false);

				switch (mDiagnosticDataEnum) {
					case DIAGNOSTIC_DELETE:
						mViewModel.deleteDiagnosticFiles(mCheckDatabase.isChecked(), mCheckErrorLog.isChecked())
								.subscribeOn(Schedulers.newThread())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(this::deleteDiagnosticFileComplete, this::handleError);
						break;
					case DIAGNOSTIC_EMAIL:
						// You must copy the file to the external directory (aka SD Card). It's because the email application cannot access your data directory
						// (in the same way that you can't access other app's data directory)
						mViewModel.copyDiagnosticFilesToTempDirectory(mCheckDatabase.isChecked(), mCheckErrorLog.isChecked())
								.subscribeOn(Schedulers.newThread())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(this::copyDiagnosticFilesToTempDirectoryComplete, this::handleError);
						break;
				}
				break;

			case R.id.buttonNegative:
				cancel();
				break;

			default:
				break;
		}
	}

	private void configureDialogCancelable(boolean cancelable) {
		if (cancelable) {
			this.setCancelable(true);
			this.setCanceledOnTouchOutside(true);
			mNegativeButton.setVisibility(View.VISIBLE);
			mProgressBarContainer.setVisibility(View.GONE);
			mPositiveButton.setVisibility(View.VISIBLE);
		}
		else {
			this.setCancelable(false);
			this.setCanceledOnTouchOutside(false);
			mNegativeButton.setVisibility(View.INVISIBLE);
			mPositiveButton.setVisibility(View.GONE);
			mProgressBarContainer.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Successful Async Delete of the KMB Database and/or kmberrlog.txt files.
	 */
	private void deleteDiagnosticFileComplete(final Boolean results) {
		mFilesDeleted = "";

		if (mCheckDatabase.isChecked()) {
			mFilesDeleted = KmbApplication.getContext().getString(R.string.kmb_database);
		}

		if (mCheckErrorLog.isChecked()) {

			if (TextUtils.isEmpty(mFilesDeleted)) {
				mFilesDeleted = KmbApplication.getContext().getString(R.string.kmb_error_log);
			}
			else {
				mFilesDeleted += " " + KmbApplication.getContext().getString(R.string.and) + " " + KmbApplication.getContext().getString(R.string.kmb_error_log);
			}
		}

		this.dismiss();
	}

	/**
	 * Successful Async Copy of the KMB Database and/or kmberrlog.txt files to the Temp directory.
	 * Email KMB Database and/or KMB Error Log as attachments
	 */
	private void copyDiagnosticFilesToTempDirectoryComplete(final ArrayList<Uri> results) {

		// need to "send multiple" to get more than one attachment
		final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getContext().getString(R.string.kmb_diagnostic_files));
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Services.Device().getDeviceInfo());
		emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		if (!results.isEmpty()) {
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, results);
		}

		getContext().startActivity(Intent.createChooser(emailIntent, getContext().getString(R.string.send_via)));

		this.dismiss();
	}

	/**
	 * Handle generic Throwable error.
	 */
	private void handleError(@NonNull final Throwable e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getContext().getString(R.string.error).toUpperCase())
				.setMessage(e.getMessage())
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						configureDialogCancelable(true);
						dialog.dismiss();
					}
				})
				.show();
	}
}
