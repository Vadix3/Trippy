package com.example.trippy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "pttt";


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
    private Button btnFind;
    private RippleBackground rippleBackground;

    private static final float DEFAULT_ZOOM = 15f;


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
        materialSearchBar = findViewById(R.id.contentMap_SBR_searchBar);
        btnFind = findViewById(R.id.contentMap_BTN_findButton);
        rippleBackground = findViewById(R.id.contentMap_RPL_ripple);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

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
        moveLocationButtonToBottom(); // Move location button to bottom of screen
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

                // Create a new predictions request and pass it to the places client
                final FindAutocompletePredictionsRequest predictionsRequest
                        = FindAutocompletePredictionsRequest.builder()
                        .setCountry("il")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
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
                                        for (AutocompletePrediction prediction : predictionList) {
                                            suggestionsList.add(prediction.getFullText(null).toString());
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
                List<Place.Field> placeField = Arrays.asList(Place.Field.LAT_LNG);

                // Create a new fetch place request using the place ID and the relevant fields
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeID, placeField).build();
                Log.d(TAG, "OnItemClickListener: Trying to fetch placeID: " + placeID + " lat and lng");
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.d(TAG, "onSuccess: Fetching successfull: " + place.getLatLng().toString());
                        LatLng placeLatLng = place.getLatLng();
                        if (placeLatLng != null) {
                            moveCamera(placeLatLng, DEFAULT_ZOOM);
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
        /** Find button will find whatever user wants to find from menu*/
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchForGivenPlacesAroundMe(view);
            }
        });

    }

    /**
     * A method to search what the user wanted to find
     */
    private void searchForGivenPlacesAroundMe(View view) {
        LatLng currentMarkerLocation = mMap.getCameraPosition().target; // center of map
        rippleBackground.startRippleAnimation();
        //TODO: API CALL
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rippleBackground.stopRippleAnimation();
            }
        }, 3000);
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
    private void moveLocationButtonToBottom() {
        Log.d(TAG, "moveLocationButtonToBottom: Moving location button to bottom");

        // Fetch the layout params of the location button
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1"))
                    .getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ABOVE, btnFind.getId());
            layoutParams.setMargins(0, 0, 40, 300);
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
}