package com.example.trippy.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.trippy.R;
import com.google.android.material.button.MaterialButton;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class TranslationDialog extends Dialog {
    public static final String TAG = "pttt";
    private static final String BASE_URL = "https://www.googleapis.com/language/translate/v2?q=";


    private EditText textToTranslateEdt;
    private TextView translatedTextLabel;
    private TextView translateButton;
    private Context context;
    private String myCountryCode = "";
    private String myCountryLanguageCode = "";

    private String srcLng = "en";

    public TranslationDialog(@NonNull Context context, String myCountryCode) {
        super(context);
        Log.d(TAG, "TranslationDialog: creating translation dialog");
        this.context = context;
        this.myCountryCode = myCountryCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Creating new translation window");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_translator);
        initViews();
    }

    private void translatePhrase(String languageName) {
        String input = "";
        if (!textToTranslateEdt.getText().toString().equals("")) { // if textbox is not empty translate
            Log.d(TAG, "translatePhrase: Translating: " + textToTranslateEdt.getText().toString());
            input = textToTranslateEdt.getText().toString();
        } else {
            translatedTextLabel.setError("Please enter text");
        }

        String sourceLanguage = "&source=" + srcLng;
        String destLanguage = "&target=" + myCountryLanguageCode;
        String api_key = context.getResources().getString(R.string.google_maps_api_key);

//         Using src and dest languages
        String requestString = BASE_URL + input + destLanguage
                + "&key=" + api_key;


        // Detecting src and translating to english
//        String requestString = BASE_URL + input + destLanguage + "&key=" + api_key;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(requestString)
                .header("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();
                Log.d(TAG, "onResponse: " + responseString);
                try {

                    JSONObject responseJson = new JSONObject(responseString);
                    Log.d(TAG, "onResponse: Whole respone: " + responseString);
                    JSONObject dataJson = responseJson.getJSONObject("data");
                    JSONArray translations = dataJson.getJSONArray("translations");
                    JSONObject translatedTextJSON = translations.getJSONObject(0);
                    String translatedText = translatedTextJSON.getString("translatedText");
                    Handler refresh = new Handler(Looper.getMainLooper());
                    refresh.post(new Runnable() {
                        public void run() {
                            translatedTextLabel.setText(languageName + ":\n" + translatedText);
                            translatedTextLabel.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (JSONException e) {
                    Log.d(TAG, "Exception: " + "" + e.getMessage());
                }
            }
        });
    }

    private void initViews() {
        textToTranslateEdt = findViewById(R.id.translator_EDT_inputText);
        translatedTextLabel = findViewById(R.id.translator_LBL_outputText);
        translateButton = findViewById(R.id.translator_BTN_translate);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCountryLanguageCode();
            }
        });
    }

    /**
     * A method to get the country language code
     */
    private void getCountryLanguageCode() {
        String requestString = "https://restcountries.eu/rest/v2/alpha/" + myCountryCode;
        Log.d(TAG, "getCountryLanguageCode: Requesting country language code for: " + myCountryCode.toLowerCase());
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(requestString)
                .header("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();
                Log.d(TAG, "onResponse: " + responseString);
                try {

                    JSONObject responseJson = new JSONObject(responseString);
//                    "languages":[{"iso639_1":"pt","iso639_2":"por","name":"Portuguese","nativeName":"Português"}]
                    JSONArray languages = responseJson.getJSONArray("languages");
                    JSONObject iso639_1 = (JSONObject) languages.get(0);
                    myCountryLanguageCode = iso639_1.getString("iso639_1");
                    String languageName = iso639_1.getString("name");
                    translatePhrase(languageName);


                } catch (JSONException e) {
                    Log.d(TAG, "Exception: " + "" + e.getMessage());
                }
            }
        });
    }
}
