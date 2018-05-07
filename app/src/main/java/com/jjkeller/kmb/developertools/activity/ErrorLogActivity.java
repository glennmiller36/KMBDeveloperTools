package com.jjkeller.kmb.developertools.activity;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.dialog.DiagnosticsDialog;
import com.jjkeller.kmb.developertools.dialog.EditTextDialog;
import com.jjkeller.kmb.developertools.enumerator.DiagnosticDataEnum;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.viewmodel.ErrorLogViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity to display the content of the kmberrlog.txt file.
 */

public class ErrorLogActivity extends AppCompatActivity {
	private ErrorLogViewModel mViewModel = new ErrorLogViewModel();

	private CoordinatorLayout mCoordinatorLayout;

	private MenuItem mEmailButton;
	private MenuItem mFindButton;
	private MenuItem mDeleteButton;

	private ScrollView mScrollView;
	private TextView mErrorLogContent;

	private TextView mNoRecords;
	private LinearLayout mProgressContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Services.Theme().getThemeResourceId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_errorlog);

		setupViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_errorlog, menu);

		mEmailButton = (MenuItem) menu.findItem(R.id.menuEmail);
		mFindButton = (MenuItem) menu.findItem(R.id.menuFind);
		mDeleteButton = (MenuItem) menu.findItem(R.id.menuDelete);

		subscribeToViewModel();

		// set icon color of menu
		Services.Theme().setMenuIconColor(menu, Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent));

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		boolean canReadErrorLogFile = Services.File().canReadFile(Services.File().getErrorLogPath());

		menu.getItem(1).setEnabled(canReadErrorLogFile);	// Email
		menu.getItem(2).setEnabled(canReadErrorLogFile);	// Find...
		menu.getItem(3).setEnabled(canReadErrorLogFile);	// Delete
		menu.getItem(4).setEnabled(canReadErrorLogFile);	// Scroll to Top
		menu.getItem(5).setEnabled(canReadErrorLogFile);	// Scroll to Bottom

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menuRefresh:
				onClickMenuRefresh();
				return true;
			case R.id.menuEmail:
				onClickMenuEmail();
				return true;
			case R.id.menuFind:
				onClickMenuFindTextInTextView();
				return true;
			case R.id.menuDelete:
				onClickMenuDelete();
				return true;
			case R.id.menuScrollTop:
				onClickMenuScrollToTop();
				return true;
			case R.id.menuScrollBottom:
				onClickMenuScrollToBottom();
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
		mProgressContainer = (LinearLayout) findViewById(R.id.llProgressContainer);
		mScrollView = (ScrollView) findViewById(R.id.hscrll1);
		mErrorLogContent = (TextView) findViewById(R.id.errorLogContent);
		mErrorLogContent.setBackgroundColor(Services.Theme().getThemeAttribute(getTheme(), android.R.attr.background));

		// change the spinner color to match the primary theme color
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.getIndeterminateDrawable()
				.setColorFilter(Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent), PorterDuff.Mode.SRC_IN);

		mNoRecords = (TextView) findViewById(R.id.textNoRecords);
	}

	/**
	 * Bind the View and ViewModel.
	 */
	private void subscribeToViewModel() {
		mViewModel.readErrorLog()
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::readErrorLogComplete, this::handleError);
	}

	/**
	 * Successful Async fetch of kmberrlog.txt file.
	 */
	private void readErrorLogComplete(final String results) {
		if (TextUtils.isEmpty(results)) {
			mErrorLogContent.setVisibility(View.GONE);

			mNoRecords.setText(this.getText(R.string.no_errorlog_files_exist));
			mNoRecords.setVisibility(View.VISIBLE);
			mProgressContainer.setVisibility(View.GONE);

			mEmailButton.setEnabled(false);
			mFindButton.setEnabled(false);
			mDeleteButton.setEnabled(false);
		}
		else {
			mScrollView.fullScroll(View.FOCUS_UP);
			mErrorLogContent.setText(results);

			mProgressContainer.setVisibility(View.GONE);
			mErrorLogContent.setVisibility(View.VISIBLE);

			mEmailButton.setEnabled(true);
			mFindButton.setEnabled(true);
			mDeleteButton.setEnabled(true);
		}
	}

	/**
	 * Handle generic Throwable error.
	 */
	private void handleError(@NonNull final Throwable e) {
		mNoRecords.setText(e.getMessage());
		mNoRecords.setVisibility(View.VISIBLE);
		mProgressContainer.setVisibility(View.GONE);
	}

	private void onClickMenuRefresh() {
		mErrorLogContent.setVisibility(View.GONE);
		mErrorLogContent.setText("");

		mProgressContainer.setVisibility(View.VISIBLE);
		mNoRecords.setVisibility(View.GONE);

		subscribeToViewModel();
	}

	private void onClickMenuEmail() {
		// Prompt user to email attach Database and/or ErrorLog
		DiagnosticsDialog dialog = new DiagnosticsDialog(this, DiagnosticDataEnum.DIAGNOSTIC_EMAIL, false, true);
		dialog.show();
	}

	private void onClickMenuDelete() {
		// Prompt user to delete Database and/or ErrorLog files
		DiagnosticsDialog dialog = new DiagnosticsDialog(this, DiagnosticDataEnum.DIAGNOSTIC_DELETE, false, true);
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dlg) {
				if (!TextUtils.isEmpty(dialog.getFilesDeleted())) {
					mErrorLogContent.setVisibility(View.GONE);
					mErrorLogContent.setText("");

					mEmailButton.setEnabled(false);
					mFindButton.setEnabled(false);
					mDeleteButton.setEnabled(false);

					Snackbar.make(mCoordinatorLayout, dialog.getFilesDeleted() + " " + getText(R.string.delete_successful), Snackbar.LENGTH_LONG)
							.setAction("Action", null).show();

					readErrorLogComplete("");
				}
			}
		});
		dialog.show();
	}

	/**
	 * Search the ErrorLog content for the requested search terms and if found, scroll the TextView to that position.
	 */
	private void onClickMenuFindTextInTextView() {
		// Prompt user for backup file name
		EditTextDialog dialog = new EditTextDialog(this, getString(R.string.find_text), null, getString(R.string.search));
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dlg) {
				String criteria = dialog.getTextInput();
				if (!TextUtils.isEmpty(criteria)) {
					String fullText = mErrorLogContent.getText().toString();
					if (fullText.contains(criteria)) {
						int indexOfCriteria = fullText.indexOf(criteria);
						int lineNumber = mErrorLogContent.getLayout().getLineForOffset(indexOfCriteria);

						mScrollView.scrollTo(0, mErrorLogContent.getLayout().getLineTop(lineNumber));
					}
				}
			}
		});
		dialog.show();
	}

	/**
	 * Scroll the ErrorLog content to the top.
	 */
	private void onClickMenuScrollToTop() {
		mScrollView.scrollTo(0, 0);
	}

	/**
	 * Scroll the ErrorLog content to the bottom.
	 */
	private void onClickMenuScrollToBottom() {
		int lineNumber = mErrorLogContent.getLayout().getLineForOffset(mErrorLogContent.getText().length());
		mScrollView.scrollTo(0, mErrorLogContent.getLayout().getLineTop(lineNumber));
	}


	/*----------------------------------------
	 * Helper methods
	 *----------------------------------------*/

}
