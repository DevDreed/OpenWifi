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
package com.dustinmreed.openwifi;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dustinmreed.openwifi.data.WifiLocationContract.WiFiLocationEntry;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    static final int COL_ID = 0;
    static final int COL_WIFILOCATION_NAME = 1;
    static final int COL_WIFILOCATION_TYPE = 2;
    static final int COL_WIFILOCATION_STREET_ADDRESS = 3;
    static final int COL_WIFILOCATION_LAT = 4;
    static final int COL_WIFILOCATION_LONG = 5;
    static final int COL_WIFILOCATION_ADDRESS = 6;
    static final int COL_WIFILOCATION_CITY = 7;
    static final int COL_WIFILOCATION_STATE = 8;
    static final int COL_WIFILOCATION_ZIPCODE = 9;
    static final String DETAIL_URI = "URI";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #OpenWiFIApp";
    private static final int DETAIL_LOADER = 0;
    private static final String[] DETAIL_COLUMNS = {
            WiFiLocationEntry._ID,
            WiFiLocationEntry.COLUMN_SITE_NAME,
            WiFiLocationEntry.COLUMN_SITE_TYPE,
            WiFiLocationEntry.COLUMN_STREET_ADDRESS,
            WiFiLocationEntry.COLUMN_COORD_LAT,
            WiFiLocationEntry.COLUMN_COORD_LONG,
            WiFiLocationEntry.COLUMN_ADDRESS,
            WiFiLocationEntry.COLUMN_CITY,
            WiFiLocationEntry.COLUMN_STATE,
            WiFiLocationEntry.COLUMN_ZIPCODE,
    };
    private ShareActionProvider mShareActionProvider;
    private String mWiFiLocation;
    private Uri mUri;
    private TextView mSiteNameView;
    private TextView mSiteAddressView;
    private TextView mSiteCityView;
    private TextView mSiteStateView;
    private TextView mSiteZipcodeView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mSiteNameView = (TextView) rootView.findViewById(R.id.detail_name_textview);
        mSiteAddressView = (TextView) rootView.findViewById(R.id.detail_address_textview);
        mSiteCityView = (TextView) rootView.findViewById(R.id.detail_city_textview);
        mSiteStateView = (TextView) rootView.findViewById(R.id.detail_state_textview);
        mSiteZipcodeView = (TextView) rootView.findViewById(R.id.detail_zipcode_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mWiFiLocation != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mWiFiLocation + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            String siteName = data.getString(COL_WIFILOCATION_NAME);
            mSiteNameView.setText(siteName);
            String siteAddress = data.getString(COL_WIFILOCATION_ADDRESS);
            mSiteAddressView.setText(siteAddress);
            String siteCity = data.getString(COL_WIFILOCATION_CITY);
            mSiteCityView.setText(siteCity);
            String siteState = data.getString(COL_WIFILOCATION_STATE);
            mSiteStateView.setText(siteState);
            String siteZipcode = data.getString(COL_WIFILOCATION_ZIPCODE);
            mSiteZipcodeView.setText(siteZipcode);

            // We still need this for the share intent
            mWiFiLocation = String.format("%s", siteName);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}