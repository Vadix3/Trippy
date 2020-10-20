package com.example.trippy.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.example.trippy.Interfaces.NewEventCallback;
import com.example.trippy.Interfaces.NewUserDetailsCallback;
import com.example.trippy.Objects.MyEvent;
import com.example.trippy.Objects.MyLatLng;
import com.example.trippy.R;
import com.google.android.gms.maps.model.LatLng;
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
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class NewEventDialog extends Dialog {
    private static final String TAG = "pttt";


    private Context context;
    private EditText eventName;
    private EditText eventTime;
    private CheckBox alarm;
    private TextView cancel;
    private TextView save;
    private Spinner eventType;
    private MaterialSearchBar materialSearchBar;
    private List<AutocompletePrediction> predictionList; // Array to store predictions
    private Place place;


    private LatLng myLocation;
    private MyLatLng eventLocation;
    private String locationName = null;

    private boolean setAlarm = false;


    public NewEventDialog(@NonNull Context context, LatLng myLocation) {
        super(context);
        this.context = context;
        this.myLocation = myLocation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_newevent);
        Log.d(TAG, "onCreate: Creating new event dialog");
        initViews();
        initPlaces();
    }

    private void initViews() {
        Log.d(TAG, "initViews: ");
        eventName = findViewById(R.id.newevent_EDT_name);
        eventTime = findViewById(R.id.newevent_EDT_time);
        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initTimePicker();
            }
        });
        eventType = findViewById(R.id.newevent_LST_typeSpinner);
        initSpinner();
        materialSearchBar = findViewById(R.id.newevent_BAR_searchBar);

        alarm = findViewById(R.id.newevent_CHK_alarm);
        cancel = findViewById(R.id.newevent_LBL_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        save = findViewById(R.id.newevent_LBL_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserInput();
            }
        });
    }

    /**
     * A method to check the user input
     */
    private void checkUserInput() {
        Log.d(TAG, "checkUserInput: Checking user input");
        if (eventName.getText().toString().equals("")) {
            eventName.setError("Please enter event name");
            return;
        }
        if (eventTime.getText().toString().equals("")) {
            eventTime.setError("Please enter event time");
            return;
        }
        if (eventType.getSelectedItem().toString().equalsIgnoreCase("Type")) {
            ((TextView) eventType.getSelectedView()).setError("Please select event type");
            return;
        }
        if (alarm.isChecked()) {
            setAlarm = true;
        }
        callbackNewEvent();
    }

    private void callbackNewEvent() {
        Log.d(TAG, "callbackNewEvent");
        if (materialSearchBar.getText().equals("")) {
            Log.d(TAG, "callbackNewEvent: Search bar is empty, clearing my location");
            myLocation = null;
        }
        MyEvent event = new MyEvent(eventName.getText().toString(), eventTime.getText().toString()
                , eventType.getSelectedItem().toString(), eventLocation, 0L, locationName);
        Log.d(TAG, "callbackNewEvent: Sending my event: " + event.toString());

        /** Callback the event back to calendar activity */
        NewEventCallback newEventCallback;
        try {
            newEventCallback = (NewEventCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement dialog listener");
        }
        newEventCallback.getNewEvent(event, setAlarm);
        dismiss();
    }

    /**
     * A method to init event type spinner
     */
    private void initSpinner() {
        Log.d(TAG, "initSpinner: initing spinner");

        ArrayList<String> types = new ArrayList<>();
        types.add("Type");
        types.add("Travel");
        types.add("Meeting");
        types.add("Bar");
        types.add("Food");
        types.add("Attraction");
        types.add("Other");

        //create an ArrayAdapter from the String Array
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, types);
        //set the view for the Drop down list
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the ArrayAdapter to the spinner
        eventType.setAdapter(dataAdapter);
        //attach the listener to the spinner
        eventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: Selected: " + types.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * A method to initialize the timepicker
     */
    private void initTimePicker() {
        Log.d(TAG, "initTimePicker: initing timepicker");

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                eventTime.setText(String.format("%02d:%02d", hourOfDay, minutes));
            }
        }, currentHour, currentMinute, false);
        timePickerDialog.show();
    }

    /**
     * A method to initialize the places API and the search bar
     */
    private void initPlaces() {
        Log.d(TAG, "initPlaces: Initing the places api");
        Places.initialize(context, context.getString(R.string.google_maps_api_key));
        PlacesClient placesClient = Places.createClient(context);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                Log.d(TAG, "onSearchStateChanged");
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                Log.d(TAG, "onSearchConfirmed");
                Activity activity = (Activity) context;
                activity.startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                switch (buttonCode) {
                    case MaterialSearchBar.BUTTON_NAVIGATION:
                        Log.d(TAG, "onButtonClicked: Navigation button clicked");
                        break;
                    case MaterialSearchBar.BUTTON_BACK:
                        Log.d(TAG, "onButtonClicked: Back button pressed");
                        if (materialSearchBar.isSuggestionsVisible())
                            materialSearchBar.hideSuggestionsList();
                        break;
                }
            }
        });
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged: Before text changed");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "onTextChanged: New prediction request");
                // Create a new predictions request and pass it to the places client
                final FindAutocompletePredictionsRequest predictionsRequest
                        = FindAutocompletePredictionsRequest.builder()
                        .setOrigin(myLocation)
                        .setSessionToken(token)
                        .setLocationBias(RectangularBounds.newInstance(
                                new LatLng(myLocation.latitude, myLocation.longitude),
                                new LatLng(myLocation.latitude, myLocation.longitude)
                        ))
                        .setQuery(charSequence.toString())
                        .build();
                // Passing the request to places client
                Log.d(TAG, "onTextChanged: Passing the request to places client");
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(
                        new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: Task is successful");
                                    FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                                    if (predictionsRequest != null) {
                                        Log.d(TAG, "onComplete: prediction request is not null");
                                        // Put the predictions in a list
                                        predictionList = predictionsResponse.getAutocompletePredictions();
                                        List<String> suggestionsList = new ArrayList<>();
                                        for (int i = (predictionList.size() - 1); i >= 0; i--) {
                                            suggestionsList.add(predictionList.get(i).getFullText(null).toString());
                                        }
                                        Log.d(TAG, "onComplete: Predictions list ready: " + suggestionsList.toString());
                                        // The predictions list is complete, pass it the the search bar
                                        materialSearchBar.updateLastSuggestions(suggestionsList);
                                        if (!materialSearchBar.isSuggestionsVisible() && !suggestionsList.isEmpty())
                                            materialSearchBar.showSuggestionsList();
                                    } else {
                                        Log.d(TAG, "onComplete: Task failed! " + task.getException().toString());
                                    }
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
                if (position >= predictionList.size()) {
                    Log.d(TAG, "OnItemClickListener: position is greater than size");
                    materialSearchBar.clearSuggestions();
                    return;
                }
                // Fetch the prediction that user clicked
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                //Delay suggestions collapse so it wont show again
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);

                hideKeyboard((Activity) context);
                final String placeID = selectedPrediction.getPlaceId();
                //TODO: Check what parameters we need, for now its just the lat and lon
                List<Place.Field> placeField = Arrays.asList(Place.Field.values());

                //Create a new fetch place request using the place ID and the relevant fields
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeID, placeField).build();
                Log.d(TAG, "OnItemClickListener: Trying to fetch: " + placeID);
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        place = fetchPlaceResponse.getPlace();
                        locationName = place.getName();
                        eventLocation = new MyLatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                        Log.d(TAG, "onSuccess: " + place.getLatLng().toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

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
