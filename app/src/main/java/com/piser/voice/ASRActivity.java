package com.piser.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public abstract class ASRActivity extends AppCompatActivity implements RecognitionListener {

    private static final String LOGTAG = "ASRActivity";

    private SpeechRecognizer mASR;
    private long startListeningTime = 0; // To skip errors (see onError method)
    private static final int REQUESTRECORDAUDIO = 22;


    protected void initComponents() {
        initASR();
    }

    private void initASR() {
        List<ResolveInfo> intActivities = this.getPackageManager().queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        //Speech recognition does not currently work on simulated devices
        if("generic".equalsIgnoreCase(Build.BRAND)){
            Log.e(LOGTAG, "ASR is not supported on virtual devices");
        } else {
            if (!intActivities.isEmpty()) {
                mASR = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                mASR.setRecognitionListener(this);
            }
        }
    }

    public void listen() {
        // Check we have permission to record audio
        if(hasASRPermission()) {
            startListening();
        }
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify the application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());

        // Specify language model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results to receive. Results listed in order of confidence
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);

        // Specify recognition language
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH);

        Log.e(LOGTAG, "Going to start listening...");
        this.startListeningTime = System.currentTimeMillis();
        mASR.startListening(intent);
    }

    public boolean hasASRPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // If  an explanation is required, show it
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
                Toast.makeText(getApplicationContext(), "SimpleASR must access the microphone in order to perform speech recognition", Toast.LENGTH_SHORT).show();

            // Request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUESTRECORDAUDIO); //Callback in "onRequestPermissionResult"

            return false;
        }
        else
            return true;
    }

    public void stopListening(){
        mASR.stopListening();
        Log.e(LOGTAG, "Stopped listening");
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        writeFeedback("Ready to hear you speak!");
        Toast.makeText(getApplicationContext(), "What do you want to search?", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {
        writeFeedback("You have started talking");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        writeFeedback("Buffer received");
    }

    @Override
    public void onRmsChanged(float v) {
        // not need
    }

    @Override
    public void onEndOfSpeech() {
        writeFeedback("You have stopped talking");
    }

    @Override
    public void onResults(Bundle results) {
        if(results!=null) {
            Log.i(LOGTAG, "ASR results received ok");
            writeFeedback("Results ready");
        }
        else {
            Log.e(LOGTAG, "ASR results null");
            writeFeedback("Error");
        }
    }

    @Override
    public void onError(final int errorCode) {
        //Possible bug in Android SpeechRecognizer: NO_MATCH errors even before the ASR
        // has even tried to recognized. We have adopted the solution proposed in:
        // http://stackoverflow.com/questions/31071650/speechrecognizer-throws-onerror-on-the-first-listening
        long duration = System.currentTimeMillis() - startListeningTime;
        if (duration < 500 && errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            Log.e(LOGTAG, "Doesn't seem like the system tried to listen at all. duration = " + duration + "ms. Going to ignore the error");
            stopListening();
        }
        else {
            String errorMsg;
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
            if (errorCode != 5 || !"Unknown client side error".equals(errorMsg)) {
                Log.e(LOGTAG, "Error -> " + errorMsg);
                stopListening();
            }
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        writeFeedback("Parcial results received");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        // not need
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mASR != null)
            mASR.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUESTRECORDAUDIO) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOGTAG, "Record audio permission granted");
                startListening();
            }
            else {
                Log.i(LOGTAG, "Record audio permission denied");
                Toast.makeText(getApplicationContext(), "Sorry, RichASR cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
            }
        }
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

    public abstract void writeFeedback(String message);
}
