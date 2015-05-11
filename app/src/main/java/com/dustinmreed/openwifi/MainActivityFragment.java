package com.dustinmreed.openwifi;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dustinmreed.openwifi.data.WifiLocationContract;

import static com.dustinmreed.openwifi.Utilities.readFromPreferences;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final int COL_WIFILOCATION_NAME = 1;
    static final int COL_WIFILOCATION_TYPE = 2;

    private static final String SELECTED_KEY = "selected_position";
    private static final int WIFILOCATION_LOADER = 0;
    private static final String[] WIFILOCATION_COLUMNS = {
            WifiLocationContract.WiFiLocationEntry.TABLE_NAME + "." + WifiLocationContract.WiFiLocationEntry._ID,
            WifiLocationContract.WiFiLocationEntry.COLUMN_SITE_NAME,
            WifiLocationContract.WiFiLocationEntry.COLUMN_SITE_TYPE,
            WifiLocationContract.WiFiLocationEntry.COLUMN_STREET_ADDRESS,
            WifiLocationContract.WiFiLocationEntry.COLUMN_COORD_LAT,
            WifiLocationContract.WiFiLocationEntry.COLUMN_COORD_LONG
    };
    private static final String KEY_MAIN_LISTVIEW_FILTER = "main_listview_filter";
    private MainActivityAdapter mWiFiLocationAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mWiFiLocationAdapter = new MainActivityAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_wifi_location);
        mListView.setAdapter(mWiFiLocationAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(WifiLocationContract.WiFiLocationEntry.buildWiFiLocationWithName(cursor.getString(COL_WIFILOCATION_NAME)
                            ));
                }
                mPosition = position;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(WIFILOCATION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String mMainListviewFilter = readFromPreferences(getActivity(), KEY_MAIN_LISTVIEW_FILTER, "all");
        Uri wifiForLocationUri;

        switch (mMainListviewFilter) {
            case "all":
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocation();
                break;
            case "library":
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocationsWithType("type", "Library");
                break;
            case "communitycenter":
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocationsWithType("type", "Regional Community Center");
                break;
            case "publicgathering":
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocationsWithType("type", "Public Gathering");
                break;
            default:
                wifiForLocationUri = WifiLocationContract.WiFiLocationEntry.buildWiFiLocation();
                break;
        }

        return new CursorLoader(getActivity(),
                wifiForLocationUri,
                WIFILOCATION_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mWiFiLocationAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWiFiLocationAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        boolean mUseTodayLayout = useTodayLayout;
    }

    public interface Callback {
        void onItemSelected(Uri locationUri);
    }
}