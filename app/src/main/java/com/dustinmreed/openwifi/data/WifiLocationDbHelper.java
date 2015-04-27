package com.dustinmreed.openwifi.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dustinmreed.openwifi.data.WifiLocationContract.WiFiLocationEntry;

/**
 * Manages a local database for weather data.
 */
public class WifiLocationDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "wifilocation.db";
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public WifiLocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WiFiLocationEntry.TABLE_NAME + " (" +
                WiFiLocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WiFiLocationEntry.COLUMN_SITE_NAME + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_SITE_TYPE + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_STREET_ADDRESS + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_COORD_LAT + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_COORD_LONG + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_CITY + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_STATE + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_ZIPCODE + " TEXT NOT NULL, " +

                " UNIQUE (" + WiFiLocationEntry.COLUMN_SITE_NAME +
                ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WiFiLocationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
