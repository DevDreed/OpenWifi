/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dustinmreed.openwifi.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

public class WifiLocationProvider extends ContentProvider {
    static final int WIFILOCATION = 100;
    static final int WIFILOCATION_WITH_NAME = 101;
    static final int WIFILOCATION_WITH_TYPE = 102;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sWiFiLocationByNameSettingQueryBuilder;
    private static final SQLiteQueryBuilder sWiFiLocationByTypeSettingQueryBuilder;

    private static final String sLocationSettingSelection =
            WifiLocationContract.WiFiLocationEntry.TABLE_NAME + "." + WifiLocationContract.WiFiLocationEntry.COLUMN_SITE_NAME + " = ? ";

    private static final String sLocationTypeSelection =
            WifiLocationContract.WiFiLocationEntry.TABLE_NAME + "." + WifiLocationContract.WiFiLocationEntry.COLUMN_SITE_TYPE + " = ? ";

    static {
        sWiFiLocationByNameSettingQueryBuilder = new SQLiteQueryBuilder();

        sWiFiLocationByNameSettingQueryBuilder.setTables(
                WifiLocationContract.WiFiLocationEntry.TABLE_NAME
        );
    }

    static {
        sWiFiLocationByTypeSettingQueryBuilder = new SQLiteQueryBuilder();

        sWiFiLocationByTypeSettingQueryBuilder.setTables(
                WifiLocationContract.WiFiLocationEntry.TABLE_NAME);
    }

    private WifiLocationDbHelper mOpenHelper;

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WifiLocationContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WifiLocationContract.PATH_WIFILOCATION, WIFILOCATION);
        matcher.addURI(authority, WifiLocationContract.PATH_WIFILOCATION + "/*", WIFILOCATION_WITH_NAME);
        matcher.addURI(authority, WifiLocationContract.PATH_WIFILOCATION + "/type" + "/*", WIFILOCATION_WITH_TYPE);
        return matcher;
    }

    private Cursor getWifiLocationByNameSetting(Uri uri, String[] projection, String sortOrder) {
        String nameSetting = WifiLocationContract.WiFiLocationEntry.getNameSettingFromUri(uri);

        String[] selectionArgs;
        String selection;


        selection = sLocationSettingSelection;
        selectionArgs = new String[]{nameSetting};


        return sWiFiLocationByNameSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWifiLocationByTypeSetting(Uri uri, String[] projection, String sortOrder) {
        String typeSetting = WifiLocationContract.WiFiLocationEntry.getTypeFromUri(uri);
        String[] selectionArgs;
        String selection;

        selection = sLocationTypeSelection;
        selectionArgs = new String[]{typeSetting};

        return sWiFiLocationByTypeSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WifiLocationDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WIFILOCATION:
                return WifiLocationContract.WiFiLocationEntry.CONTENT_TYPE;
            case WIFILOCATION_WITH_NAME:
                return WifiLocationContract.WiFiLocationEntry.CONTENT_TYPE;
            case WIFILOCATION_WITH_TYPE:
                return WifiLocationContract.WiFiLocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case WIFILOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WifiLocationContract.WiFiLocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case WIFILOCATION_WITH_NAME: {
                retCursor = getWifiLocationByNameSetting(uri, projection, sortOrder);
                break;
            }
            case WIFILOCATION_WITH_TYPE: {
                retCursor = getWifiLocationByTypeSetting(uri, projection, sortOrder);
                break;
            }
            default:
                retCursor = getWifiLocationByTypeSetting(uri, projection, sortOrder);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WIFILOCATION: {
                long _id = db.insert(WifiLocationContract.WiFiLocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            case WIFILOCATION:
                rowsDeleted = db.delete(
                        WifiLocationContract.WiFiLocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case WIFILOCATION:
                rowsUpdated = db.update(WifiLocationContract.WiFiLocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WIFILOCATION:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(WifiLocationContract.WiFiLocationEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}