package com.piser.voice;

import android.content.Intent;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Calendar;

public class CreateEvent extends AppCompatActivity {

    // Month range (0-11)
    private int begin_day, begin_month, begin_year, begin_hour, begin_minutes;
    private int end_day, end_month, end_year, end_hour, end_minutes;
    private String event_title;
    private String event_description;

    //TODO: load data with voice interaction

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(begin_year, begin_month, begin_day, begin_hour, begin_minutes);
        Calendar endTime = Calendar.getInstance();
        endTime.set(end_year, end_month, end_day, end_hour, end_minutes);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(Events.TITLE, event_title)
                .putExtra(Events.DESCRIPTION, event_description);
        startActivity(intent);
    }
}
