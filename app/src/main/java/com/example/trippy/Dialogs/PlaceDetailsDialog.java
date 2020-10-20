package com.example.trippy.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.example.trippy.Activities.MainActivity;
import com.example.trippy.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaceDetailsDialog extends Dialog {

    public static final String TAG = "pttt";
    private Context context;
    private TextView name;
    private TextView address;
    private ListView hours;
    private TextView rating;
    private ImageView photo;
    private ImageView exitBtn;

    private Place place;
    private PlacesClient placesClient;
    private String placeType;


    public PlaceDetailsDialog(@NonNull Context context) {
        super(context);
    }

    public PlaceDetailsDialog(@NonNull Context context, Place place, PlacesClient placesClient
            , String placeType) {
        super(context);
        Log.d(TAG, "PlaceDetailsDialog: " + place.toString());
        this.context = context;
        this.place = place;
        this.placesClient = placesClient;
        this.placeType = placeType;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: new Place details dialog dialog");
        setContentView(R.layout.dialog_place_details);
        initViews();
        displayPlaceDetails();
    }

    /**
     * Upadte place details
     */
    private void displayPlaceDetails() {
        Log.d(TAG, "displayPlaceDetails: Displaying place details: " + place.toString());
        if (place.getName() != null) {
            name.setText(place.getName());
        }
        if (place.getAddress() != null) {
            address.setText(place.getAddress());
        }
        if (placeType.equalsIgnoreCase("TRANSIT_STATION")
                || placeType.equalsIgnoreCase("ATM")) {
            hours.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) photo.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.placeDialog_LBL_placeRating);
            photo.setLayoutParams(params);
            rating.setVisibility(View.GONE);
        } else {
            getOpeningHours();
        }
        if (place.getRating() != null) {
            rating.setText("Rating: " + place.getRating().toString());
        } else {
            rating.setVisibility(View.GONE);
        }
        getPlacePhoto();
    }

    /**
     * A method to get the opening hours of the place
     */
    private void getOpeningHours() {
        ArrayList<String> sundayStartDays = new ArrayList<>();

        Log.d(TAG, "getOpeningHours: Getting opening hours");
        if (place.getOpeningHours() != null) {

            List<String> weekDays = place.getOpeningHours().getWeekdayText();

            sundayStartDays.add(weekDays.get(6)); // Start with sunday
            for (int i = 0; i < 6; i++) {
                sundayStartDays.add(weekDays.get(i)); // Add rest of days
            }
        } else {
            sundayStartDays.add("Opening hours unavailable");

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.hours_listview_row
                , sundayStartDays);
        hours.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    /**
     * A method to display place photo
     */
    private void getPlacePhoto() {
        Log.d(TAG, "getPlacePhoto: fetching place image");

        // create a ProgressDrawable object which we will show as placeholder
        CircularProgressDrawable drawable = new CircularProgressDrawable(context);
        drawable.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
        drawable.setCenterRadius(30f);
        drawable.setStrokeWidth(5f);
        // set all other properties as you would see fit and start it
        drawable.start();

        photo.setImageDrawable(drawable);

        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())

        // Get the photo metadata.
        final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
        if (metadata == null || metadata.isEmpty()) {
            Log.w(TAG, "No photo metadata.");
            photo.setVisibility(View.GONE);
            return;
        }
        final PhotoMetadata photoMetadata = metadata.get(0);

        // Get the attribution text.
        final String attributions = photoMetadata.getAttributions();

        // Create a FetchPhotoRequest.
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(300) // Optional.
                .setMaxHeight(300) // Optional.
                .build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Log.d(TAG, "getPlacePhoto: fetching successfull");
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            photo.setImageBitmap(bitmap);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                photo.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Initialize the views
     */
    private void initViews() {
        Log.d(TAG, "initViews: initing views");
        name = findViewById(R.id.placeDialog_LBL_placeName);
        address = findViewById(R.id.placeDialog_LBL_placeAddress);
        rating = findViewById(R.id.placeDialog_LBL_placeRating);
        hours = findViewById(R.id.placeDialog_LTS_hoursList);
        photo = findViewById(R.id.placeDialog_IMG_placeImage);
        exitBtn = findViewById(R.id.placeDialog_BTN_exitButton);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
