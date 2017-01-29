package com.piser.voice;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.SpeechRecognizer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by SergioPadilla on 31/12/16.
 */

public class CreateEvent extends TTSARSActivity {

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

            if(started) {
                if(isEmpty(title)) {
                    title.setText(result);
                    talkAndListen(Models.ASKFORDESCRIPTION);
                }
                else if(isEmpty(description)) {
                    description.setText(result);
                    talkAndListen(Models.ASKFORDAY);
                }
                else if(isEmpty(day)) {
                    day.setText(result);
                    talkAndListen(Models.ASKFORMONTH);
                }
                else if(isEmpty(month)) {
                    if(monthIsOk(result)) {
                        month.setText(result);
                        talkAndListen(Models.ASKFORYEAR);
                    }
                    else {
                        talkAndListen(Models.ERRORMONTH);
                    }
                }
                else if(isEmpty(year)) {
                    year.setText(result);
                    talkAndListen(Models.ASKFORHOUR);
                }
                else if(isEmpty(hour)) {
                    hour.setText(result);
                    talk(Models.FINISH_CREATE);
                    createEvent();
                    started = false;
                }
            }
            else if (Models.HELP.equals(result)) {
                talkAndListen(Models.INSTRUCTIONS_CREATE);
            }
            else if (Models.REMOVE.equals(result)) {
                remove();
                started = false;
                talk(Models.INSTRUCTIONS_CREATE);
            }
            else if(Models.START.equals(result)) {
                remove();
                started = true;
                talkAndListen(Models.ASKFORTITLE);
            }
            else {
                talk(Models.TRYAGAIN);
            }
        }

        stopListening();
    }

    private void createEvent() {
        int begin_day = Integer.parseInt(day.getText().toString());
        int begin_month = parseMonth(month.getText().toString());
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
                .putExtra(CalendarContract.Events.TITLE, title.getText().toString())
                .putExtra(CalendarContract.Events.DESCRIPTION, description.getText().toString());
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

    private void talkAndListen(String message) {
        talk(message);
        listen();
    }

    private boolean monthIsOk(String possible) {
        List<String> months = Models.getMonths();
        boolean equal = false;

        for(int i = 0; i < months.size() && !equal; i++) {
            equal = months.get(i).equals(possible);
        }

        return equal;
    }

    private int parseMonth(String month) {
        return Models.getMonthsDict().get(month);
    }

    @Override
    public void writeFeedback(String message) {}
}
