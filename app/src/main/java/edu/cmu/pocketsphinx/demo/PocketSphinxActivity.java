package edu.cmu.pocketsphinx.demo;
//package edu.cmu.pocketsphinx.demo.wear;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    //    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PLAY_SEARCH = "huddle-break";

    private static final String TREE_HANDOFF = "hand-off";
    private static final String TREE_PITCH = "pitch";
    private static final String TREE_PASS = "pass";
    private static final String TREE_RUN = "run";
    private static final String TREE_KICK = "kick";
    private static final String TREE_PUNT = "punt";
    private static final String TREE_FUMBLED = "fumbled";
    private static final String TREE_SACKED = "sacked";
    private static final String TREE_PENALTY = "penalty";

    private static final String TREE_NUMBERS = "to-number";
    //    private static final String PHONE_SEARCH = "phones";
    private static final String MENU_SEARCH = "menu";


    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "flag football";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(MENU_SEARCH, R.string.menu_caption);
        captions.put(DIGITS_SEARCH, R.string.digits_caption);
        captions.put(PLAY_SEARCH, R.string.play_caption);

        captions.put(TREE_FUMBLED, R.string.fumbled_caption);
        captions.put(TREE_HANDOFF, R.string.handoff_caption);
        captions.put(TREE_KICK, R.string.kick_caption);
        captions.put(TREE_PASS, R.string.pass_caption);
        captions.put(TREE_PENALTY, R.string.penalty_caption);
        captions.put(TREE_PITCH, R.string.pitch_caption);
        captions.put(TREE_PUNT, R.string.punt_caption);
        captions.put(TREE_RUN, R.string.run_caption);
        captions.put(TREE_SACKED, R.string.sacked_caption);
        captions.put(TREE_NUMBERS, R.string.numbers_caption);
//        captions.put(PHONE_SEARCH, R.string.phone_caption);
//        captions.put(FORECAST_SEARCH, R.string.forecast_caption);
        setContentView(R.layout.main);
        ((TextView) findViewById(edu.cmu.pocketsphinx.demo.R.id.caption_text))
                .setText("Preparing the recognizer");

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        runRecognizerSetup();
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE))
            switchSearch(MENU_SEARCH);

        else if (text.equals(DIGITS_SEARCH))
            switchSearch(DIGITS_SEARCH);
//        else if (text.equals(PLAY_SEARCH))
//            switchSearch(PLAY_SEARCH);

        else if (text.equals(TREE_HANDOFF)) switchSearch(TREE_HANDOFF);
        else if (text.equals(TREE_PITCH)) switchSearch(TREE_PITCH);
        else if (text.equals(TREE_PASS)) switchSearch(TREE_PASS);
        else if (text.equals(TREE_RUN)) switchSearch(TREE_RUN);
        else if (text.equals(TREE_KICK)) switchSearch(TREE_KICK);
        else if (text.equals(TREE_PUNT)) switchSearch(TREE_PUNT);
        else if (text.equals(TREE_FUMBLED)) switchSearch(TREE_FUMBLED);
        else if (text.equals(TREE_SACKED)) switchSearch(TREE_SACKED);
        else if (text.equals(TREE_PENALTY)) switchSearch(TREE_PENALTY);

        else if (text.contains(TREE_NUMBERS)) switchSearch(TREE_NUMBERS);
        else if (text.contains("huddle-break")) switchSearch(MENU_SEARCH);


//        else if (text.equals(PHONE_SEARCH))
//            switchSearch(PHONE_SEARCH);
//        else if (text.equals(FORECAST_SEARCH))
//            switchSearch(FORECAST_SEARCH);
        else
            ((TextView) findViewById(R.id.result_text)).setText(text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
//            String prob = String.valueOf(hypothesis.getProb());
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
//        if (!recognizer.getSearchName().equals(KWS_SEARCH))
//            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 20000);

        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
//                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setDictionary(new File(assetsDir, "flag-football-en-us.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

        // Create grammar-based search for digit recognition
//        File flagFootballGrammar = new File(assetsDir, "flag-football.gram");
//        recognizer.addGrammarSearch(PLAY_SEARCH, flagFootballGrammar);

        File flagFootballPass = new File(assetsDir, "flag-football-pass.gram");
        recognizer.addGrammarSearch(TREE_PASS, flagFootballPass);

        File flagFootballHandoff = new File(assetsDir, "flag-football-handoff.gram");
        recognizer.addGrammarSearch(TREE_HANDOFF, flagFootballHandoff);

        File flagFootballNumbers = new File(assetsDir, "flag-football-numbers.gram");
        recognizer.addGrammarSearch(TREE_NUMBERS, flagFootballNumbers);

        // Create language model search
//        File languageModel = new File(assetsDir, "weather.dmp");
//        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);

        // Phonetic search
//        File phoneticModel = new File(assetsDir, "en-phone.dmp");
//        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
}