package com.jjkeller.kmb.developertools.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjkeller.kmb.developertools.BuildConfig;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.manager.Services;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter.DATETIME_PATTERN;
import static com.jjkeller.kmb.developertools.adapter.SqlQueryAdapter.DATE_PATTERN;

/**
 * A PreferenceActivity that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

	private String mThemeValue;
	private String mAccentValue;

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Services.Theme().getThemeResourceId());

		super.onCreate(savedInstanceState);

		setupActionBar();

		mThemeValue = Services.Preferences().getActiveThemeValue();
		mAccentValue = Services.Preferences().getActiveAccentValue();
	}

	/**
	 * Define handles to the child views.
	 */
	private void setupActionBar() {
		getLayoutInflater().inflate(R.layout.toolbar, (ViewGroup)findViewById(android.R.id.content));
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// to avoid layout being overlapped by the Toolbar - add padding
		LinearLayout ll = (LinearLayout) getListView().getParent().getParent();
		ll.setPadding(ll.getPaddingLeft(), toolbar.getLayoutParams().height, ll.getPaddingRight(), ll.getPaddingBottom());
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onIsMultiPane() { return isXLargeTablet(this); }

	@Override
	protected void onResume() {
		super.onResume();

		if (!mThemeValue.equalsIgnoreCase(Services.Preferences().getActiveThemeValue()) ||
				!mAccentValue.equalsIgnoreCase(Services.Preferences().getActiveAccentValue())) {
			// recreate() worked but you could no longer click on the Header to launch the fragment
			// so decided to finish the activity and start the same one
			finish();
			startActivity(new Intent(this, SettingsActivity.class));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.pref_headers, target);
	}

	/**
	 * This method stops com.jjkeller.kmb.developertools.fragment injection in malicious applications.
	 * Make sure to deny any unknown fragments here.
	 */
	protected boolean isValidFragment(String fragmentName) {
		return PreferenceFragment.class.getName().equals(fragmentName)
				|| AboutPreferenceFragment.class.getName().equals(fragmentName)
				|| DisplayPreferenceFragment.class.getName().equals(fragmentName)
				|| TermsOfServicePreferenceFragment.class.getName().equals(fragmentName);
	}

	/**
	 * This com.jjkeller.kmb.developertools.fragment shows About info regarding the App and Device.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class AboutPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_about);
			setHasOptionsMenu(true);

			EditTextPreference appVersion = (EditTextPreference) findPreference("prefAppVersion");
			appVersion.setSummary(getAppVersionInfo());

			EditTextPreference appBuildDate = (EditTextPreference) findPreference("prefAppBuildDate");
			appBuildDate.setSummary(getAppBuildDate());

			EditTextPreference deviceManufacturer = (EditTextPreference) findPreference("prefDeviceManufacturer");
			deviceManufacturer.setSummary(android.os.Build.MANUFACTURER);

			EditTextPreference deviceModel = (EditTextPreference) findPreference("prefDeviceModel");
			deviceModel.setSummary(Build.MODEL);

			EditTextPreference deviceBrand = (EditTextPreference) findPreference("prefDeviceBrand");
			deviceBrand.setSummary(Build.BRAND);

			EditTextPreference deviceAndroidVersion = (EditTextPreference) findPreference("prefDeviceAndroidVersion");
			deviceAndroidVersion.setSummary(getDeviceAndroidVersion());

			EditTextPreference databasePath = (EditTextPreference) findPreference("prefDatabasePath");
			databasePath.setSummary(getDatabasePath());

			EditTextPreference databaseVersion = (EditTextPreference) findPreference("prefDatabaseVersion");
			databaseVersion.setSummary(getDatabaseVersion());
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				getActivity().finish();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

		/**
		 * Get the current version number and name
		 */
		private String getAppVersionInfo() {
			String versionName = "";
			try {
				PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
				versionName = packageInfo.versionName;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}

			return versionName;
		}

		/**
		 * Get the Gradle build date
		 */
		private String getAppBuildDate() {
			Date buildDate = new Date(BuildConfig.BUILD_TIME);
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN);
			return dateFormat.format(buildDate);
		}

		/**
		 * Get the device Android version and API level
		 */
		private String getDeviceAndroidVersion() {
			int sdk = Build.VERSION.SDK_INT;
			String versionRelease = Build.VERSION.RELEASE;

			return String.format(getString(R.string.androidversionformat), versionRelease, String.valueOf(sdk));
		}

		/**
		 * Get the KMB Database path
		 */
		private String getDatabasePath() {
			String path = Services.Database().getKmbDatabasePath();
			if (TextUtils.isEmpty(path)) {
				path = getString(R.string.no_kmb_database);
			}

			return path;
		}

		/**
		 * Get the KMB Database path
		 */
		private String getDatabaseVersion() {
			try {
				String version = Services.Database().getKmbDatabaseVersion();
				if (TextUtils.isEmpty(version)) {
					return getString(R.string.no_kmb_database);
				}

				Calendar c = Calendar.getInstance();
				c.set(2000, 0, 1);
				c.add(Calendar.DATE, Integer.parseInt(version));

				SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
				return version + " (" + df.format(c.getTime()) + ")";
			}
			catch (SQLiteException ex) {
				return getString(R.string.kmb_database_notcompatible);
			}
		}
	}

	/**
	 * This com.jjkeller.kmb.developertools.fragment shows Display info like Theme.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DisplayPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_display);
			setHasOptionsMenu(true);

			ListPreference theme = (ListPreference) findPreference("prefAppearanceTheme");
			theme.setValue(Services.Preferences().getActiveThemeValue());
			theme.setSummary(Services.Theme().getActiveThemeName());
			theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Services.Preferences().setActiveThemeValue(newValue.toString());
					theme.setValue(newValue.toString());
					preference.setSummary(theme.getEntry());

					// re-create the Activity to reflect the changed Theme
					getActivity().setTheme(Services.Theme().getThemeResourceId());
					getActivity().recreate();

					return true;
				}
			});

			ListPreference accent = (ListPreference) findPreference("prefAppearanceAccent");
			accent.setValue(Services.Preferences().getActiveAccentValue());
			accent.setSummary(Services.Theme().getActiveAccentName());
			accent.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Services.Preferences().setActiveAccentValue(newValue.toString());
					accent.setValue(newValue.toString());
					preference.setSummary(accent.getEntry());

					// re-create the Activity to reflect the changed Theme
					getActivity().setTheme(Services.Theme().getThemeResourceId());
					getActivity().recreate();

					return true;
				}
			});
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				getActivity().finish();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * This com.jjkeller.kmb.developertools.fragment shows Terms of Service agreement.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class TermsOfServicePreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_privacyandterms);
			setHasOptionsMenu(true);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				getActivity().finish();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
}
