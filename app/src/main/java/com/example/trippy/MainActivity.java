package com.example.trippy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blongho.country_data.World;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


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
import java.util.Collections;
import java.util.List;

//TODO:Deal with no internet problems. dont make user wait for response from server.
public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , OnNewTripCallbackListener, OnCalendarDialogDismissedListener {

    private static final String TAG = "pttt";
    private static final String NEW_TRIP_DIALOG = "newTripDialog";
    private static final String OPEN_NAVIGATION = "navigationDialog";
    private static final String OPEN_CALENDAR = "addCalendarEvent";
    private static final String OPEN_TRANSLATOR = "openTranslator";


    private Toast toast;
    /**
     * Views
     */
    //Layouts
    private RelativeLayout mainLayout;
    private RelativeLayout titleLayout;

    //Fragment
    private EventsListFragment eventsListFragment;

    //Buttons
    private MaterialButton openMapButton;
    private MaterialButton openDirectionButton;
    private MaterialButton openCalendarButton;
    private MaterialButton openTranslatorButton;

    //TextViews
    private TextView welcomeLabel;
    private TextView currencyLabel;
    private TextView weatherLabel;
    private TextView tripDatesLabel;
    //TODO: Convert to fine list
    private TextView tripEventsLabel;
    //ImageViews

    /**
     * Location
     */
    private FusedLocationProviderClient fusedLocationProviderClient; // Fetching location
    private Location mLastKnownLocation; // Last known location of the device
    private LocationCallback locationCallback; // Updating users request if last known location is null
    private LatLng myLocationLatLng;
    private MyTrip myCurrentTrip;

    /**
     * Variables
     */
    // If there are details saved on server / firebase
    private boolean tripDetailsAvailableToLoad = true;

    // If user loaded / created trip details
    private boolean tripDetailsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_LAY_mainlayout);
        makeSnackbar("Loading location data", R.color.coolBlue);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        isLocationEnabled();
        initViews();
        World.init(getApplicationContext());


    }

    /**
     * A method to init Views
     */
    private void initViews() {

        openMapButton = findViewById(R.id.main_BTN_openMap);
        openMapButton.setOnClickListener(this);
        openCalendarButton = findViewById(R.id.main_BTN_addCalenderEvent);
        openCalendarButton.setOnClickListener(this);
        openDirectionButton = findViewById(R.id.main_BTN_directions);
        openDirectionButton.setOnClickListener(this);
        openTranslatorButton = findViewById(R.id.main_BTN_Translator);
        openTranslatorButton.setOnClickListener(this);

        // Activate buttons before dialog
        openCalendarButton.setClickable(false);
        openMapButton.setClickable(false);
        openTranslatorButton.setClickable(false);
        openDirectionButton.setClickable(false);

        titleLayout = findViewById(R.id.main_LAY_titleLayout);
        welcomeLabel = findViewById(R.id.main_LBL_welcomeTo);
        currencyLabel = findViewById(R.id.main_LBL_currencyLabel);
        weatherLabel = findViewById(R.id.main_LBL_weather);
        tripDatesLabel = findViewById(R.id.main_LBL_tripdateslabel);


    }

    /**
     * A method to initialize the events list fragment
     */
    private void initEventsFragment() {
        eventsListFragment = new EventsListFragment(myCurrentTrip.getEvents());
        FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
        transaction1.replace(R.id.main_LAY_eventsList, eventsListFragment);
        transaction1.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_BTN_addCalenderEvent:
                openCalendar();
                break;
            case R.id.main_BTN_openMap:
                openMap();
                break;
            case R.id.main_BTN_directions:
                makeToast("Opening directions map");
                break;
            case R.id.main_BTN_Translator:
                openTranslator();
                break;
        }
    }

    /**
     * A method to open the translator activity
     */
    private void openTranslator() {
        createDialog(new TranslationDialog(MainActivity.this, myCurrentTrip.getCountryCode()), OPEN_TRANSLATOR);
    }

    /**
     * A method to open the caledar
     */
    private void openCalendar() {
        Log.d(TAG, "openCalendar: Open calendar pressed");
        if (myCurrentTrip.getTripDates() != null && myCurrentTrip.getEvents() != null) { // I have both arrays
            Log.d(TAG, "openCalendar: Sending events and dates");
            createDialog(new CalendarDialog(MainActivity.this, myCurrentTrip.getTripDates()
                    , myCurrentTrip.getEvents()), OPEN_CALENDAR);
            return;
        }
        if (myCurrentTrip.getTripDates() == null && myCurrentTrip.getEvents() == null) { // If I have nothing
            Log.d(TAG, "openCalendar: Sending nothing");
            createDialog(new CalendarDialog(MainActivity.this), OPEN_CALENDAR);
            return;
        }
        if (myCurrentTrip.getTripDates() == null) { // if I have only events array
            Log.d(TAG, "openCalendar: Sending only events array");
            createDialog(new CalendarDialog(MainActivity.this, null
                    , myCurrentTrip.getEvents()), OPEN_CALENDAR);
            return;
        }
        if (myCurrentTrip.getEvents() == null) { // I have only trip dates array
            Log.d(TAG, "openCalendar: Sending only tripDates array");
            createDialog(new CalendarDialog(MainActivity.this, myCurrentTrip.getTripDates()
                    , null), OPEN_CALENDAR);
            return;
        }
    }

    /**
     * A method to open the map activity
     */
    private void openMap() {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);

    }


    /** Location stuff*/

    /**
     * A method to check if location is enabled or not
     */
    public void isLocationEnabled() {
        Log.d(TAG, "isLocationEnabled: Checking if user enabled location services");
        // Open a new location request
        final LocationRequest locationRequest = openLocationRequest();
        // Pass the location request to the builder
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addAllLocationRequests(Collections.singleton(locationRequest));
        SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);
        // Check whether the location is on or not
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        //if location toggle is already enabled
        task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "onSuccess: Location is already enabled");
                getDeviceLocation();
            }
        });

        // Location is not toggled, but we can check if the issue can be resolved
        task.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try { // Try to resolve the issue
                        Log.d(TAG, "onFailure: Location not enabled, trying to resolve");
                        // Show the user a dialog where he can accept or not the location toggle
                        resolvableApiException.startResolutionForResult(MainActivity.this, 51);
                    } catch (IntentSender.SendIntentException ex) {
                        Log.d(TAG, "onFailure: NOT RESOLVABLE");
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * A method to open a new location request
     */
    private LocationRequest openLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    /**
     * A method to check what the user has decided to do regarding location toggle
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: check what user decided");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            // User has toggled location on
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: User enabled location");
                // Find users current location
                // Move the map to users current location
                getDeviceLocation();
            } else {
                Log.d(TAG, "onActivityResult: User decided not to turn on location");
            }
        }
    }

    /**
     * A method to fetch the device location
     * We ask the fusedLocationProviderClient to give us the last location.
     * When the request is complete, we check if the task was successful or not.
     * if it was successfull, it can still be null, so we check.
     * if it's not null, move the camera to the location
     * else, we have to create a location request, and a location callback.
     */
    @SuppressLint("MissingPermission") // Don't need, already did it on start
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting users location");
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) { // Got result
                if (task.isSuccessful()) {
                    mLastKnownLocation = task.getResult();
                    if (mLastKnownLocation != null) { // If the result is not null
                        Log.d(TAG, "onComplete: Result is not null");
                        updateUItoMatchLocation();
                    } else { // If the location is null, we need to request updated location
                        Log.d(TAG, "onComplete: Result is null, requesting location update");
                        //Request
                        final LocationRequest locationRequest = openLocationRequest();
                        //Callback
                        locationCallback = new LocationCallback() {
                            /** We tried to get the last location from the fusedLocationProvider,
                             it returned null so we need to create a location request
                             if the result is still null, we return. else we update our last known
                             location variable*/
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationResult == null) {
                                    Log.d(TAG, "onLocationResult: result is null, didnt get updated location");
                                    return;
                                }
                                Log.d(TAG, "onLocationResult: got updated location");
                                mLastKnownLocation = locationResult.getLastLocation();
                                // Remove the updates so we wont keep getting location updates.
                                updateUItoMatchLocation();
                                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest
                                , locationCallback, null);
                    }
                } else { // In case we were unable to get last known location
                    Toast.makeText(MainActivity.this, "Unable to get last location"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * A method to update the app with the users location
     */
    @SuppressLint("MissingPermission")
    private void updateUItoMatchLocation() {
        /** TODO: Fake latLng to check if it works. fake here
         * myLocationLatLng = new LatLng(fakeLatitude,fakeLongitude);
         * this should give the device a fake location
         * below a fake rio de janeiro location
         */
        //Knabarovsk
//        myLocationLatLng = new LatLng(48.4814, 135.0721);
        //Prague
//        myLocationLatLng = new LatLng(50.0755, 14.4378);
        //Berlin
        myLocationLatLng = new LatLng(52.5200, 13.4050);
        //Rio
//        myLocationLatLng = new LatLng(-22.908333, -43.196388);
        //BangKok
//        myLocationLatLng = new LatLng(13.7563, 100.5018);
        //Real location
//        myLocationLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        //Marseille
//        myLocationLatLng = new LatLng(43.2965,5.3698);
        Log.d(TAG, "updateUItoMatchLocation: location: " + myLocationLatLng.toString());

        initMyCurrentLocation();
        setFlagImage(myCurrentTrip.getCountryCode());
        welcomeLabel.setText("" + myCurrentTrip.getCity() + ", " + myCurrentTrip.getCountry());

        /** After we are done with location, find currency*/
        if (myCurrentTrip.getCurrencyCode().equalsIgnoreCase("eur")) { // The country is already EUR
            Log.d(TAG, "updateUItoMatchLocation: Currency already eur, no need to convert");
            currencyLabel.setText("Currency: EUR");
            getLocationWeather();
        } else
            getCurrency();
    }

    /**
     * A method to initialize MyTrip object
     */
    private void initMyCurrentLocation() {
        try {
            Geocoder geocoder = new Geocoder(this);
            Log.d(TAG, "updateUItoMatchLocation: Trying to get location from coordinates");
            List<Address> addresses = geocoder.getFromLocation(myLocationLatLng.latitude, myLocationLatLng.longitude, 1);
            Log.d(TAG, "initMyCurrentLocation: " + addresses.toString());
            String city = "";
            city = getCityNameByCoordinates(geocoder, myLocationLatLng.latitude, myLocationLatLng.longitude);
            String country = addresses.get(0).getCountryName();
            String countryCode = addresses.get(0).getCountryCode();
            String currencyCode = countryCodeToCurrencyCode(countryCode);
            float currencyRate = 0;
            myCurrentTrip = new MyTrip(city, country, countryCode, currencyCode, currencyRate
                    , null, null, null);
        } catch (IOException e) {
            Log.d(TAG, "updateUItoMatchLocation: Problem: " + e.getMessage());
        }
    }

    /**
     * A method to set the flag image according to the given country code
     */
    private void setFlagImage(String countryCode) {
        Log.d(TAG, "setFlagImage: Setting flag image to: " + countryCode);
        final int flag = World.getFlagOf(countryCode);
        //Set background to the given country image
//        ImageView backgroundImage = new ImageView(this);
//        backgroundImage.setImageAlpha(40);
//        backgroundImage.setImageResource(flag);
//        titleLayout.setBackground(backgroundImage.getDrawable());
        ImageView flagImage = findViewById(R.id.main_IMG_flagImage);
        flagImage.setImageResource(flag);
        flagImage.setVisibility(View.VISIBLE);
    }

    /**
     * A method to convert given country code to currency
     */
    private String countryCodeToCurrencyCode(String countryCode) {
        Log.d(TAG, "countryCodeToCurrencyCode: Converting country code to currency code");
        String currencyCode = "";
        String jsonString = "";

        try {
            jsonString = readJsonFromRaw(R.raw.currency);
        } catch (IOException e) {
            Log.d(TAG, "countryCodeToCurrencyCode: Couldnt parse json from raw: " + e.getMessage());
        }

        currencyCode = getValueFromJsonString(jsonString, countryCode, null);
        Log.d(TAG, "countryCodeToCurrencyCode: Got currency code: " + currencyCode + " from key: "
                + countryCode);
        return currencyCode;
    }

    /**
     * A method to return a String json from raw folder
     * id = R.raw.filename
     */
    private String readJsonFromRaw(int id) throws IOException {
        String res = "";
        InputStream is = getResources().openRawResource(id);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.d(TAG, "countryCodeToCurrencyCode: Cant convert to string " + e.getMessage());
        } finally {
            is.close();
        }

        return writer.toString();
    }

    /**
     * A method to return value from given JSON and key
     * param = in case there are multiple parts of the json
     */
    private String getValueFromJsonString(String jsonString, String key, String param) {
        Log.d(TAG, "getValueFromJsonString: Getting " + key + " with param " + param + " from " + jsonString);
        String val = "";
        try {
            JSONObject obj = new JSONObject(jsonString);
            if (param == null) {
                val = obj.getString(key);
            } else {
                JSONObject b = obj.getJSONObject(param);
                val = b.getString(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return val;
    }

    /**
     * A method to get currency and update label
     */
    private void getCurrency() {
        Log.d(TAG, "getCurrency: Opening http request to get currency");

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getString(R.string.currency_converter_url))
                .header("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: Request failed:" + e.getMessage());
                //TODO: Check for last currency if available
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currencyLabel.setText("Currency not available");
                        makeToast("Currency not available");
                    }
                });
                getLocationWeather();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG, "onResponse: Request successful");
                if (response == null) {
                    Log.d(TAG, "onResponse: Response is null");
                } else {
                    String responseString = response.body().string();
                    String tempCurrency = getValueFromJsonString(responseString
                            , myCurrentTrip.getCurrencyCode(), "rates");
                    Log.d(TAG, "onResponse: Got: " + tempCurrency);
                    myCurrentTrip.setCurrencyRate(Float.parseFloat(tempCurrency));
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currencyLabel.setText("1 EUR = " + myCurrentTrip.getCurrencyRate()
                                    + " " + myCurrentTrip.getCurrencyCode());
                        }
                    });
                }

                /** We are done with the currency, lets get the weather info*/
                getLocationWeather();
            }
        });

    }

    /**
     * A method to get the weather of current location
     */
    private void getLocationWeather() {
        Log.d(TAG, "getLocationWeather: Getting city weather");


        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + myCurrentTrip.getCity()
                + "&units=metric&appid=" + getString(R.string.open_weather_api_key);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: Request failed:" + e.getMessage());
                //TODO: Check previous weather
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        makeToast("Weather not available");
                        weatherLabel.setText("Weather not available");
                    }
                });
                checkForTripDetails();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String weatherString = response.body().string();
                Log.d(TAG, "onResponse: " + weatherString);

                try {
                    JSONObject obj = new JSONObject(weatherString);
                    JSONObject weatherDescription = (JSONObject) obj
                            .getJSONArray("weather").get(0);
                    JSONObject weatherTempArray = obj.getJSONObject("main");
                    Log.d(TAG, "onResponse: " + weatherDescription);
                    final String locationTemp = weatherTempArray.getString("temp");
                    final String locationWeatherDescription = weatherDescription.getString("description");
                    final String feelsLike = weatherTempArray.getString("feels_like");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int realIntTemp = (int) Float.parseFloat(locationTemp);
                            int feelsIntTemp = (int) Float.parseFloat(feelsLike);
                            if (realIntTemp != feelsIntTemp) { // If there is no difference
                                weatherLabel.setGravity(Gravity.CENTER_HORIZONTAL);
                                weatherLabel.setText(realIntTemp + "°C, feels like: "
                                        + feelsIntTemp
                                        + "°C\n" + locationWeatherDescription);
                            } else {
                                weatherLabel.setGravity(Gravity.CENTER_HORIZONTAL);
                                weatherLabel.setText(realIntTemp + "°C\n" + locationWeatherDescription);
                            }
                            makeSnackbar("Location data loaded!", R.color.coolGreen);

                            //TODO: Continue here after we got the weather details
                            checkForTripDetails();
                        }
                    });
                } catch (JSONException e) {
                    Log.d(TAG, "onResponse: Exception: " + e.getMessage());
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            weatherLabel.setGravity(Gravity.CENTER_HORIZONTAL);
                            weatherLabel.setText("");
                            makeSnackbar("Weather info anavailable", R.color.bpRed);
                            checkForTripDetails();
                        }
                    });
                }
            }
        });
    }

    /**
     * A method to check if trip details are available.
     * if there are, load them from SP.
     * else prompt user to enter them
     */
    private void checkForTripDetails() {
        Log.d(TAG, "checkForTripDetails: Checking if trip details are available");
        if (tripDetailsAvailableToLoad) {
            loadTripDetails();
            releaseButtons();
        } else {
            //Trip details are not available, ask user to enter them
            createDialog(new NewTripActivity(MainActivity.this), NEW_TRIP_DIALOG);
        }
    }

    /**
     * A method to load trip details from SP or server
     * TODO:Complete it once relevant, load type?
     */
    private void loadTripDetails() {
        Log.d(TAG, "loadTripDetails: Loading trip details from");
    }

    /**
     * There is a problem getting city details from google geocoder, this method gets the correct
     * city name
     */
    private String getCityNameByCoordinates(Geocoder mGeocoder, double lat, double lon) throws IOException {
        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 10);
        if (addresses != null && addresses.size() > 0) {
            for (Address adr : addresses) {
                if (adr.getLocality() != null && adr.getLocality().length() > 0) {
                    return adr.getLocality();
                }
            }
        }
        return null;
    }

    /** End of location stuff*/


    /*************************UTILS***********************/
    /**
     * A method to display a toast
     */
    private void makeToast(String message) {
        Log.d(TAG, "makeToast:" + message);
        if (toast != null)
            toast.cancel();

        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * A method to display a snackbar
     */
    private void makeSnackbar(String message, int color) {
        Log.d(TAG, "makeSnackbar: " + message);
        Snackbar snackbar = Snackbar
                .make(mainLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getColor(color));
        snackbar.show();
    }

    /**
     * A method to create and show a dialog
     */
    private void createDialog(final Dialog dialog, final String val) {
        Log.d(TAG, "createDialog: Creating dialog " + dialog.getClass().toString() + " With values: " + val);
        if (toast != null)
            toast.cancel();
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        int width = 0;
        int height = 0;
        switch (val) {
            case NEW_TRIP_DIALOG:
                height = (int) (getResources().getDisplayMetrics().heightPixels * 0.2);
                width = (int) (getResources().getDisplayMetrics().widthPixels * 0.7);
                break;
            case OPEN_CALENDAR:
                height = (int) (getResources().getDisplayMetrics().heightPixels * 0.45);
                width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                break;
            case OPEN_NAVIGATION:
                height = (int) (getResources().getDisplayMetrics().heightPixels * 0.55);
                width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                break;
            case OPEN_TRANSLATOR:
                height = (int) (getResources().getDisplayMetrics().heightPixels * 0.75);
                width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                break;
        }

        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }

        });
    }

    /**
     * A method to release the buttons
     */
    private void releaseButtons() {
        // Activate buttons after dialog
        openCalendarButton.setClickable(true);
        openMapButton.setClickable(true);
        openTranslatorButton.setClickable(true);
        openDirectionButton.setClickable(true);
    }


    /**
     * Update dates label according to received dates
     */
    private void updateTripDatesLabel() {
        //TODO: 1 day trip?
        Log.d(TAG, "updateTripDatesLabel: Updating trip dates to: " + myCurrentTrip.getTripDates());
        List<CalendarDay> tripDates = myCurrentTrip.getTripDates();
        /** Check if the trip year start and end are the same
         * check if trip month start and end are the same
         */
        CalendarDay startDay = tripDates.get(0);
        CalendarDay endDay = tripDates.get(tripDates.size() - 1);
        String dateText = "";
        if (startDay.getYear() != endDay.getYear()) { // Trip ending on different years
            dateText = startDay.getDay() + "/" + startDay.getMonth() + "/" + startDay.getYear() + " - "
                    + endDay.getDay() + "/" + endDay.getMonth() + "/" + endDay.getYear();
        } else {
            if (startDay.getMonth() != endDay.getMonth()) { // Trip ending on different months
                dateText = startDay.getDay() + "/" + startDay.getMonth() + " - "
                        + endDay.getDay() + "/" + endDay.getMonth();
            } else { // Trip in same month
                dateText = startDay.getDay() + " - " + endDay.getDay() + "/" + startDay.getMonth();
            }
        }
        tripDatesLabel.setText(dateText);
        tripDatesLabel.setVisibility(View.VISIBLE);
    }


    /** ================== CALLBACKS ================*/

    /**
     * Callback method to get events array from calendar dialog
     */
    @Override
    public void getEventsArray(ArrayList<MyEvent> tripEvents) {
        if (tripEvents == null) {
            Log.d(TAG, "getEventsArray: Got null events array");
        } else {
            Log.d(TAG, "getEventsArray: Got callback with: " + tripEvents.toString());
            myCurrentTrip.setEvents(tripEvents);

            //TODO: Display only closest event, do events button
            //TODO: Sort
            //TODO: list
            /**List fragment*/
            initEventsFragment();
        }
    }

    /**
     * Get the dates list and trip name from new trip dialog
     */
    @Override
    public void getResult(List<CalendarDay> tripDates, String tripName) {

        releaseButtons();

        if (tripDates == null && tripName == null) {
            Log.d(TAG, "getResult: Null results");
            return;
        }
        if (tripDates == null) {
            Log.d(TAG, "getResult: Null tripDates");
            return;
        }
        if (tripName == null) {
            Log.d(TAG, "getResult: Null tripName");
            return;
        }

        myCurrentTrip.setTripDates(tripDates);
        myCurrentTrip.setTripName(tripName);

        Log.d(TAG, "getResult: getting callback from new trip dialog with results:\n" +
                "tripDates: " + myCurrentTrip.getTripDates().toString()
                + "\ntripName: " + myCurrentTrip.getTripName());

        welcomeLabel.setText("" + tripName);
        welcomeLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        tripDetailsLoaded = true;

        updateTripDatesLabel();
    }
}

