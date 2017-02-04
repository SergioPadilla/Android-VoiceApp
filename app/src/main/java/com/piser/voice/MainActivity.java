package com.piser.voice;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SergioPadilla on 31/12/16.
 */

public class MainActivity extends TTSARSActivity {

    private ListView events_list;
    private EventsAdapter adapter;

    private List<CalendarEvent> calendarEvents;

    // The indexes for the projection
    private static final int PROJECTION_CALENDAR_ID_INDEX = 0;
    private static final int PROJECTION_TITLE_INDEX = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        events_list = (ListView) findViewById(R.id.events_list);

        initComponents();
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        calendarEvents = new ArrayList<>();
        adapter = new EventsAdapter(MainActivity.this);
        events_list.setAdapter(adapter);
    }

    private void getEvents() {
        /**
         * Method to get the events of the user and show in the list of this activity
         */
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Projection array. Creating indices for this array
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
        String selection = "";
        String[] selectionArgs = new String[]{};
        // Submit the query and get a Cursor object back.
        cursor = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        // Use the cursor to step through the returned records
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long calID = cursor.getLong(PROJECTION_CALENDAR_ID_INDEX);
                String event_title = cursor.getString(PROJECTION_TITLE_INDEX);

                calendarEvents.add(new CalendarEvent(calID, event_title));
            }
            cursor.close();
        }

        adapter.loadEvents(calendarEvents);
    }

    @Override
    public void writeFeedback(String message) {
        /**
         * This method is util if you can/need show the flow of ASR activity
         */
    }

    @Override
    public void onResults(Bundle results) {
        super.onResults(results);

        if (results != null) {
            ArrayList<String> recognitions = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String result = recognitions.get(0).toLowerCase();

            // Check the result of recognition
            if (Models.CREATE.equals(result)){
                // User said "crear"
                startActivity(new Intent(MainActivity.this, CreateEvent.class));
            }
            else if (Models.LIST.equals(result)) {
                // User said "listar"
                talk(Models.LIST_SUCCESS);
                getEvents();
            }
            else if (Models.HELP.equals(result)) {
                // User said "ayuda"
                talkAndListen(Models.INSTRUCTIONS_MAIN);
            }
            else {
                // Nothing match
                talk(Models.TRYAGAIN);
            }
        }

        stopListening();
    }
}
