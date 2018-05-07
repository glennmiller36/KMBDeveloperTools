package com.jjkeller.kmb.developertools.manager;

/**
 * This class provides access to App settings.
 */

public interface IPreferencesManager {
    String getLastExecutedSqlQuery();
	void setLastExecutedSqlQuery(String query);

	String getTerminalCommandsFavorites();
	void setTerminalCommandsFavorites(String commaSeparatedString);

	String getActiveThemeValue();
	void setActiveThemeValue(String value);

	String getActiveAccentValue();
	void setActiveAccentValue(String value);

	Boolean getShowAppIntroValue();
	void setShowAppIntroValue(Boolean value);
}
