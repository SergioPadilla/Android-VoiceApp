package com.piser.voice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * Created by SergioPadilla on 22/1/17.
 */

public class AIActivity extends AppCompatActivity implements AIListener {

    private final static String LOGTAG = "ApiAi";

    private AIService aiService;

    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_ai);

        resultTextView = (TextView) findViewById(R.id.result_api_api);

        initAIService();
    }

    public void initAIService() {
        final AIConfiguration config = new AIConfiguration(Config.AI_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }

    public void listen() {
        aiService.startListening();
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

    @Override
    public void onResult(final AIResponse response) {
        Log.e(LOGTAG, "onResult");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Result result = response.getResult();

                // Get parameters
                String parameterString = "";
                if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                    for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                        parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                    }
                }

                // Show results in TextView.
                resultTextView.setText("Query:" + result.getResolvedQuery() +
                        "\nAction: " + result.getAction() +
                        "\nParameters: " + parameterString);
            }
        });
    }

    @Override
    public void onError(final AIError error) {
        Log.e(LOGTAG, "onError");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText(error.toString());
            }
        });
    }

    @Override
    public void onAudioLevel(float level) {
        // show sound level
    }

    @Override
    public void onListeningStarted() {
        // show recording indicator
    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {
        // hide recording indicator
    }
}
