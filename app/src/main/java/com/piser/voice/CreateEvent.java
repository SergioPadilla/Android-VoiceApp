package com.piser.voice;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class CreateEvent extends ASRActivity {

    // Month range (0-11)
    private int begin_day, begin_month, begin_year, begin_hour, begin_minutes;
    private int end_day, end_month, end_year, end_hour, end_minutes;
    private String event_title;
    private String event_description;

    private TextView feedback;

    //TODO: load data with voice interaction

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        initComponents();

        feedback = (TextView) findViewById(R.id.feedbackTxt);

//        Calendar beginTime = Calendar.getInstance();
//        beginTime.set(begin_year, begin_month, begin_day, begin_hour, begin_minutes);
//        Calendar endTime = Calendar.getInstance();
//        endTime.set(end_year, end_month, end_day, end_hour, end_minutes);
//        Intent intent = new Intent(Intent.ACTION_INSERT)
//                .setData(Events.CONTENT_URI)
//                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
//                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
//                .putExtra(Events.TITLE, event_title)
//                .putExtra(Events.DESCRIPTION, event_description);
//        startActivity(intent);
    }

    /***********************************************************************************************
     * Process ASR events
     **********************************************************************************************/

    @Override
    public void onResults(Bundle results) {
        super.onResults(results);
        if(results!=null){
            //Retrieves the N-best list and the confidences from the ASR result
            ArrayList<String> nBestList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            
            ArrayList<String> nBestView = new ArrayList<String>();

            if(nBestList != null && nBestList.size() > 0)
                nBestView.add(nBestList.get(0));

            setListView(nBestView);
        }

        stopListening();
    }

    /**
     * Includes the recognition results in the list view
     * @param nBestView list of matches
     */
    private void setListView(ArrayList<String> nBestView){
        // Instantiates the array adapter to populate the listView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nBestView);
        ListView listView = (ListView) findViewById(R.id.nbest_listview);
        listView.setAdapter(adapter);
    }

    @Override
    public void writeFeedback(String message) {
        feedback.setText(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ai_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_record_voice) {
            listen();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
}
