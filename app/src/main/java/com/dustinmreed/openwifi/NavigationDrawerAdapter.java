package com.dustinmreed.openwifi;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import static com.dustinmreed.openwifi.Utilities.readFromPreferences;
import static com.dustinmreed.openwifi.Utilities.saveToPreferences;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {

    private static final String KEY_MAIN_LISTVIEW_FILTER = "main_listview_filter";
    private static final String KEY_NAV_DRAWER_HIGHLIGHT = "nav_drawer_highlight";

    List<Information> data = Collections.emptyList();
    Context context;
    private LayoutInflater inflater;

    public NavigationDrawerAdapter(Context context, List<Information> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.nav_drawer_custom_row, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        Information current = data.get(position);
        viewHolder.title.setText(current.title);
        viewHolder.icon.setImageResource(current.iconId);

        if (readFromPreferences(context, KEY_NAV_DRAWER_HIGHLIGHT, null) != null) {
            if (readFromPreferences(context, KEY_NAV_DRAWER_HIGHLIGHT, null).equalsIgnoreCase(String.valueOf(position))) {
                viewHolder.title.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
                viewHolder.icon.setColorFilter(context.getResources().getColor(R.color.primaryColorDark), PorterDuff.Mode.SRC_IN);
                viewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.grey));
            }
        } else {
            if (position == 0) {
                saveToPreferences(context, KEY_NAV_DRAWER_HIGHLIGHT, "0");
                viewHolder.title.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
                viewHolder.icon.setColorFilter(context.getResources().getColor(R.color.primaryColorDark), PorterDuff.Mode.SRC_IN);
                viewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.grey));
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView icon;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.listText);
            icon = (ImageView) itemView.findViewById(R.id.listIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent;
            switch (getAdapterPosition()) {
                case 0:
                    saveToPreferences(context, KEY_MAIN_LISTVIEW_FILTER, "all");
                    saveToPreferences(context, KEY_NAV_DRAWER_HIGHLIGHT, String.valueOf(getAdapterPosition()));
                    intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                case 1:
                    intent = new Intent(context, MapActivity.class);
                    context.startActivity(intent);
                    break;
                case 2:
                    saveToPreferences(context, KEY_MAIN_LISTVIEW_FILTER, "library");
                    saveToPreferences(context, KEY_NAV_DRAWER_HIGHLIGHT, String.valueOf(getAdapterPosition()));
                    intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                case 3:
                    saveToPreferences(context, KEY_MAIN_LISTVIEW_FILTER, "communitycenter");
                    saveToPreferences(context, KEY_NAV_DRAWER_HIGHLIGHT, String.valueOf(getAdapterPosition()));
                    intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                case 4:
                    saveToPreferences(context, KEY_MAIN_LISTVIEW_FILTER, "publicgathering");
                    saveToPreferences(context, KEY_NAV_DRAWER_HIGHLIGHT, String.valueOf(getAdapterPosition()));
                    intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }
}
