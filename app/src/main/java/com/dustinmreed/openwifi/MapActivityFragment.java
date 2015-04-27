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

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dustinmreed.openwifi.data.WifiLocationContract;
import com.dustinmreed.openwifi.data.WifiLocationContract.WiFiLocationEntry;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static com.dustinmreed.openwifi.Utilities.getFormattedAddress;

public class MapActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_WIFILOCATION_NAME = 1;
    static final int COL_WIFILOCATION_TYPE = 2;
    static final int COL_WIFILOCATION_LAT = 4;
    static final int COL_WIFILOCATION_LONG = 5;
    static final int COL_WIFILOCATION_ADDRESS = 6;
    static final int COL_WIFILOCATION_CITY = 7;
    static final int COL_WIFILOCATION_STATE = 8;
    static final int COL_WIFILOCATION_ZIPCODE = 9;
    static final String DETAIL_URI = "URI";
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
    private CameraUpdate cu;
    private CameraPosition cp;

    public MapActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        MapsInitializer.initialize(this.getActivity());

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.fullmapview);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocation();
        if (null != wifiForLocationUri) {
            return new CursorLoader(
                    getActivity(),
                    wifiForLocationUri,
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
        List<Marker> markers = new ArrayList<>();
        if (data != null && data.moveToFirst()) {
            data.moveToFirst();
            while (!data.isAfterLast()) {
                String siteName = data.getString(COL_WIFILOCATION_NAME);
                String siteType = data.getString(COL_WIFILOCATION_TYPE);
                String siteAddress = data.getString(COL_WIFILOCATION_ADDRESS);
                String siteCity = data.getString(COL_WIFILOCATION_CITY);
                String siteState = data.getString(COL_WIFILOCATION_STATE);
                String siteZipcode = data.getString(COL_WIFILOCATION_ZIPCODE);
                Double latitude = Double.valueOf(data.getString(COL_WIFILOCATION_LAT));
                Double longitude = Double.valueOf(data.getString(COL_WIFILOCATION_LONG));

                map = mapView.getMap();
                map.getUiSettings().setMyLocationButtonEnabled(false);
                Marker newmarker;

                LatLng latlng = new LatLng(latitude, longitude);
                switch (siteType) {
                    case "Library":
                        newmarker = map.addMarker(new MarkerOptions().position(latlng).title(siteName).snippet(getFormattedAddress(siteAddress, siteCity, siteState, siteZipcode)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        break;
                    case "Regional Community Center":
                        newmarker = map.addMarker(new MarkerOptions().position(latlng).title(siteName).snippet(getFormattedAddress(siteAddress, siteCity, siteState, siteZipcode)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                        break;
                    default:
                        newmarker = map.addMarker(new MarkerOptions().position(latlng).title(siteName).snippet(getFormattedAddress(siteAddress, siteCity, siteState, siteZipcode)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                        break;
                }
                markers.add(newmarker);
                data.moveToNext();
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 100;
            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            try {
                map.moveCamera(cu);
                map.animateCamera(cu);
            } catch (IllegalStateException ise) {

                map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

                    @Override
                    public void onMapLoaded() {
                        map.moveCamera(cu);
                        map.animateCamera(cu);
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (cp != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
            cp = null;
        }
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
        cp = map.getCameraPosition();
        map = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}