package com.jjkeller.kmb.developertools.manager;

import android.content.SharedPreferences;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;

/**
 * This class provides access to App settings.
 */

public class PreferencesManager implements IPreferencesManager {

    public static final String PREFS_NAME = "KmbDevPrefs";

    private static final String PREFS_KEY_LASTEXECUTEDSQLQUERY = "lastExecutedSqlQuery";
    private static final String PREFS_KEY_TERMINALCOMMANDSFAVORITES = "terminalCommandsFavorites";
    private static final String PREFS_KEY_ACTIVETHEME = "activeTheme";
    private static final String PREFS_KEY_ACCENTCOLOR = "accentColor";
    private static final String PREFS_KEY_APPINTRO = "appIntro";

    private static final String DEFAULT_THEME_LIGHT = "2";
    private static final String DEFAULT_ACCENTCOLOR_BLUE = "7";
    private static final Boolean DEFAULT_SHOW_APPINTRO = true;

    public String getLastExecutedSqlQuery() {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(PREFS_KEY_LASTEXECUTEDSQLQUERY, KmbApplication.getContext().getString(R.string.default_query));
    }

    public void setLastExecutedSqlQuery(String query) {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_KEY_LASTEXECUTEDSQLQUERY, query.trim());

        // Commit the edits!
        editor.commit();
    }

    public String getTerminalCommandsFavorites() {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(PREFS_KEY_TERMINALCOMMANDSFAVORITES, "");
    }

    public void setTerminalCommandsFavorites(String commaSeparatedString) {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_KEY_TERMINALCOMMANDSFAVORITES, commaSeparatedString.trim());

        // Commit the edits!
        editor.commit();
    }

    public String getActiveThemeValue() {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(PREFS_KEY_ACTIVETHEME, DEFAULT_THEME_LIGHT);
    }

    public void setActiveThemeValue(String value) {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_KEY_ACTIVETHEME, value.trim());

        // Commit the edits!
        editor.commit();
    }

    public String getActiveAccentValue() {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(PREFS_KEY_ACCENTCOLOR, DEFAULT_ACCENTCOLOR_BLUE);
    }

    public void setActiveAccentValue(String value) {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_KEY_ACCENTCOLOR, value.trim());

        // Commit the edits!
        editor.commit();
    }

    public Boolean getShowAppIntroValue() {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(PREFS_KEY_APPINTRO, DEFAULT_SHOW_APPINTRO);
    }

    public void setShowAppIntroValue(Boolean value) {
        SharedPreferences settings = KmbApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREFS_KEY_APPINTRO, value);

        // Commit the edits!
        editor.commit();
    }

}
