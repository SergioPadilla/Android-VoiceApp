package com.piser.voice;

/**
 * Created by SergioPadilla on 30/12/16.
 */

public class CalendarEvent {

    private long calendar_id;
    private String event_title;

    CalendarEvent(long id, String title) {
        calendar_id = id;
        event_title = title;
    }

    public long getID() {
        return calendar_id;
    }

    public String getTitle() {
        return event_title;
    }
}
