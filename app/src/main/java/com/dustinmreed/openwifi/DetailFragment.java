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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dustinmreed.openwifi.data.WifiLocationContract.WiFiLocationEntry;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.support.v4.app.ActivityCompat.invalidateOptionsMenu;
import static com.dustinmreed.openwifi.Utilities.getFormattedAddress;
import static com.dustinmreed.openwifi.Utilities.getLinkFormattedAddress;
import static com.dustinmreed.openwifi.Utilities.replaceSpacesPlusSign;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    static final int COL_WIFILOCATION_NAME = 1;
    static final int COL_WIFILOCATION_TYPE = 2;
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
    MapView mapView;
    GoogleMap map;
    private ShareActionProvider mShareActionProvider;
    private String mWiFiLocation;
    private Uri mUri;
    private TextView mSiteNameView;
    private TextView mSiteAddressView;
    private FloatingActionButton favNavigation;

    private Double latitude;
    private Double longitude;
    private String siteName;
    private String siteZipcode;
    private String siteType;
    private String siteAddress;
    private String siteCity;
    private String siteState;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        invalidateOptionsMenu(getActivity());
        Bundle arguments = getArguments();
        View rootView;
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            mSiteNameView = (TextView) rootView.findViewById(R.id.detail_name_textview);
            mSiteAddressView = (TextView) rootView.findViewById(R.id.detail_address_textview);

            MapsInitializer.initialize(getActivity().getApplicationContext());

            // Gets the MapView from the XML layout and creates it
            mapView = (MapView) rootView.findViewById(R.id.mapview);
            map = mapView.getMap();
            mapView.onCreate(savedInstanceState);

            favNavigation = (FloatingActionButton) rootView.findViewById(R.id.route_nav_icon);
            favNavigation.setSize(FloatingActionButton.SIZE_NORMAL);
            favNavigation.setColorNormalResId(R.color.accentColor);
            favNavigation.setColorPressedResId(R.color.accentColorPressed);
            favNavigation.setIcon(R.drawable.ic_directions_white);
            favNavigation.setStrokeVisible(false);
            ViewCompat.setElevation(favNavigation, 10);
        } else {
            mWiFiLocation = null;
            rootView = inflater.inflate(R.layout.fragment_detail_empty, container, false);
        }


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        if (mWiFiLocation == null) {
            menuItem.setVisible(false);
        }
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, mWiFiLocation + "\n" + siteType + "\n" + replaceSpacesPlusSign(getLinkFormattedAddress(getActivity(), siteAddress, siteCity, siteState, siteZipcode)) + "\n" + FORECAST_SHARE_HASHTAG);
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
            Log.e(LOG_TAG, mUri.toString());
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

            siteName = data.getString(COL_WIFILOCATION_NAME);
            mSiteNameView.setText(siteName);
            siteType = data.getString(COL_WIFILOCATION_TYPE);
            siteAddress = data.getString(COL_WIFILOCATION_ADDRESS);
            siteCity = data.getString(COL_WIFILOCATION_CITY);
            siteState = data.getString(COL_WIFILOCATION_STATE);
            siteZipcode = data.getString(COL_WIFILOCATION_ZIPCODE);
            mSiteAddressView.setText(getFormattedAddress(siteAddress, siteCity, siteState, siteZipcode));

            latitude = Double.valueOf(data.getString(COL_WIFILOCATION_LAT));
            longitude = Double.valueOf(data.getString(COL_WIFILOCATION_LONG));

            favNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("geo:" + latitude + ", " + longitude)
                            .buildUpon()
                            .appendQueryParameter("q", siteAddress + ", " + siteCity)
                            .build();
//                    Uri gmmIntentUri = Uri.parse("google.navigation:q=Taronga+Zoo,+Sydney+Australia");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });


            // Gets to GoogleMap from the MapView and does initialization stuff
            map = mapView.getMap();
            map.getUiSettings().setMyLocationButtonEnabled(false);

            LatLng latlng = new LatLng(latitude, longitude);
            switch (siteType) {
                case "Library":
                    map.addMarker(new MarkerOptions().position(latlng).title(siteName).snippet(getFormattedAddress(siteAddress, siteCity, siteState, siteZipcode)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    break;
                case "Regional Community Center":
                    map.addMarker(new MarkerOptions().position(latlng).title(siteName).snippet(getFormattedAddress(siteAddress, siteCity, siteState, siteZipcode)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    break;
                default:
                    map.addMarker(new MarkerOptions().position(latlng).title(siteName).snippet(getFormattedAddress(siteAddress, siteCity, siteState, siteZipcode)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    break;
            }



            // Updates the location and zoom of the MapView
            //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(36.05590917600006, -86.67243400799998), 20);
            // Move the camera instantly to location with a zoom of 15.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));

            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomTo(14));
            //map.animateCamera(cameraUpdate);

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

    @Override
    public void onResume() {
        checkGooglePlayServicesAvailable();
        if (mapView != null) {
            mapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    private void checkGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        getActivity().finish();
                    }
                });
                dialog.show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }
}