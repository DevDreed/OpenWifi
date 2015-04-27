package com.dustinmreed.openwifi;

import android.content.Context;
import android.content.SharedPreferences;

public class Utilities {

    public static final String PREF_FILE_NAME = "sharedprefs";

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    public static String getFormattedAddress(String siteAddress, String siteCity, String siteState, String siteZipcode) {
        return siteAddress + " " + siteCity + ", " + siteState + ' ' + siteZipcode;
    }

}
