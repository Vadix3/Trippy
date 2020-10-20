package com.example.trippy.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.trippy.Interfaces.NewUserDetailsCallback;
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

public class NewAccountDialog extends Dialog {
    private static final String TAG = "pttt";
    private Context context;
    private ImageView exitBtn;
    private EditText name;
    private EditText lastName;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private TextView submit;
    private Spinner countryList;
    private ArrayList<String> names;

    public NewAccountDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_newuser);
        initViews();
    }

    private void initViews() {
        name = findViewById(R.id.newuser_EDT_firstName);
        lastName = findViewById(R.id.newuser_EDT_lastName);
        email = findViewById(R.id.newuser_EDT_email);
        password = findViewById(R.id.newuser_EDT_password);
        confirmPassword = findViewById(R.id.newuser_EDT_confirmPassword);
        submit = findViewById(R.id.newuser_LBL_submit);
        countryList = findViewById(R.id.newuser_LST_countrySpinner);
        exitBtn = findViewById(R.id.newuser_BTN_exit);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        populateCountryList();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForValidInputs();
            }
        });

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

    /**
     * A method to check for valid inputs
     */
    private void checkForValidInputs() {
        Log.d(TAG, "checkForValidInputs: checking for valid input");


        if (name.getText().toString().equals("")) {
            Log.d(TAG, "checkForValidInputs: first name invalid");
            name.setError("Please enter a name");
            return;
        }

        if (lastName.getText().toString().equals("")) {
            Log.d(TAG, "checkForValidInputs: last name invalid");
            lastName.setError("Please enter a last name");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Log.d(TAG, "checkForValidInputs: Email invalid");
            email.setError("Please enter a valid email address");
            return;
        }

        if (password.getText().toString().equals("") || password.getText().toString().length() < 6) {
            if (password.getText().toString().length() < 6) {
                Log.d(TAG, "checkForValidInputs: short password");
                password.setError("Please enter at least 6 characters");
                return;
            } else {
                Log.d(TAG, "checkForValidInputs: invalid password");
                password.setError("Please enter a password");
                return;
            }
        }

        if (confirmPassword.getText().toString().equals("")) {
            Log.d(TAG, "checkForValidInputs: confirm invalid");
            confirmPassword.setError("Please confirm password");
            return;
        }

        if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Log.d(TAG, "checkForValidInputs: Passwords doesnt match");
            confirmPassword.setText("");
            confirmPassword.setError("Passwords didn't match!");
            return;
        }
        if (countryList.getSelectedItem().toString().equalsIgnoreCase("Country")) {
            ((TextView) countryList.getSelectedView()).setError("Please select a country");
            return;
        }

        //If all is good send user
        String fname = name.getText().toString().substring(0, 1).toUpperCase() + name.getText().toString().substring(1);
        String lname = lastName.getText().toString().substring(0, 1).toUpperCase() + lastName.getText().toString().substring(1);
        String mEmail = email.getText().toString();
        String userPassword = password.getText().toString();
        String mCountry = countryList.getSelectedItem().toString();
        Log.d(TAG, "After checks: fname: " + fname + " lname: " + lname + " email: "
                + mEmail + " password: " + userPassword + " country: " + mCountry);

        MyUser temp = new MyUser(fname, lname, mEmail, userPassword, mCountry
                , "", "", 0);

        getCountryStuff(temp);
    }

    /**
     * A method to get country currency code, currenct rate and country code
     */
    private void getCountryStuff(MyUser mUser) {
        Log.d(TAG, "getCountryStuff: got user: " + mUser.toString());
        Log.d(TAG, "getCountryStuff: Getting the country details");

        try {
            String countryCodeString = readJsonFromRaw(R.raw.countries);
            String currencyCodeString = readJsonFromRaw(R.raw.currency);
            JSONArray countryCodeJson = new JSONArray(countryCodeString);
            JSONObject currencyCodeJson = new JSONObject(currencyCodeString);
            for (int i = 0; i < countryCodeJson.length(); i++) {
                JSONObject temp = (JSONObject) countryCodeJson.get(i);
                if (temp.get("name").toString().equalsIgnoreCase(mUser.getCountry())) {
                    Log.d(TAG, "getCountryStuff: Found country name: " + mUser.getCountry());
                    mUser.setCountryCode(temp.get("code").toString());
                    Log.d(TAG, "getCountryStuff: Set country code: " + mUser.getCountryCode());
                }
            }
            mUser.setCurrencyCode(currencyCodeJson.get(mUser.getCountryCode()).toString());
            Log.d(TAG, "getCountryStuff: Got currency code: " + mUser.getCurrencyCode());
            callBackNewUser(mUser);
        } catch (IOException | JSONException e) {
            Log.d(TAG, "countryCodeToCurrencyCode: Couldnt parse json from raw: " + e.getMessage());
        }
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
     * A method to callBack events array to main activity
     */
    private void callBackNewUser(MyUser user) {
        Log.d(TAG, "setOnDismissListener: Dismissing calendar dialog");
        /** Callback days list and name */
        NewUserDetailsCallback newUserDetailsCallback;
        try {
            newUserDetailsCallback = (NewUserDetailsCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement dialog listener");
        }


        // Send the trip Dates and trip name to main layout
        newUserDetailsCallback.getNewUser(user);
        dismiss();
    }


}
