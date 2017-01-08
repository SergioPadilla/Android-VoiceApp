package com.piser.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateEvent extends AppCompatActivity implements RecognitionListener {

    // Month range (0-11)
    private int begin_day, begin_month, begin_year, begin_hour, begin_minutes;
    private int end_day, end_month, end_year, end_hour, end_minutes;
    private String event_title;
    private String event_description;

    private TextView feedback;

    private SpeechRecognizer myASR;

    // Default values for the language model and maximum number of recognition results
    // They are shown in the GUI when the app starts, and they are used when the user selection is not valid
    private final static int DEFAULT_NUMBER_RESULTS = 10;
    private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;

    private int numberRecoResults = DEFAULT_NUMBER_RESULTS;
    private String languageModel = DEFAULT_LANG_MODEL;

    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;
    private static final String LOGTAG = "RICHASR";

    private long startListeningTime = 0; // To skip errors (see onError method)

    //TODO: load data with voice interaction

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        
        //Initialize ASR
        initASR();

        feedback = (TextView) findViewById(R.id.feedbackTxt);
        Button speak = (Button) findViewById(R.id.speech_btn);

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecognitionParams(); //Read speech recognition parameters from GUI
                listen(Locale.FRENCH, languageModel, numberRecoResults); 				//Set up the recognizer with the parameters and start listening
            }
        });

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

    /*****************************************************************************************
     * MANAGE ASR
     *****************************************************************************************/

    /**
     * Creates the speech recognizer instance if it is available
     * */
    public void initASR() {

        // find out whether speech recognition is supported
        List<ResolveInfo> intActivities = this.getPackageManager().queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        //Speech recognition does not currently work on simulated devices
        if("generic".equals(Build.BRAND.toLowerCase())){
            Log.e(LOGTAG, "ASR is not supported on virtual devices");
        } else {
            if (intActivities.size() != 0) {
                myASR = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                myASR.setRecognitionListener(this);
            }
        }

        Log.i(LOGTAG, "ASR initialized");
    }

    /**
     * Starts speech recognition after checking the ASR parameters
     *
     * @param language Language used for speech recognition (e.g. Locale.ENGLISH)
     * @param languageModel Type of language model used (free form or web search)
     * @param maxResults Maximum number of recognition results
     */
    public void listen(final Locale language, final String languageModel, final int maxResults) {
        Button b = (Button) findViewById(R.id.speech_btn);
        b.setEnabled(false);

        if((languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) || languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)) && (maxResults>=0)) {
            // Check we have permission to record audio
            checkASRPermission();

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            // Specify the calling package to identify the application
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            //Caution: be careful not to use: getClass().getPackage().getName());

            // Specify language model
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

            // Specify how many results to receive. Results listed in order of confidence
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);

            // Specify recognition language
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);

            Log.i(LOGTAG, "Going to start listening...");
            this.startListeningTime = System.currentTimeMillis();
            myASR.startListening(intent);

        }
        else {
            Log.e(LOGTAG, "Invalid params to listen method");
            feedback.setText("Error: invalid parameters");
        }

    }

    /**
     * Checks whether the user has granted permission to the microphone. If the permission has not been provided,
     * it is requested. The result of the request (whether the user finally grants the permission or not)
     * is processed in the onRequestPermissionsResult method.
     *
     * This is necessary from Android 6 (API level 23), in which users grant permissions to apps
     * while the app is running. In previous versions, the permissions were granted when installing the app
     * See: http://developer.android.com/intl/es/training/permissions/requesting.html
     */
    public void checkASRPermission() {
        if (android.support.v4.content.ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // If  an explanation is required, show it
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
                Toast.makeText(getApplicationContext(), "SimpleASR must access the microphone in order to perform speech recognition", Toast.LENGTH_SHORT).show();

            // Request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO); //Callback in "onRequestPermissionResult"
        }
    }

    /**
     * Processes the result of the record audio permission request. If it is not granted, the
     * abstract method "onRecordAudioPermissionDenied" method is invoked. Such method must be implemented
     * by the subclasses of VoiceActivity.
     * More info: http://developer.android.com/intl/es/training/permissions/requesting.html
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.i(LOGTAG, "Record audio permission granted");
            else {
                Log.i(LOGTAG, "Record audio permission denied");
                Toast.makeText(getApplicationContext(), "Sorry, RichASR cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Stops listening to the user
     */
    public void stopListening(){
        myASR.stopListening();
        Log.i(LOGTAG, "Stopped listening");
        Button b = (Button) findViewById(R.id.speech_btn);
        b.setEnabled(true);
    }

    /***********************************************************************************************
     * Process ASR events
     **********************************************************************************************/

	/*
	 * (non-Javadoc)
	 *
	 * Invoked when the ASR provides recognition results
	 *
	 * @see android.speech.RecognitionListener#onResults(android.os.Bundle)
	 */
    @Override
    public void onResults(Bundle results) {
        if(results!=null){
            Log.i(LOGTAG, "ASR results received ok");
            feedback.setText("Results ready");

            //Retrieves the N-best list and the confidences from the ASR result
            ArrayList<String> nBestList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            
            ArrayList<String> nBestView = new ArrayList<String>();

            if(nBestList != null && nBestList.size() > 0)
                nBestView.add(nBestList.get(0));

            setListView(nBestView);

        }
        else{
            Log.e(LOGTAG, "ASR results null");
            //There was a recognition error
            feedback.setText("Error");
        }

        stopListening();
    }

    /*
     * (non-Javadoc)
     *
     * Invoked when the ASR is ready to start listening
     *
     * @see android.speech.RecognitionListener#onReadyForSpeech(android.os.Bundle)
     */
    @Override
    public void onReadyForSpeech(Bundle arg0) {
        feedback.setText("Ready to hear you speak!");
    }

    /*
     * (non-Javadoc)
     *
     * Invoked when the ASR encounters an error
     *
     * @see android.speech.RecognitionListener#onError(int)
     */
    @Override
    public void onError(final int errorCode) {
        //Possible bug in Android SpeechRecognizer: NO_MATCH errors even before the the ASR
        // has even tried to recognized. We have adopted the solution proposed in:
        // http://stackoverflow.com/questions/31071650/speechrecognizer-throws-onerror-on-the-first-listening
        long duration = System.currentTimeMillis() - startListeningTime;
        if (duration < 500 && errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            Log.e(LOGTAG, "Doesn't seem like the system tried to listen at all. duration = " + duration + "ms. Going to ignore the error");
            stopListening();
        }
        else {
            String errorMsg = "";
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMsg = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMsg = "Unknown client side error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMsg = "Insufficient permissions";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMsg = "Network related error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMsg = "Network operation timed out";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMsg = "No recognition result matched";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMsg = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errorMsg = "Server sends error status";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMsg = "No speech input";
                    break;
                default:
                    errorMsg = "";
            }
            if (errorCode == 5 && errorMsg.equals("Unknown client side error")) {
                //Log.e(LOGTAG, "Going to ignore the error");
                //Another frequent error that is not really due to the ASR
            } else {
                feedback.setText("Error: " + errorMsg);
                Log.e(LOGTAG, "Error -> " + errorMsg);
                stopListening();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onBeginningOfSpeech()
     */
    @Override
    public void onBeginningOfSpeech() {
        feedback.setText("You have started talking");
    }

    /*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onBufferReceived(byte[])
     */
    @Override
    public void onBufferReceived(byte[] buffer) {
        feedback.setText("Buffer received");
    }

    /*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onBeginningOfSpeech()
     */
    @Override
    public void onEndOfSpeech() {
        feedback.setText("You have stopped talking");
    }

    /*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onEvent(int, android.os.Bundle)
     */
    @Override
    public void onEvent(int arg0, Bundle arg1) {}

    /*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onPartialResults(android.os.Bundle)
     */
    @Override
    public void onPartialResults(Bundle arg0) {
        feedback.setText("Parcial results received");
    }

    /*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onRmsChanged(float)
     */
    @Override
    public void onRmsChanged(float arg0) {}


    /**
     * Reads the values for the language model and the maximum number of recognition results
     * from the GUI
     */
    private void setRecognitionParams()  {
        numberRecoResults = DEFAULT_NUMBER_RESULTS;
        languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
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
    public void onDestroy() {
        super.onDestroy();
        myASR.destroy();
        Log.i(LOGTAG, "ASR destroyed");
    }
}
