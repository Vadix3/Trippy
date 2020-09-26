package com.example.trippy.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blongho.country_data.World;
import com.bumptech.glide.Glide;
import com.example.trippy.Dialogs.NewCountryDialog;
import com.example.trippy.Dialogs.NewTripDialog;
import com.example.trippy.Fragments.CalendarFragment;
import com.example.trippy.Fragments.CurrencyFragment;
import com.example.trippy.Fragments.TranslatorFragment;
import com.example.trippy.Fragments.WeatherFragment;
import com.example.trippy.Interfaces.OnCalendarDialogDismissedListener;
import com.example.trippy.Interfaces.OnNewTripCallbackListener;
import com.example.trippy.Interfaces.OnSelectedCountryListener;
import com.example.trippy.Objects.MyContainer;
import com.example.trippy.Objects.MyEvent;
import com.example.trippy.Objects.MyTrip;
import com.example.trippy.Objects.MyUser;
import com.example.trippy.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


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
import java.util.Collections;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

//TODO:Deal with no internet problems. dont make user wait for response from server.
public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , OnNewTripCallbackListener, OnCalendarDialogDismissedListener, OnSelectedCountryListener
        , NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "pttt";
    private static final String NEW_TRIP_DIALOG = "newTripDialog";
    private static final String OPEN_NAVIGATION = "navigationDialog";
    private static final String OPEN_TRANSLATOR = "openTranslator";

    public static final int EMAIL_LOGIN = 0;
    public static final int GOOGLE_LOGIN = 1;
    public static final int FACEBOOK_LOGIN = 2;


    private Toast toast;
    /**
     * Views
     */
    //Layouts
    private DrawerLayout mainDrawerLayout;
    private RelativeLayout mainLayout;
    private LinearLayout currencyWeatherLayout;
    private DrawerLayout drawerLayout;

    //Cards
    private MaterialCardView currencyCard;
    private WeatherFragment weatherFragment;
    private CurrencyFragment currencyFragment;


    //TextViews
    private TextView welcomeLabel;

    //ImageViews
    private ImageView countryPhoto;


    //Other
    private MaterialToolbar materialToolbar;
    private NavigationView navigationView;
    /**
     * Location
     */
    private FusedLocationProviderClient fusedLocationProviderClient; // Fetching location
    private Location mLastKnownLocation; // Last known location of the device
    private LocationCallback locationCallback; // Updating users request if last known location is null
    private LatLng myLocationLatLng;
    private MyTrip myCurrentTrip;
    private MyUser myUser;
    private MyContainer myContainer;

    /**
     * Variables
     */


    /**
     * Login stuff
     */
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MaterialProgressBar loadingBar;

    private Snackbar snackbar;

    @Override
    protected void onResume() {
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.nav_home);
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_LAY_mainlayout);
        makeSnackbar("Loading data", R.color.colorPrimary);
        initAdStuff();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        getUserDataFromLogin(getIntent().getIntExtra("loginCode", 0));
    }

    /**
     * A method to get the users name and email from login
     */
    private void getUserDataFromLogin(int loginCode) {
        //FireBase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        //Login
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        String userEmail = "";
        String displayName = "";
        /** If the user logged in with email than the details are in firebase already.
         *  if the user logged in using google or facebook, he may log in without the data
         *  being in firestore.
         *  Therefore we need to extract display name and email.
         *  if the email exists in firestore, get data.
         *  if not, prompt user to set the country, add to the user object and save in firestore.
         */
        switch (loginCode) {
            case EMAIL_LOGIN:
                displayName = user.getDisplayName();
                userEmail = user.getEmail();
                Log.d(TAG, "getUserDataFromLogin: " + userEmail
                        + " Logged in with password");
                break;
            case GOOGLE_LOGIN:
                displayName = googleAccount.getDisplayName();
                userEmail = googleAccount.getEmail();
                Log.d(TAG, "getUserDataFromLogin: Name: " + displayName + " Email:" + userEmail
                        + " Logged in with Google");
                break;
            case FACEBOOK_LOGIN:
                displayName = getIntent().getStringExtra("name");
                userEmail = getIntent().getStringExtra("email");
                Log.d(TAG, "getUserDataFromLogin: Name: " + displayName + " Email:" + userEmail
                        + " Logged in with Facebook");
                break;
        }
        fetchUserFromFirestore(displayName, userEmail);
    }

    /**
     * Fetch the user using email from firestore
     */
    private void fetchUserFromFirestore(String displayName, String userEmail) {
        Log.d(TAG, "fetchUserFromFirestore: Fetching user from " + "users/" + userEmail);
        DocumentReference docRef = db.document("users/" + userEmail);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "onSuccess: user exists!");
                    myContainer = documentSnapshot.toObject(MyContainer.class);
                    myCurrentTrip = myContainer.getMyTrip();
                    myUser = myContainer.getMyUser();
                    Log.d(TAG, "onSuccess: Got user: " + myUser.toString());
                    isLocationEnabled();
                    initViews();
                    World.init(getApplicationContext());
                    if (myCurrentTrip != null) {
                        Log.d(TAG, "onSuccess: myCurrentTrip: " + myCurrentTrip.toString());
                    }
                } else {
                    Log.d(TAG, "onSuccess: Document does not exist! Prompt enter country");
                    getUserCountry(displayName, userEmail);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Exception: " + e.getMessage());
            }
        });
    }

    /**
     * A user chose to log in using facebook / google, and the user does not exist in firestore.
     * I can get the users name, lastname and email, But I need the users country to complete the
     * user object. This method will ask the user to select country of origin to complete object.
     * The method will get the country & currency codes accordingly.
     * =
     */
    private void getUserCountry(String displayName, String userEmail) {
        Log.d(TAG, "getUserCountry: Asking user to enter country");

        String arr[] = displayName.split(" ", 2);

        String tempName = arr[0];
        String tempLastName = arr[1];

        myUser = new MyUser(tempName, tempLastName, userEmail, "", "Counry"
                , "CountryCode", "CurrencyCode", 0);


        NewCountryDialog newCountryDialog = new NewCountryDialog(this);
        newCountryDialog.show();
        //TODO: Make new country dialog not cancellable
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.4);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        newCountryDialog.getWindow().setLayout(width, height);
        newCountryDialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        newCountryDialog.getWindow().setDimAmount(0.9f);
    }

    /**
     * A method to init Views
     */
    private void initViews() {
        Log.d(TAG, "initViews: Initing views");
        mainDrawerLayout = findViewById(R.id.main_LAY_mainDrawerlayout);
        mainDrawerLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Log.d(TAG, "onFocusChange: Main layout focused");
                }
            }
        });

        // Progress bar
        loadingBar = findViewById(R.id.main_BAR_progressBar);
        loadingBar.setIndeterminate(true);

        currencyWeatherLayout = findViewById(R.id.main_LAY_currencyWeatherLayout);
        currencyWeatherLayout.setVisibility(View.GONE);
        drawerLayout = findViewById(R.id.main_LAY_mainDrawerlayout);
        navigationView = findViewById(R.id.main_NAV_navigationView);
        welcomeLabel = findViewById(R.id.main_LBL_welcomeTo);
        materialToolbar = findViewById(R.id.main_LAY_MaterialToolBar);
        countryPhoto = findViewById(R.id.main_IMG_countryPhoto);
        setToolbarStuff();
    }

    /**
     * A method to initialize the ad stuff
     */
    private void initAdStuff() {
        Log.d(TAG, "initAdStuff: Initing add stuff");
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "onInitializationComplete: Ad init complete");
            }
        });

        AdView adView = new AdView(this);
        adView = findViewById(R.id.main_AD_adview);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "onAdLoaded: ad loaded");
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.d(TAG, "onAdFailedToLoad: ad failed to load: " + adError.toString());
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                Log.d(TAG, "onAdOpened: ad opened");
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "onAdClicked: ad clicked");
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "onAdLeftApplication: ad left app");
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "onAdClosed: ad closed");
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    /**
     * A method to initialize the toolbar options
     */
    private void setToolbarStuff() {
        Log.d(TAG, "setToolbarStuff: Creating toolbar options");
        navigationView.bringToFront();
        materialToolbar.setTitle("Hello " + myUser.getFirstMame() + "!");
        setSupportActionBar(materialToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, materialToolbar
                , R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    /**
     * A method to initialize the translator fragment
     */
    private void initTranslatorFragment() {
        Log.d(TAG, "initTranslatorFragment: Creating translator fragment");
        TranslatorFragment translatorFragment = new TranslatorFragment(myCurrentTrip.getCountryCode());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_LAY_translate, translatorFragment);
        transaction.commit();
        loadingBar.setIndeterminate(false);
        loadingBar.setVisibility(View.GONE);
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
        //Khabarovsk
//        myLocationLatLng = new LatLng(48.4814, 135.0721);
        //Prague
//        myLocationLatLng = new LatLng(50.0755, 14.4378);
        //Berlin
//        myLocationLatLng = new LatLng(52.5200, 13.4050);
        //Rio
        myLocationLatLng = new LatLng(-22.908333, -43.196388);
        //BangKok
//        myLocationLatLng = new LatLng(13.7563, 100.5018);
        //Marseilles
//        myLocationLatLng = new LatLng(43.2965, 5.3698);
        //Barcelona
//        myLocationLatLng = new LatLng(41.3851, 2.1734);
        //Real location
//        myLocationLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        Log.d(TAG, "updateUItoMatchLocation: location: " + myLocationLatLng.toString());
        makeSnackbar("Getting country", R.color.colorPrimary);

        initMyCurrentLocation();
        setFlagImage(myCurrentTrip.getCountryCode());
        setCityImage(myCurrentTrip.getCity());
        welcomeLabel.setText("" + myCurrentTrip.getCity() + ", " + myCurrentTrip.getCountry());

        /** After we are done with location, find currency*/
        Log.d(TAG, "updateUItoMatchLocation: USD currency, convert my currency to usd");
        getCurrency();
    }

    /**
     * A method to set image of the city
     */
    private void setCityImage(String city) {
        Log.d(TAG, "setCityImage: Setting city image for: " + city);
        String url = "https://pixabay.com/api/?key=" + getString(R.string.pixabay_api_key)
                + "&q=" + city + "&image_type=photo";
        Log.d(TAG, "setCityImage: Requesting: " + url);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: Exception: " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String initialResponse = response.body().string();
                Log.d(TAG, "onResponse: Got response: " + initialResponse);
                try {
                    JSONObject all = new JSONObject(initialResponse);
                    JSONArray array = (JSONArray) all.get("hits");
                    if (array.length() == 0) {
                        Log.d(TAG, "onResponse: Didn't find any images for the city, searching country images");
                        setCityImage(myCurrentTrip.getCountry());
                    } else {
                        JSONObject urlContainer = (JSONObject) array.get(0);
                        String imageUrl = (String) urlContainer.get("largeImageURL");
                        Log.d(TAG, "onResponse: Image url: " + imageUrl);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(countryPhoto).load(imageUrl).into(countryPhoto);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "onResponse: Json exception: " + e.getMessage());
                }

            }
        });
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
            if (myCurrentTrip == null) {
                myCurrentTrip = new MyTrip(city, country, countryCode, currencyCode, currencyRate
                        , null, null, null);
            }
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


        ImageView flagImageLeft = findViewById(R.id.main_IMG_flagImageLeft);
        Glide.with(flagImageLeft).load(flag).into(flagImageLeft);
        flagImageLeft.setImageResource(flag);
        flagImageLeft.setVisibility(View.VISIBLE);

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
        makeSnackbar("Getting currency", R.color.colorPrimary);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getString(R.string.currency_converter_new_url))
                .header("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: Request failed:" + e.getMessage());
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currencyFragment = new CurrencyFragment(myUser.getCurrencyRate()
                                , 0, myUser.getCurrencyCode()
                                , "N/A");
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.main_LAY_currency, currencyFragment);
                        transaction.commit();
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
                    String src = "";
                    String dst = "";
                    if (!myUser.getCurrencyCode().equalsIgnoreCase("usd")) {
                        Log.d(TAG, "onResponse: My currency is not usd");
                        src = getValueFromJsonString(responseString
                                , myUser.getCurrencyCode(), "rates");
                        myUser.setCurrencyRate(Float.parseFloat(src));

                    } else {
                        Log.d(TAG, "onResponse: My currency is usd");
                        myUser.setCurrencyRate(1f);
                    }


                    if (!myCurrentTrip.getCurrencyCode().equalsIgnoreCase("usd")) {
                        Log.d(TAG, "onResponse: Target currency is not usd");
                        dst = getValueFromJsonString(responseString
                                , myCurrentTrip.getCurrencyCode(), "rates");
                        myCurrentTrip.setCurrencyRate(Float.parseFloat(dst));
                    } else {
                        Log.d(TAG, "onResponse: Target currency is usd");
                        myCurrentTrip.setCurrencyRate(1f);
                    }

                    Log.d(TAG, "onResponse: " + myUser.getCurrencyCode() + ": " + myUser.getCurrencyRate()
                            + " " + myCurrentTrip.getCurrencyCode() + ": " + myCurrentTrip.getCurrencyRate());


                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currencyFragment = new CurrencyFragment(myUser.getCurrencyRate()
                                    , myCurrentTrip.getCurrencyRate(), myUser.getCurrencyCode()
                                    , myCurrentTrip.getCurrencyCode());
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.main_LAY_currency, currencyFragment);
                            transaction.commit();
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
        makeSnackbar("Getting weather", R.color.colorPrimary);
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + myCurrentTrip.getCity()
                + "&units=metric&appid=" + getString(R.string.open_weather_api_key);

        Log.d(TAG, "getLocationWeather: Requesting: " + url);
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
                        getMainLooper().prepare();
                        makeToast("Weather not available");
                        //TODO: Crashes here: "Can't create handler inside thread Thread"
                        weatherFragment = new WeatherFragment(null
                                , null, null, null);
                        FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                        transaction1.replace(R.id.main_LAY_weather, weatherFragment);
                        transaction1.commit();
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
                    JSONArray firstContainer = obj.getJSONArray("weather");
                    JSONObject first = (JSONObject) firstContainer.get(0);
                    JSONObject second = (JSONObject) obj.get("main");

                    String iconID = (String) first.get("icon");
                    String weatherDescription = (String) first.get("description");
                    String realTemp;
                    if (second.get("temp") instanceof Integer) {
                        realTemp = "" + Math.round((int) second.get("temp"));
                    } else {
                        realTemp = "" + Math.round((double) second.get("temp"));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            weatherFragment = new WeatherFragment(realTemp + "°C"
                                    , weatherDescription, iconID, myLocationLatLng);
                            FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                            transaction1.replace(R.id.main_LAY_weather, weatherFragment);
                            transaction1.commit();
                            loadingBar.setIndeterminate(false);
                            loadingBar.setVisibility(View.GONE);
                            checkForTripDetails();
                        }
                    });

                } catch (JSONException e) {
                    Log.d(TAG, "onResponseException: " + e.getMessage());
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            weatherFragment = new WeatherFragment(null
                                    , null, null, null);
                            FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                            transaction1.replace(R.id.main_LAY_weather, weatherFragment);
                            transaction1.commit();
                            loadingBar.setIndeterminate(false);
                            loadingBar.setVisibility(View.GONE);
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

        if (myCurrentTrip.getTripName() != null) {
            Log.d(TAG, "checkForTripDetails: Trip details available!");
            //There are trip details, init cards and save user to firestore
            welcomeLabel.setText("" + myCurrentTrip.getTripName());
            welcomeLabel.setGravity(Gravity.CENTER_HORIZONTAL);
            initCalendarFragment();
            initTranslatorFragment();
            currencyWeatherLayout.setVisibility(View.VISIBLE);
            if (snackbar != null) {
                snackbar.dismiss();
            }
        } else {
            //Trip details are not available, ask user to enter them
            Log.d(TAG, "checkForTripDetails: Trip details are unavailable!");
            createDialog(new NewTripDialog(MainActivity.this, getSupportFragmentManager()), NEW_TRIP_DIALOG);
        }
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
        if (snackbar != null) {
            snackbar.dismiss();
        }
        snackbar = Snackbar
                .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(getColor(color));
        snackbar.show();

        View view = snackbar.getView();
        TextView txtv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
        txtv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtv.setTextSize(15);
        txtv.setTypeface(null, Typeface.BOLD);
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
                height = (int) (getResources().getDisplayMetrics().heightPixels * 0.3);
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
            saveUserToFirestore();
        }
    }

    /**
     * A method to initialize the calendar fragment
     */
    private void initCalendarFragment() {
        Log.d(TAG, "initCalendarFragment: Creating calendar fragment with trip: " + myCurrentTrip.toString());
        CalendarFragment calendarFragment = new CalendarFragment(myCurrentTrip);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_LAY_calendar, calendarFragment);
        transaction.commit();
    }

    /**
     * Get the dates list and trip name from new trip dialog
     */
    @Override
    public void getResult(List<CalendarDay> tripDates, String tripName) {

        if (tripDates == null && tripName == null) {
            Log.d(TAG, "getResult: Null results, user did not want to enter trip details");
            initCalendarFragment();
            initTranslatorFragment();
            currencyWeatherLayout.setVisibility(View.VISIBLE);
            saveUserToFirestore();
            loadingBar.setIndeterminate(false);
            loadingBar.setVisibility(View.GONE);
            if (snackbar != null) {
                snackbar.dismiss();
            }
            return;
        }

        ArrayList<Long> convertedTripDates = new ArrayList<>();

        for (CalendarDay calendarDay : tripDates) {
            convertedTripDates.add(calendarDay.getDate().toEpochDay());
        }
        myCurrentTrip.setTripDates(convertedTripDates);
        myCurrentTrip.setTripName(tripName);
        Log.d(TAG, "getResult: My trip details: " + myCurrentTrip.toString());

        welcomeLabel.setText("" + tripName);
        welcomeLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        initCalendarFragment();
        initTranslatorFragment();
        currencyWeatherLayout.setVisibility(View.VISIBLE);
        loadingBar.setIndeterminate(false);
        loadingBar.setVisibility(View.GONE);
        if (snackbar != null) {
            snackbar.dismiss();
        }
        saveUserToFirestore();
    }


    /**
     * A method to get the selected country from user
     */
    @Override
    public void getSelectedCountry(String country) {
        Log.d(TAG, "getSelectedCountry: User selected country: " + country);
        myUser.setCountry(country);
        getCountryStuff(country);
    }

    /**
     * A method to get country details after user logged in using google or facebook
     */
    private void getCountryStuff(String country) {
        Log.d(TAG, "getCountryStuff: Got country to get stuff: " + country);
        try {
            String countryCodeString = readJsonFromRaw(R.raw.countries);
            String currencyCodeString = readJsonFromRaw(R.raw.currency);
            JSONArray countryCodeJson = new JSONArray(countryCodeString);
            JSONObject currencyCodeJson = new JSONObject(currencyCodeString);
            for (int i = 0; i < countryCodeJson.length(); i++) {
                JSONObject temp = (JSONObject) countryCodeJson.get(i);
                if (temp.get("name").toString().equalsIgnoreCase(country)) {
                    Log.d(TAG, "getCountryStuff: Found country name: " + country);
                    myUser.setCountryCode(temp.get("code").toString());
                    Log.d(TAG, "getCountryStuff: Set country code: " + myUser.getCountryCode());
                }
            }
            myUser.setCurrencyCode(currencyCodeJson.get(myUser.getCountryCode()).toString());
            Log.d(TAG, "getCountryStuff: Got currency code: " + myUser.getCurrencyCode());

        } catch (IOException | JSONException e) {
            Log.d(TAG, "countryCodeToCurrencyCode: Couldnt parse json from raw: " + e.getMessage());
        }
        isLocationEnabled();
        initViews();
        World.init(getApplicationContext());
    }


    /**
     * A method to save a new user that logged in using facebook or google to firestore
     */
    private void saveUserToFirestore() {
        Log.d(TAG, "saveNewUserToFirestore: Saving user: " + myUser.toString());
        String emailID = myUser.getEmailAddress();


        myContainer = new MyContainer(myUser, myCurrentTrip);


        db.collection("users")
                .document(emailID)
                .set(myContainer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: User saved successfully!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Exception: " + e.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: Item selected: " + item.getTitle());
        switch (item.getItemId()) {
            case R.id.nav_map:
                openMap();
                break;
            case R.id.nav_logout:
                logoutFromApp();
                break;
            case R.id.nav_share:
                makeToast("Opening share");
                break;
            case R.id.nav_rate:
                makeToast("Moving to rate");
                break;
            case R.id.nav_home:
                makeToast("Moving home");
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A method to log user out
     */
    private void logoutFromApp() {
        Log.d(TAG, "logoutFromApp: Logging out");

        //Logout firebase
        if (mAuth != null) {
            Log.d(TAG, "onStart: FIREBASE: User logged in");
            mAuth.signOut();
        }

        //Logout facebook
        LoginManager fb = LoginManager.getInstance();
        if (fb != null) {
            Log.d(TAG, "logoutFromApp: FACEBOOK: User logged in");
            fb.logOut();
        }

        //Logout google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d(TAG, "logoutFromApp: GOOGLE: User logged in");
            GoogleSignInOptions gso = new GoogleSignInOptions.
                    Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                    build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleSignInClient.signOut();
        }
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("LOGGED_OUT", 1);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {

    }
}

