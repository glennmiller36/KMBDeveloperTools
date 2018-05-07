package com.jjkeller.kmb.developertools.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.dialog.DiagnosticsDialog;
import com.jjkeller.kmb.developertools.enumerator.DiagnosticDataEnum;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.viewmodel.MainViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity to display launcher links to tasks.
 */

public class MainActivity extends AppCompatActivity {
    public static final int STARTACTIVITY_SETTINGS = 1;

    private MainViewModel mViewModel = new MainViewModel();

    private CoordinatorLayout mCoordinatorLayout;

    private TextView mStatusDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Services.Theme().getThemeResourceId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();

        // first time launching the app, show the App Intro
        if (Services.Preferences().getShowAppIntroValue()) {
            Services.Preferences().setShowAppIntroValue(false);
            startActivity(new Intent(this, IntroActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), STARTACTIVITY_SETTINGS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();

        updateDatabaseStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == STARTACTIVITY_SETTINGS) {
            // Make sure the request was successful
            //if (resultCode == RESULT_OK) {

                // re-create the Activity to reflect the changed Theme
                setTheme(Services.Theme().getThemeResourceId());
                recreate();
            //}
        }
    }

    /**
     * Define handles to the child views.
     */
    private void setupViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        LinearLayout buttonExecuteSql = (LinearLayout) findViewById(R.id.buttonExecuteSql);
        buttonExecuteSql.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SqlQueryActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout buttonDatabaseManagement = (LinearLayout) findViewById(R.id.buttonDatabaseManagement);
        buttonDatabaseManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BackupListActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout buttonViewErrorLog = (LinearLayout) findViewById(R.id.buttonViewErrorLog);
        buttonViewErrorLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ErrorLogActivity.class);
                startActivity(intent);
            }
        });

        MainActivity self = this;

        LinearLayout buttonEmailDiagnostics = (LinearLayout) findViewById(R.id.buttonEmailDiagnostics);
        buttonEmailDiagnostics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prompt user to email attach Database and/or ErrorLog
                DiagnosticsDialog dialog = new DiagnosticsDialog(self, DiagnosticDataEnum.DIAGNOSTIC_EMAIL, true, true);
                dialog.show();
            }
        });

        LinearLayout buttonDeleteDiagnostics = (LinearLayout) findViewById(R.id.buttonDeleteDiagnostics);
        buttonDeleteDiagnostics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prompt user to delete Database and/or ErrorLog files
                DiagnosticsDialog dialog = new DiagnosticsDialog(self, DiagnosticDataEnum.DIAGNOSTIC_DELETE, true, true);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dlg) {
                        if (!TextUtils.isEmpty(dialog.getFilesDeleted())) {
                            // update database status
                            updateDatabaseStatus();

                            Snackbar.make(mCoordinatorLayout, dialog.getFilesDeleted() + " " + getText(R.string.delete_successful), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }
                });
                dialog.show();
            }
        });

        LinearLayout buttonBluetooth = (LinearLayout) findViewById(R.id.buttonBluetooth);
        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BluetoothPairedActivity.class);
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // keep the new Activity from being added to the history stack
                startActivity(intent);
            }
        });

        mStatusDescription = (TextView) findViewById(R.id.txtStatusDescription);
    }

    private void updateDatabaseStatus() {
        mStatusDescription.setText(getString(R.string.checking_status));

        subscribeToViewModel();
    }

    /**
     * Bind the View and ViewModel.
     */
    private void subscribeToViewModel() {
        mViewModel.getDatabaseVersion()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::getDatabaseVersionComplete, this::handleError);
    }

    /**
     * Successful Async fetch database version.
     */
    private void getDatabaseVersionComplete(final String results) {
        if (TextUtils.isEmpty(results)) {
            mStatusDescription.setTextColor(ContextCompat.getColor(this, R.color.colorErrorRed));

            if (TextUtils.isEmpty(Services.Database().getKmbDatabasePath())) {
                mStatusDescription.setText(getString(R.string.no_kmb_database));
            }
            else {
                mStatusDescription.setText(getString(R.string.kmb_database_notcompatible));
            }
        }
        else {
            mStatusDescription.setTextColor(Services.Theme().getThemeAttribute(getTheme(), android.R.attr.divider));
            mStatusDescription.setText(getString(R.string.kmb_database_version) + " " + results);
        }
    }

    /**
     * Handle generic Throwable error.
     */
    private void handleError(@NonNull final Throwable e) {
        mStatusDescription.setText(e.getMessage());
    }
}
