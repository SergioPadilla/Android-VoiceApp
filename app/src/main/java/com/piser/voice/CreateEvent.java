package com.piser.voice;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.SpeechRecognizer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SergioPadilla on 31/12/16.
 */

public class CreateEvent extends TTSARSActivity {

    // Month range (0-11)
    private int begin_day, begin_month, begin_year, begin_hour, begin_minutes;
    private int end_day, end_month, end_year, end_hour, end_minutes;
    private String event_title;
    private String event_description;

    private TextView day;
    private TextView month;
    private TextView year;
    private TextView hour;
    private TextView title;
    private TextView description;

    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        initComponents();

        day = (TextView) findViewById(R.id.create_event_day);
        month = (TextView) findViewById(R.id.create_event_month);
        year = (TextView) findViewById(R.id.create_event_year);
        hour = (TextView) findViewById(R.id.create_event_hour);
        title = (TextView) findViewById(R.id.create_event_title);
        description = (TextView) findViewById(R.id.create_event_description);
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        welcomeMessage = Models.WELCOME_CREATE;
        started = false;
    }

    @Override
    public void onResults(Bundle results) {
        super.onResults(results);

        if (results != null) {
            ArrayList<String> recognitions = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String result = recognitions.get(0).toLowerCase();
            if (Models.HELP.equals(result)) {
                talk(Models.INSTRUCTIONS_CREATE);
                listen();
            }
            else if (Models.REMOVE.equals(result)) {
                remove();
                started = false;
                talk(Models.INSTRUCTIONS_CREATE);
            }
            else if(started) {
                if(isEmpty(title)) {
                    title.setText(result);
                    talk(Models.ASKFORDESCRIPTION);
                }
                else if(isEmpty(description)) {
                    description.setText(result);
                    talk(Models.ASKFORDAY);
                }
                else if(isEmpty(day)) {
                    day.setText(result);
                    talk(Models.ASKFORMONTH);
                }
                else if(isEmpty(month)) {
                    month.setText(result);
                    talk(Models.ASKFORYEAR);
                }
                else if(isEmpty(year)) {
                    year.setText(result);
                    talk(Models.ASKFORHOUR);
                }
                else if(isEmpty(hour)) {
                    hour.setText(result);
                    talk(Models.FINISH_CREATE);
                    createEvent();
                    started = false;
                }
            }
            else if(Models.START.equals(result)) {
                remove();
                started = true;
                talk(Models.ASKFORTITLE);
            }
            else {
                talk(Models.TRYAGAIN);
            }
        }

        stopListening();
    }

    private void createEvent() {
        int begin_day = Integer.parseInt(day.getText().toString());
        int begin_month = Integer.parseInt(month.getText().toString());
        int begin_year = Integer.parseInt(year.getText().toString());
        int begin_hour = Integer.parseInt(hour.getText().toString());

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(begin_year, begin_month, begin_day, begin_hour, 0);
        Calendar endTime = Calendar.getInstance();
        endTime.set(begin_year, begin_month, begin_day, begin_hour+1%24, 0);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, event_title)
                .putExtra(CalendarContract.Events.DESCRIPTION, event_description);
        startActivity(intent);
    }

    private boolean isEmpty(TextView text) {
        return text.getText().length() > 0;
    }

    private void remove() {
        day.setText("");
        month.setText("");
        year.setText("");
        hour.setText("");
        title.setText("");
        description.setText("");
    }

    @Override
    public void writeFeedback(String message) {}
}
