package com.dustinmreed.openwifi;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivityAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;

    public MainActivityAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = R.layout.list_item_wifi_location;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String siteName = cursor.getString(MainActivityFragment.COL_WIFILOCATION_NAME);
        String siteType = cursor.getString(MainActivityFragment.COL_WIFILOCATION_TYPE);
        switch (siteType) {
            case "Library":
                viewHolder.typeImageView.setImageResource(R.drawable.ic_local_library_grey600_36dp);
                viewHolder.typeImageView.setColorFilter(Color.parseColor("#71B2F3"));
                break;
            case "Regional Community Center":
                viewHolder.typeImageView.setImageResource(R.drawable.ic_public_grey600_36dp);
                viewHolder.typeImageView.setColorFilter(Color.parseColor("#E9A25C"));
                break;
            case "Public Gathering":
                viewHolder.typeImageView.setImageResource(R.drawable.ic_location_city_grey600_36dp);
                viewHolder.typeImageView.setColorFilter(Color.parseColor("#EBEB5E"));
                break;
        }

        viewHolder.nameTextView.setText(siteName);
        viewHolder.typeTextView.setText(siteType);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public static class ViewHolder {
        private final TextView nameTextView;
        private final TextView typeTextView;
        private final ImageView typeImageView;

        public ViewHolder(View view) {
            typeImageView = (ImageView) view.findViewById(R.id.list_location_type);
            nameTextView = (TextView) view.findViewById(R.id.list_item_wifi_name_textview);
            typeTextView = (TextView) view.findViewById(R.id.list_item_wifi_type_textview);
        }
    }
}