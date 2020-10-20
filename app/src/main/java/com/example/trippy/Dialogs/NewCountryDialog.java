package com.example.trippy.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.trippy.Interfaces.NewUserDetailsCallback;
import com.example.trippy.Interfaces.OnSelectedCountryListener;
import com.example.trippy.Objects.MyUser;
import com.example.trippy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

public class NewCountryDialog extends Dialog {
    private static final String TAG = "pttt";
    private Context context;
    private Spinner countryList;
    private TextView confirm;
    private ArrayList<String> names;


    public NewCountryDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Creating country select dialog");
        setContentView(R.layout.dialog_select_country);
        initViews();
    }

    /**
     * A method to return a String json from raw folder
     * id = R.raw.filename
     */
    private String readJsonFromRaw(int id) throws IOException {
        String res = "";
        InputStream is = context.getResources().openRawResource(id);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.d(TAG, "Can't convert to string " + e.getMessage());
        } finally {
            is.close();
        }
        return writer.toString();
    }

    /**
     * A method to populate country list with countries
     */
    private void populateCountryList() {
        Log.d(TAG, "populateCountryList: Populating country list");
        try {
            String countriesList = readJsonFromRaw(R.raw.countries);
            JSONArray countriesArrayJson = new JSONArray(countriesList);
            names = new ArrayList<>();
            names.add("Country");
            for (int i = 0; i < countriesArrayJson.length(); i++) {
                JSONObject temp = (JSONObject) countriesArrayJson.get(i);
                names.add(temp.get("name").toString());
            }

            //create an ArrayAdaptar from the String Array
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, names);
            //set the view for the Drop down list
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //set the ArrayAdapter to the spinner
            countryList.setAdapter(dataAdapter);
            //attach the listener to the spinner
            countryList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "onItemSelected: Selected: " + names.get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } catch (IOException | JSONException e) {
            Log.d(TAG, "populateCountryList: Exception: " + e.getMessage());
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: initing views");
        countryList = findViewById(R.id.selectCountry_LST_countrySpinner);
        populateCountryList();
        confirm = findViewById(R.id.selectCountry_LBL_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIfCountrySelected();
            }
        });
    }

    private void checkIfCountrySelected() {
        Log.d(TAG, "checkIfCountrySelected: Checking if country selected");
        if (countryList.getSelectedItem().toString().equalsIgnoreCase("Country")) {
            Log.d(TAG, "checkIfCountrySelected: User did not select a country");
            ((TextView) countryList.getSelectedView()).setError("Please select a country");
            return;
        }
        String mCountry = countryList.getSelectedItem().toString();
        calBackSelectedCountry(mCountry);
    }

    /**
     * A method to callBack selected country to main activity
     */
    private void calBackSelectedCountry(String country) {
        Log.d(TAG, "setOnDismissListener: Dismissing calendar dialog");
        /** Callback days list and name */
        OnSelectedCountryListener onSelectedCountryListener;
        try {
            onSelectedCountryListener = (OnSelectedCountryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement dialog listener");
        }
        // Send the country name to main layout

        onSelectedCountryListener.getSelectedCountry(country);
        dismiss();
    }


}
