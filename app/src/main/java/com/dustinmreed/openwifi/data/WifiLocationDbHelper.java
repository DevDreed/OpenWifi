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
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                WiFiLocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WiFiLocationEntry.COLUMN_SITE_NAME + " INTEGER NOT NULL, " +
                WiFiLocationEntry.COLUMN_SITE_TYPE + " TEXT NOT NULL, " +
                WiFiLocationEntry.COLUMN_STREET_ADDRESS + " REAL NOT NULL, " +
                WiFiLocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                WiFiLocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                WiFiLocationEntry.COLUMN_SITE_TYPE + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WiFiLocationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
