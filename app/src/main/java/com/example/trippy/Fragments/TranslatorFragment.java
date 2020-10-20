package com.example.trippy.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.trippy.Activities.MainActivity;
import com.example.trippy.Dialogs.TranslationDialog;
import com.example.trippy.R;
import com.google.android.material.card.MaterialCardView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TranslatorFragment extends Fragment {
    public static final String TAG = "pttt";
    private static final String BASE_URL = "https://www.googleapis.com/language/translate/v2?q=";

    protected View view;
    private LinearLayout mainLayout;
    private RelativeLayout title;
    private TextView result;
    private EditText input;
    private String countryCode = "";
    private String languageCode = "";
    private String userInput = "";

    public TranslatorFragment() {
    }

    public TranslatorFragment(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_translator, container, false);
        }
        mainLayout = view.findViewById(R.id.translateFragment_LAY_focusableLinear);
        title = view.findViewById(R.id.translateFragment_LAY_openTranslatorLayout);
        result = view.findViewById(R.id.translateFragment_LBL_translateResult);
        input = view.findViewById(R.id.translateFragment_EDT_inputText);


        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b && !result.hasFocus()) {
                    Log.d(TAG, "onFocusChange: input translate lost focus view: " + view.toString());
                    input.setText("");
                    result.setText("Translate");
                } else {
                    Log.d(TAG, "onFocusChange: Translate card got focus!");
                    result.setText("Translate");
                }
            }
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //TODO: Limit for 2 words
                if (!result.getText().toString().equals("translate")) {
                    result.setText("Translate");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().toString().equals("")) {
                    input.setError("Enter word to translate");
                } else {
                    userInput = input.getText().toString();
                    //TODO: Deal with edit text focus ( edittext.clearFocus(); )
                    result.setClickable(false);
                    input.clearFocus();
                    translateWord();
                }
            }
        });
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setEnabled(false);
                openTranslatorDialog();
            }
        });

        return view;
    }

    private void translateWord() {
        Log.d(TAG, "translateWord: Translating: " + input.getText().toString());
        getCountryLanguageCode(userInput);
    }

    /**
     * A method to open the translator dialog
     */
    private void openTranslatorDialog() {
        Log.d(TAG, "openTranslatorDialog: Opening translation dialog with countryCode: IL");
        TranslationDialog dialog = new TranslationDialog(getActivity(), countryCode);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.show();
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        dialog.getWindow().setDimAmount(0.9f);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                title.setEnabled(true);
            }
        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    /**
     * A method to get the country language code
     */
    private void getCountryLanguageCode(final String inputText) {
        String requestString = "https://restcountries.eu/rest/v2/alpha/" + countryCode;
        Log.d(TAG, "getCountryLanguageCode: Requesting country language code for: "
                + countryCode.toLowerCase());
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
                    languageCode = iso639_1.getString("iso639_1");
                    String languageName = iso639_1.getString("name");
                    translatePhrase(languageName, inputText);
                } catch (JSONException e) {
                    Log.d(TAG, "Exception: " + "" + e.getMessage());
                }
            }
        });
    }

    private void translatePhrase(String languageName, String inputText) {
        Log.d(TAG, "translatePhrase: Translating phrase: " + inputText + " to language: " + languageName);
        String destLanguage = "&target=" + languageCode;
        String api_key = getActivity().getResources().getString(R.string.google_maps_api_key);
        String requestString = BASE_URL + inputText + destLanguage
                + "&key=" + api_key;
        Log.d(TAG, "translatePhrase: Requesting: " + requestString);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(requestString)
                .header("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: Exception: " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responeMsg = response.body().string();
                Log.d(TAG, "onResponse: " + responeMsg);

                try {
                    JSONObject responseJson = new JSONObject(responeMsg);
                    JSONObject dataJson = responseJson.getJSONObject("data");
                    JSONArray translations = dataJson.getJSONArray("translations");
                    JSONObject translatedTextJSON = translations.getJSONObject(0);
                    String translatedText = translatedTextJSON.getString("translatedText");
                    Log.d(TAG, "onResponse: Translated text: " + translatedText);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            result.setText(userInput + ":\n" + translatedText);
                            result.setClickable(true);
                            //TODO: Cache translated text to sp
                        }
                    });
                } catch (JSONException e) {
                    Log.d(TAG, "onResponse: " + e.getMessage());
                }
            }
        });
    }
}
