package com.jjkeller.kmb.developertools.manager;

import android.content.res.Resources;
import android.view.Menu;

/**
 * This class provides access to Theme settings.
 */

public interface IThemeManager {
	String getActiveThemeName();
	String getActiveAccentName();
	int getThemeResourceId();
	int getDialogThemeResourceId();
	int getThemeAttribute(Resources.Theme theme, int attribute);
	void setMenuIconColor(Menu menu, int color);
}
