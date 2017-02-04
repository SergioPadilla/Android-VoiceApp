package com.piser.voice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by SergioPadilla on 31/12/16.
 */

public class EventsAdapter extends ArrayAdapter<CalendarEvent> {

    /**
     * Adapter to load each item of the list with a event
     */

    List<CalendarEvent> events;
    Activity activity;

    public EventsAdapter(Activity activity) {
        super(activity.getApplicationContext(), 0);
        this.activity = activity;
    }

    public void loadEvents(List<CalendarEvent> events) {
        this.events = events;
        clear();
        addAll(events);
        notifyDataSetChanged();
    }

    @SuppressWarnings("NullableProblems")
    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        ViewHolder holder;
        final CalendarEvent event = events.get(position);

        if(item == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            item = inflater.inflate(R.layout.item_event, null);
            holder = new ViewHolder();
            holder.calendar_id = (TextView) item.findViewById(R.id.calendar_id);
            holder.title = (TextView) item.findViewById(R.id.event_title);
            item.setTag(holder);
        }
        else {
            holder = (ViewHolder)item.getTag();
        }

        holder.title.setText(event.getTitle());
        holder.calendar_id.setText(Long.toString(event.getID()));

        return item;
    }

    private static class ViewHolder {
        TextView title;
        TextView calendar_id;
    }
}
