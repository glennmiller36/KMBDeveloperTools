package com.jjkeller.kmb.developertools.manager;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Menu;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;

/**
 * This class provides access to Theme settings.
 */

public class ThemeManager implements IThemeManager {

    public String getActiveThemeName() {
        String value = Services.Preferences().getActiveThemeValue();

        String[] themes = KmbApplication.getContext().getResources().getStringArray(R.array.prefThemes);
        int i = Integer.valueOf(value) - 1; // convert 1-relative to 0-relative index
        return themes[i];
    }

    public String getActiveAccentName() {
        String value = Services.Preferences().getActiveAccentValue();

        String[] accents = KmbApplication.getContext().getResources().getStringArray(R.array.prefAccents);
        int i = Integer.valueOf(value) - 1; // convert 1-relative to 0-relative index
        return accents[i];
    }

    public int getThemeResourceId() {
    	String theme = Services.Preferences().getActiveThemeValue();

        switch (Services.Preferences().getActiveAccentValue()) {
            case "2":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Red_Light;
				else
					return R.style.AppTheme_Red_Dark;

			case "3":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Pink_Light;
				else
					return R.style.AppTheme_Pink_Dark;

			case "4":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Purple_Light;
				else
					return R.style.AppTheme_Purple_Dark;

			case "5":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_DeepPurple_Light;
				else
					return R.style.AppTheme_DeepPurple_Dark;

			case "6":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Indigo_Light;
				else
					return R.style.AppTheme_Indigo_Dark;

			case "7":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Blue_Light;
				else
					return R.style.AppTheme_Blue_Dark;

			case "8":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_LightBlue_Light;
				else
					return R.style.AppTheme_LightBlue_Dark;

			case "9":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Cyan_Light;
				else
					return R.style.AppTheme_Cyan_Dark;

			case "10":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Teal_Light;
				else
					return R.style.AppTheme_Teal_Dark;

			case "11":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Green_Light;
				else
					return R.style.AppTheme_Green_Dark;

			case "12":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_LightGreen_Light;
				else
					return R.style.AppTheme_LightGreen_Dark;

			case "13":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Lime_Light;
				else
					return R.style.AppTheme_Lime_Dark;

			case "14":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Yellow_Light;
				else
					return R.style.AppTheme_Yellow_Dark;

			case "15":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Amber_Light;
				else
					return R.style.AppTheme_Amber_Dark;

			case "16":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Orange_Light;
				else
					return R.style.AppTheme_Orange_Dark;

			case "17":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_DeepOrange_Light;
				else
					return R.style.AppTheme_DeepOrange_Dark;

			case "18":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Brown_Light;
				else
					return R.style.AppTheme_Brown_Dark;

			case "19":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_Grey_Light;
				else
					return R.style.AppTheme_Grey_Dark;

			case "20":
				if (theme.equalsIgnoreCase("2"))
					return R.style.AppTheme_BlueGrey_Light;
				else
					return R.style.AppTheme_BlueGrey_Dark;

            default:
            	if (theme.equalsIgnoreCase("2"))
					return R.style.BaseTheme_Light;
				else
                	return R.style.BaseTheme_Dark;
        }
    }

    public int getDialogThemeResourceId() {
		String theme = Services.Preferences().getActiveThemeValue();

		switch (Services.Preferences().getActiveAccentValue()) {
			case "2":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Red_Light;
				else
					return R.style.Dialog_Red_Dark;

			case "3":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Pink_Light;
				else
					return R.style.Dialog_Pink_Dark;

			case "4":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Purple_Light;
				else
					return R.style.Dialog_Purple_Dark;

			case "5":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_DeepPurple_Light;
				else
					return R.style.Dialog_DeepPurple_Dark;

			case "6":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Indigo_Light;
				else
					return R.style.Dialog_Indigo_Dark;

			case "7":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Blue_Light;
				else
					return R.style.Dialog_Blue_Dark;

			case "8":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_LightBlue_Light;
				else
					return R.style.Dialog_LightBlue_Dark;

			case "9":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Cyan_Light;
				else
					return R.style.Dialog_Cyan_Dark;

			case "10":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Teal_Light;
				else
					return R.style.Dialog_Teal_Dark;

			case "11":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Green_Light;
				else
					return R.style.Dialog_Green_Dark;

			case "12":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_LightGreen_Light;
				else
					return R.style.Dialog_LightGreen_Dark;

			case "13":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Lime_Light;
				else
					return R.style.Dialog_Lime_Dark;

			case "14":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Yellow_Light;
				else
					return R.style.Dialog_Yellow_Dark;

			case "15":
				if (theme.equalsIgnoreCase("2"))
					return R.style.Dialog_Amber_Light;
				else
					return R.style.Dialog_Amber_Dark;

			default:
				if (theme.equalsIgnoreCase("2"))
					return R.style.BaseTheme_Dialog_Light;
				else
					return R.style.BaseTheme_Dialog_Dark;
		}
    }

    public int getThemeAttribute(Resources.Theme theme, int attribute) {
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attribute, typedValue, true);
        return typedValue.data;
    }

    public void setMenuIconColor(Menu menu, int color) {
        // set icon color of menu
		for(int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }
}
