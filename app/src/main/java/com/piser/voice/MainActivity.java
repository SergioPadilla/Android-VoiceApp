package com.piser.voice;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button events;
    private Button create_event;
    private ListView events_list;
    private EventsAdapter adapter;

    private List<CalendarEvent> calendarEvents;

    // The indices for the projection
    private static final int PROJECTION_CALENDAR_ID_INDEX = 0;
    private static final int PROJECTION_ORGANIZER_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;
    private static final int PROJECTION_DESCRIPTION_INDEX = 3;
    private static final int PROJECTION_DTSTART_INDEX = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarEvents = new ArrayList<>();
        adapter = new EventsAdapter(MainActivity.this);

        events = (Button) findViewById(R.id.button_events);
        create_event = (Button) findViewById(R.id.button_create_events);
        events_list = (ListView) findViewById(R.id.events_list);

        events_list.setAdapter(adapter);
        events.setOnClickListener(getEvents());
        create_event.setOnClickListener(createEvent());
    }

    private View.OnClickListener getEvents() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                // Projection array. Creating indices for this array instead of doing
                // dynamic lookups improves performance.
                String[] EVENT_PROJECTION = new String[]{
                        Events.CALENDAR_ID,                     // 0
                        Events.ORGANIZER,                       // 1
                        Events.TITLE,                           // 2
                        Events.DESCRIPTION,                     // 3
                        Events.DTSTART                          // 4
                };
                // Run query
                Cursor cursor = null;
                ContentResolver cr = getContentResolver();
                Uri uri = Events.CONTENT_URI;
//                String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
//                        + Calendars.ACCOUNT_TYPE + " = ?) AND ("
//                        + Calendars.OWNER_ACCOUNT + " = ?))";
                String selection = "";
                String[] selectionArgs = new String[]{};
                // Submit the query and get a Cursor object back.
                cursor = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

                // Use the cursor to step through the returned records
                if(cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        long calID = cursor.getLong(PROJECTION_CALENDAR_ID_INDEX);
                        String event_title = cursor.getString(PROJECTION_TITLE_INDEX);
                        String dtStart = cursor.getString(PROJECTION_DTSTART_INDEX);

                        calendarEvents.add(new CalendarEvent(calID, event_title, dtStart));
                    }
                }

                adapter.loadEvents(calendarEvents);
            }
        };
    }

    private View.OnClickListener createEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateEvent.class));
            }
        };
    }
}
