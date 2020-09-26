package com.example.trippy.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;

import com.example.trippy.Dialogs.PlaceDetailsDialog;
import com.example.trippy.Fragments.SearchTypeFragment;
import com.example.trippy.Interfaces.OnSearchTypeSelectedListener;
import com.example.trippy.Objects.MyMarker;
import com.example.trippy.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "pttt";
    private FirebaseAuth mAuth;

    /**
     * Map stuff
     */
    private GoogleMap mMap; // Map
    private FusedLocationProviderClient fusedLocationProviderClient; // Fetching location
    private PlacesClient placesClient; // Loading suggestions
    private List<AutocompletePrediction> predictionList; // Array to store predictions
    private Location mLastKnownLocation; // Last known location of the device
    private LocationCallback locationCallback; // Updating users request if last known location is null

    /**
     * Views
     */
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private RippleBackground rippleBackground;
    private ImageView centerMarker;
    private FrameLayout searchTypeLayout;
    private static final float DEFAULT_ZOOM = 15f;

    private String mySearchType = "";

    //Other
    private MaterialToolbar materialToolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    /**
     * A method to store marker with place
     */
    private ArrayList<MyMarker> myMarkers;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Creating layout");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViews();
        initMapFragment();

    }

    /**
     * A method to create the map fragment
     */
    private void initMapFragment() {
        Log.d(TAG, "initMapFragment: Inflating map fragment");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentMap_FRG_map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
    }

    /**
     * A method to initialize the views
     */
    private void initViews() {
        Log.d(TAG, "initViews: Initializing views");

        drawerLayout = findViewById(R.id.map_LAY_mainDrawerlayout);
        materialToolbar = findViewById(R.id.map_LAY_MaterialToolBar);
        navigationView = findViewById(R.id.map_NAV_navigationView);

        materialSearchBar = findViewById(R.id.contentMap_SBR_searchBar);
        rippleBackground = findViewById(R.id.contentMap_RPL_ripple);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        centerMarker = findViewById(R.id.contentMap_IMG_pin);

        OnSearchTypeSelectedListener onSearchTypeSelectedListener = new OnSearchTypeSelectedListener() {
            @Override
            public void setSearchType(String searchType) {
                Log.d(TAG, "setSearchType Callback: " + searchType);
                mySearchType = searchType;

                searchForGivenPlacesAroundMe();

                /**
                 style="@style/Widget.MaterialComponents.Button.TextButton.Icon"
                 app:strokeColor="@color/colorPrimary"
                 app:strokeWidth="1dp"
                 */
            }
        };

        searchTypeLayout = findViewById(R.id.contentMap_LAY_buttonsFragment);
        SearchTypeFragment searchTypeFragment = new SearchTypeFragment();
        searchTypeFragment.setActivityCallBack(onSearchTypeSelectedListener);
        FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
        transaction1.replace(R.id.contentMap_LAY_buttonsFragment, searchTypeFragment);
        transaction1.commit();
        myMarkers = new ArrayList<>();

        setToolbarStuff();
    }


    /**
     * A method to initialize the toolbar options
     */
    private void setToolbarStuff() {
        Log.d(TAG, "setToolbarStuff: Creating toolbar options");
        navigationView.bringToFront();
        materialToolbar.setTitle("Search places nearby!");
        setSupportActionBar(materialToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, materialToolbar
                , R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_map);

//        Menu menu = navigationView.getMenu();
//        menu.findItem(R.id.nav_profile).setVisible(false);
    }

    //I dont need to ask for location because of the start permission check
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Preparing map");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true); //
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Init places
        initPlaces();
        moveMapButtons(); // Move location button to bottom of screen
        isLocationEnabled(); // Check for location toggle
    }

    /**
     * A method to initialize the places API
     */
    private void initPlaces() {
        Log.d(TAG, "initPlaces: Initializing the places api");
        Places.initialize(MapActivity.this, getString(R.string.google_maps_api_key));
        placesClient = Places.createClient(this);
        // New autocomplete token
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                Log.d(TAG, "onSearchStateChanged");

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                Log.d(TAG, "onSearchConfirmed");
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                switch (buttonCode) {
                    case MaterialSearchBar.BUTTON_NAVIGATION:
                        Log.d(TAG, "onButtonClicked: button navigation");
                        //opening or closing a navigation drawer
                        break;
                    case MaterialSearchBar.BUTTON_BACK:
                        if (materialSearchBar.isSuggestionsVisible())
                            materialSearchBar.hideSuggestionsList();
                }

            }
        });
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "onTextChanged: new prediction request");
                centerMarker.setVisibility(View.VISIBLE);
                LatLng currentMarkerLocation = mMap.getCameraPosition().target; // center of map
                double tempLat = currentMarkerLocation.latitude;
                double tempLon = currentMarkerLocation.longitude;

                // Create a new predictions request and pass it to the places client
                final FindAutocompletePredictionsRequest predictionsRequest
                        = FindAutocompletePredictionsRequest.builder()
                        .setOrigin(currentMarkerLocation)
                        .setSessionToken(token)
                        .setLocationBias(RectangularBounds.newInstance(
                                new LatLng(tempLat, tempLon),
                                new LatLng(tempLat, tempLon)))
                        .setQuery(charSequence.toString())
                        .build();

                // Passing the request to the places client
                Log.d(TAG, "onTextChanged: Passing the request to the places client");
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(
                        new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                                if (task.isSuccessful()) {
                                    // The task is successfull, find what are the suggestions
                                    Log.d(TAG, "onComplete: Successful task");
                                    FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                                    if (predictionsRequest != null) {
                                        Log.d(TAG, "onComplete: prediction responese is not null");
                                        // Put the predictions in our predictions list
                                        predictionList = predictionsResponse.getAutocompletePredictions();
                                        List<String> suggestionsList = new ArrayList<>();
                                        Log.d(TAG, "onComplete: Converting predictions to strings");
                                        for (int i = (predictionList.size() - 1); i >= 0; i--) {
                                            suggestionsList.add(predictionList.get(i).getFullText(null).toString());
                                        }
                                        Log.d(TAG, "Ready predictions list: " + suggestionsList.toString());
                                        // List is complete, pass it to material search bar
                                        materialSearchBar.updateLastSuggestions(suggestionsList);
                                        if (!materialSearchBar.isSuggestionsVisible() && !suggestionsList.isEmpty())
                                            materialSearchBar.showSuggestionsList();
                                    }
                                } else {
                                    Log.d(TAG, "onComplete: Task failed: " + task.getException()
                                            .toString());
                                }
                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        // Set a suggestion click listener
        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                Log.d(TAG, "OnItemClickListener: Item clicked");
                /** We need the lat and long for the selected entry.
                 *  We have the place ID of the selected entry.
                 *  We will take the place ID and send it to places API.
                 *  Request the lat and long.
                 *  move camera to lat and long.
                 */
                if (position >= predictionList.size()) {
                    Log.d(TAG, "OnItemClickListener: Position is greater than the size");
                    materialSearchBar.clearSuggestions();
                    return;
                }
                // Fetch the prediction that was clicked by the user
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                // Slightly delay the suggestions collapse so it wont show again
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);

                hideKeyboard(MapActivity.this);

                final String placeID = selectedPrediction.getPlaceId();

                // We are interested only in the lat and lng attributes of the place
                List<Place.Field> placeField = Arrays.asList(Place.Field.values());

                // Create a new fetch place request using the place ID and the relevant fields
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeID, placeField).build();
                Log.d(TAG, "OnItemClickListener: Trying to fetch placeID: " + placeID + " lat and lng");
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.d(TAG, "onSuccess: Fetching successfull: " + place.getLatLng().toString());
                        if (place != null) {
                            mySearchType = "default";
                            centerMarker.setVisibility(View.INVISIBLE);
                            addMarkerToMap(place);
                            moveCamera(place.getLatLng(), DEFAULT_ZOOM);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    // Failed to fetch the location
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.d(TAG, "onFailure: Fetching failed: " + e.getMessage());
                        }
                    }
                });


            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }

    /**
     * A method to search what the user wanted to find
     */
    private void searchForGivenPlacesAroundMe() {
        Log.d(TAG, "searchForGivenPlacesAroundMe: Ripple animation");
        centerMarker.setVisibility(View.VISIBLE);
        LatLng currentMarkerLocation = mMap.getCameraPosition().target; // center of map
        rippleBackground.startRippleAnimation();
        openHttpRequestForPlaces(currentMarkerLocation);
    }

    /**
     * A method to open HTTP request for places around me
     */
    private void openHttpRequestForPlaces(LatLng currentMarkerLocation) {
        Log.d(TAG, "openHttpRequestForPlaces: Searching for places around" +
                currentMarkerLocation.toString());

        int myRadius = 1500;
        String placeType = mySearchType;
        String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
        String tempLocation = "" + currentMarkerLocation.latitude + "," + "" + currentMarkerLocation.longitude;
        String tempRadius = "&radius=" + myRadius;
        String tempType = "&type=" + placeType;
        String apiKey = "&key=" + getString(R.string.google_maps_api_key);

        String url = baseUrl + tempLocation + tempRadius + tempType + apiKey;

        OkHttpClient okHttpClient = new OkHttpClient();
        Log.d(TAG, "openHttpRequestForPlaces: Requesting:\n" + url);
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: Request failed:" + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MapActivity.this, "Couldn't fetch location, Please try again"
                                , Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG, "onResponse: Request successful");
                if (response == null) {
                    Log.d(TAG, "onResponse: Response is null");
                } else {
                    String responseString = response.body().string();
                    Log.d(TAG, "onResponse: success: " + responseString);
                    try {
                        JSONObject results = new JSONObject(responseString);
                        JSONArray resultsArray = results.getJSONArray("results");
                        ArrayList<JSONObject> placesJSON = new ArrayList<>();

                        ArrayList<String> placesIDs = new ArrayList<>();
                        ArrayList<Place> places = new ArrayList<>();

                        for (int i = 0; i < resultsArray.length(); i++) {
                            placesJSON.add((JSONObject) resultsArray.get(i));
                            placesIDs.add((String) placesJSON.get(i).get("place_id"));
                        }
                        myMarkers.clear(); // Clear markers array
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMap.clear();
                            }
                        });
                        for (String id : placesIDs) {
                            addPlaceToMap(id);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Slightly delay the suggestions collapse so it wont show again
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        rippleBackground.stopRippleAnimation();
                                    }
                                }, 3000);
                            }
                        });

                    } catch (JSONException e) {
                        Log.d(TAG, "onResponse: Exception: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * A method to add fetched place to map
     */
    private void addPlaceToMap(final String placeID) {
        // We are interested only in the lat and lng attributes of the place
        List<Place.Field> placeField = Arrays.asList(Place.Field.values());

        // Create a new fetch place request using the place ID and the relevant fields
        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeID, placeField).build();
        Log.d(TAG, "OnItemClickListener: Trying to fetch placeID: " + placeID);
        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                Place place = fetchPlaceResponse.getPlace();
                Log.d(TAG, "onSuccess: Fetching successfull: " + place.toString());

                addMarkerToMap(place);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            // Failed to fetch the location
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Fetching failed: " + e.getMessage());
            }
        });
    }

    /**
     * A method to add the right marker to the map according to place type
     */
    private void addMarkerToMap(Place place) {

        String restaurantIcon = "menu";
        String supermarketIcon = "cart";
        String barIcon = "alcohol";
        String busIcon = "travel";
        String atmIcon = "atm";
        String iconName = "";

        switch (mySearchType) {
            case "restaurant":
                iconName = restaurantIcon;
                break;
            case "supermarket":
                iconName = supermarketIcon;
                break;
            case "bar":
                iconName = barIcon;
                break;
            case "transit_station":
                iconName = busIcon;
                break;
            case "atm":
                iconName = atmIcon;
                break;
            default:
                iconName = "default";
                break;
        }
        int icWidth = 100;
        int icHeight = 100;
        Marker tempMarker = null;
        if (!iconName.equals("default")) { // Add marker from relative search
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources()
                    .getIdentifier(iconName, "drawable", getPackageName()));
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, icWidth, icHeight, false);

            MarkerOptions tempMarkerOptions = new MarkerOptions()
                    .position(place.getLatLng())
                    .title(place.getName())
                    .snippet("Tap for info")
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
            tempMarker = mMap.addMarker(tempMarkerOptions);

        } else { // Add regular marker for specific place search
            MarkerOptions tempMarkerOptions = new MarkerOptions()
                    .position(place.getLatLng())
                    .title(place.getName())
                    .snippet("Tap for info");
            tempMarker = mMap.addMarker(tempMarkerOptions);
        }
        myMarkers.add(new MyMarker(tempMarker, place));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d(TAG, "onInfoWindowClick: " + marker.getId());
                Place selectedPlace = findMarkerByID(marker.getId());
                Log.d(TAG, "onInfoWindowClick: Fetched place: " + selectedPlace.toString());
                PlaceDetailsDialog placeDetailsDialog = new PlaceDetailsDialog(MapActivity.this
                        , selectedPlace, placesClient, mySearchType);
                createNewPlaceDetailsDialog(placeDetailsDialog);
            }
        });
    }

    /**
     * Creates a new place details dialog
     */
    private void createNewPlaceDetailsDialog(PlaceDetailsDialog placeDetailsDialog) {
        Log.d(TAG, "createNewPlaceDetailsDialog: Creating new places dialog");
        placeDetailsDialog.show();
        placeDetailsDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT
                , WindowManager.LayoutParams.WRAP_CONTENT);
        placeDetailsDialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        placeDetailsDialog.getWindow().setDimAmount(0.9f);
    }

    /**
     * A method to find a place by marker ID
     */
    private Place findMarkerByID(String id) {
        Place place = null;
        for (MyMarker myMarker : myMarkers) {
            if (myMarker.getMarker().getId().equals(id)) {
                place = myMarker.getPlace();
            }
        }
        return place;
    }


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
        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);
        // Check whether the location is on or not
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        //if location toggle is already enabled
        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "onSuccess: Location is already enabled");
                getDeviceLocation();
            }
        });

        // Location is not toggled, but we can check if the issue can be resolved
        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try { // Try to resolve the issue
                        Log.d(TAG, "onFailure: Location not enabled, trying to resolve");
                        // Show the user a dialog where he can accept or not the location toggle
                        resolvableApiException.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException ex) {
                        Log.d(TAG, "onFailure: NOT RESOLVABLE");
                        ex.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (materialSearchBar.isSuggestionsVisible())
                    materialSearchBar.clearSuggestions();
                if (materialSearchBar.isSearchOpened())
                    materialSearchBar.closeSearch();
                return false;
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
                        moveCamera(new LatLng(mLastKnownLocation.getLatitude()
                                , mLastKnownLocation.getLongitude()), DEFAULT_ZOOM);
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
                                //Got the updated location, we move the camera to the updated location
                                moveCamera(new LatLng(mLastKnownLocation.getLatitude()
                                        , mLastKnownLocation.getLongitude()), DEFAULT_ZOOM);
                                // Remove the updates so we wont keep getting location updates.
                                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest
                                , locationCallback, null);
                    }
                } else { // In case we were unable to get last known location
                    Toast.makeText(MapActivity.this, "Unable to get last location"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * A method to move the location button the the bottom right of the screen
     */
    private void moveMapButtons() {
        Log.d(TAG, "moveLocationButtonToBottom: Moving location button to bottom");
        // Fetch the layout params of the location button
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1"))
                    .getParent()).findViewById(Integer.parseInt("2"));

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 0, 180);

            View toolbar = ((View) mapView.findViewById(Integer.parseInt("1")).
                    getParent()).findViewById(Integer.parseInt("4"));

            // and next place it, for example, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            rlp.addRule(RelativeLayout.LEFT_OF, 0);
            rlp.addRule(RelativeLayout.LEFT_OF, locationButton.getId());
            rlp.setMargins(100, 0, 100, 180);
        }
    }

    /**
     * A method to move the camera to the given location
     */
    private void moveCamera(LatLng location, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to " + location.latitude + " ; "
                + location.longitude);
        mMap.moveCamera(CameraUpdateFactory // Move the camera to the users location
                .newLatLngZoom(new LatLng(location
                        .latitude, location
                        .longitude), zoom));
    }

    /**
     * A method to hide the keyboard
     */
    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: Item selected: " + item.getTitle());
        switch (item.getItemId()) {
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
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

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
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("LOGGED_OUT", 1);
        startActivity(intent);
        finish();
    }

}