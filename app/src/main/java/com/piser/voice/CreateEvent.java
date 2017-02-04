package com.piser.voice;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.SpeechRecognizer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by SergioPadilla on 31/12/16.
 */

public class CreateEvent extends TTSARSActivity {

    private TextView day;
    private TextView month;
    private TextView year;
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

            if(started) { // check if the user started to complete the field before
                if(isEmpty(title)) {
                    // if started and title is empty => recognition correspond with title
                    title.setText(result);
                    talkAndListen(Models.ASKFORDESCRIPTION);
                }
                else if(isEmpty(description)) {
                    // if started and description is empty => recognition correspond with description
                    description.setText(result);
                    talkAndListen(Models.ASKFORDAY);
                }
                else if(isEmpty(day)) {
                    // if started and day is empty => recognition correspond with day
                    if(isInteger(result)) {
                        // must be a number and between 1 and 31
                        if(Integer.parseInt(result) > 0 && Integer.parseInt(result) < 32) {
                            day.setText(result);
                            talkAndListen(Models.ASKFORMONTH);
                        }
                        else
                            talkAndListen(Models.ERRORDAY2);
                    }
                    else
                        talkAndListen(result + Models.ERRORDAY1);
                }
                else if(isEmpty(month)) {
                    // if started and month is empty => recognition correspond with month
                    if(monthIsOk(result)) {
                        // should be a real month
                        month.setText(result);
                        talkAndListen(Models.ASKFORYEAR);
                    }
                    else
                        talkAndListen(Models.ERRORMONTH);
                }
                else if(isEmpty(year)) {
                    // if started and year is empty => recognition correspond with year
                    if(isInteger(result)) {
                        // should a number and greater or equal to this year
                        if(Integer.parseInt(result) >= 2017) {
                            year.setText(result);
                            talk(Models.FINISH_CREATE);
                            createEvent();
                        }
                        else
                            talkAndListen(Models.ERRORYEAR2);
                    }
                    else
                        talkAndListen(Models.ERRORYEAR1);
                }
            }
            else if (Models.HELP.equals(result)) {
                // user said "ayuda"
                talkAndListen(Models.INSTRUCTIONS_CREATE);
            }
            else if (Models.REMOVE.equals(result)) {
                // user said "borrar"
                remove();
                started = false;
                talk(Models.INSTRUCTIONS_CREATE);
            }
            else if(Models.START.equals(result)) {
                // user said "comenzar"
                remove();
                started = true;
                talkAndListen(Models.ASKFORTITLE);
            }
            else {
                // Nothing match
                talk(Models.TRYAGAIN);
            }
        }

        stopListening();
    }

    private boolean isInteger(String s) {
        /**
         * Check if the user said some int
         */
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private void createEvent() {
        /**
         * Create event in the calendar of the user
         */
        int begin_day = Integer.parseInt(day.getText().toString());
        int begin_month = parseMonth(month.getText().toString());
        int begin_year = Integer.parseInt(year.getText().toString());
        //int begin_hour = Integer.parseInt(hour.getText().toString());
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int begin_hour = calendar.get(Calendar.HOUR_OF_DAY);

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
        /**
         * Check if the field is empty, util to know if the user have completed this field before
         */
        return text.getText().length() == 0;
    }

    private void remove() {
        /**
         * Clean the fields
         */
        day.setText("");
        month.setText("");
        year.setText("");
        title.setText("");
        description.setText("");
    }

    private boolean monthIsOk(String possible) {
        /**
         * Check if the month is real month
         */
        List<String> months = Models.getMonths();
        boolean equal = false;

        for(int i = 0; i < months.size() && !equal; i++) {
            equal = months.get(i).equals(possible);
        }

        return equal;
    }

    private int parseMonth(String month) {
        /**
         * Transform the month string into the int correspond with that month (0-11)
         */
        return Models.getMonthsDict().get(month);
    }

    @Override
    public void writeFeedback(String message) {}
}
