package com.piser.voice;

/**
 * Created by SergioPadilla on 30/12/16.
 */

public class CalendarEvent {

    private long calendar_id;
    private String event_title;
    private String start_time;

    CalendarEvent(long id, String title, String time) {
        calendar_id = id;
        event_title = title;
        start_time = time;
    }

    private long getID() {
        return calendar_id;
    }

    private String getTitle() {
        return event_title;
    }

    private String getTimeStart() {
        return start_time;
    }
}
