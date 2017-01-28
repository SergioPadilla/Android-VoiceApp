package com.piser.voice;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract.Events;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ASRActivity {


    private int TTS_DATA_CHECK = 12;    // Request code to identify the intent that looks for a TTS Engine in the device

    private TextToSpeech mytts = null;


    private ListView events_list;
    private EventsAdapter adapter;

    private List<CalendarEvent> calendarEvents;

    // The indices for the projection
    private static final int PROJECTION_CALENDAR_ID_INDEX = 0;
    private static final int PROJECTION_ORGANIZER_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;
    private static final int PROJECTION_DESCRIPTION_INDEX = 3;
    private static final int PROJECTION_DTSTART_INDEX = 4;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up the speak button

        // Invoke the method to initialize text to speech
        initComponents();
        calendarEvents = new ArrayList<>();
        adapter = new EventsAdapter(MainActivity.this);

        events_list = (ListView) findViewById(R.id.events_list);

        events_list.setAdapter(adapter);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        initTTS();
    }


    private void saidInstructions(){
        String text = Models.INSTRUCCIONES;

        // If there is text, call the method speak() to synthesize it
        if (text != null && text.length() > 0) {

            if (Build.VERSION.SDK_INT >= 21) {
                //For SDK 21 and later, the speak method accepts four parameters:
                //text: the string to be spoken (obtained from the interface)
                //QUEUE_ADD: queuing strategy = this message is added to the end of the playback queue
                //null: we do not indicate any specific synthesis parameters, just use the default
                //"msg": unique identifier for this request
                mytts.speak(text, TextToSpeech.QUEUE_ADD, null, "msg");
            } else {
                //For earlier versions, it accepts three parameters (deprecated)
                mytts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }
    private void saidTryAgain(){
        String text = Models.TRYAGAIN;

        // If there is text, call the method speak() to synthesize it
        if (text != null && text.length() > 0) {

            if (Build.VERSION.SDK_INT >= 21) {
                //For SDK 21 and later, the speak method accepts four parameters:
                //text: the string to be spoken (obtained from the interface)
                //QUEUE_ADD: queuing strategy = this message is added to the end of the playback queue
                //null: we do not indicate any specific synthesis parameters, just use the default
                //"msg": unique identifier for this request
                mytts.speak(text, TextToSpeech.QUEUE_ADD, null, "msg");
            } else {
                //For earlier versions, it accepts three parameters (deprecated)
                mytts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }
    private void initTTS() {

        //Check if the engine is installed, when the check is finished, the
        //onActivityResult method is automatically invoked
        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_DATA_CHECK);
    }



    private void getEvents() {
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
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long calID = cursor.getLong(PROJECTION_CALENDAR_ID_INDEX);
                String event_title = cursor.getString(PROJECTION_TITLE_INDEX);
                String dtStart = cursor.getString(PROJECTION_DTSTART_INDEX);

                calendarEvents.add(new CalendarEvent(calID, event_title, dtStart));
            }
        }

        adapter.loadEvents(calendarEvents);
    }

    @Override
    public void writeFeedback(String message) {

    }

    @Override
    public void onResults(Bundle results) {
        super.onResults(results);

        if (results != null) {
            //Retrieves the N-best list and the confidences from the ASR result
            ArrayList<String> nBestList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String BestResult = nBestList.get(0);
            if (Models.CREAR.equals(BestResult.toLowerCase())){
                startActivity(new Intent(MainActivity.this, CreateEvent.class));
            }
            else if (Models.LISTAR.equals(BestResult.toLowerCase())){
                getEvents();
            }
            else if (Models.AYUDA.equals(BestResult.toLowerCase())){
                saidInstructions();
            }
            else {
                saidTryAgain();
            }

        }

        stopListening();
    }

    /**
     * Callback from check for text to speech engine installed
     * If positive, then creates a new <code>TextToSpeech</code> instance which will be called when user
     * clicks on the 'Speak' button
     * If negative, creates an intent to install a <code>TextToSpeech</code> engine
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check that the result received is from the TTS_DATA_CHECK action
        if (requestCode == TTS_DATA_CHECK) {

            // If the result of the action is CHECK_VOICE_DATA_PASS, there is a TTS Engine
            //available in the device
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {

                // Create a TextToSpeech instance
                mytts = new TextToSpeech(this, new OnInitListener() {
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            // Display Toast
                            Toast.makeText(MainActivity.this, "TTS initialized", Toast.LENGTH_LONG).show();

                            // Set language to US English if it is available
                                mytts.setLanguage( new Locale("ES"));
                        }
                        saidInstructions();
                    }
                });
            } else {
                // The TTS is not available, we will try to install it:
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);

                PackageManager pm = getPackageManager();
                ResolveInfo resolveInfo = pm.resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);

                //If the install can be started automatically we launch it (startActivity), if not, we
                //ask the user to install the TTS from Google Play (toast)
                if (resolveInfo != null) {
                    startActivity(installIntent);
                } else {
                    Toast.makeText(MainActivity.this, "There is no TTS installed, please download it from Google Play", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }


    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
