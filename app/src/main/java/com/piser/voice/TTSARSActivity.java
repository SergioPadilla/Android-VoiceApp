package com.piser.voice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by SergioPadilla on 29/1/17.
 */

public abstract class TTSARSActivity extends ASRActivity {

    int TTS_CODE_REQUEST = 12;
    TextToSpeech tts = null;
    String welcomeMessage;

    boolean shouldListen;

    @Override
    protected void initComponents() {
        super.initComponents();
        initTTS();
        welcomeMessage = Models.WELCOME;
    }

    public void initTTS() {
        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_CODE_REQUEST);
    }

    private void talk(String message, final boolean listen) {
        shouldListen = listen;
        if (Build.VERSION.SDK_INT >= 21) {
            tts.speak(message, TextToSpeech.QUEUE_ADD, null, "msg");
        } else {
            tts.speak(message, TextToSpeech.QUEUE_ADD, null);
        }
    }

    void talk(String message) {
        talk(message, false);
    }

    void talkAndListen(String message) {
        talk(message, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CODE_REQUEST) {
            // If the result of the action is CHECK_VOICE_DATA_PASS, there is a TTS Engine
            //available in the device
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // TTS available, create it
                tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Toast.makeText(TTSARSActivity.this, "TTS initialized", Toast.LENGTH_LONG).show();
                            tts.setLanguage(new Locale("ES"));
                            talk(welcomeMessage);
                        }
                    }
                });
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(shouldListen)
                                    listen();
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
            } else {
                // TTS not available, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);

                PackageManager pm = getPackageManager();
                ResolveInfo resolveInfo = pm.resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);

                if (resolveInfo != null) {
                    startActivity(installIntent);
                } else {
                    Toast.makeText(TTSARSActivity.this, "There is no TTS installed, please download it from Google Play", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void errorRecognition() {
        talk(Models.ERRORRECOGNITION);
    }
}
