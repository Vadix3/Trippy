package com.example.trippy.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.trippy.Objects.MyEvent;
import com.example.trippy.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class NextEventDialog extends Dialog implements OnMapReadyCallback{

    private static final String TAG = "pttt";
    private static final float DEFAULT_ZOOM = 15f;

    private Context context;
    private MyEvent myEvent;

    private TextView eventTitle;
    private TextView eventAddress;
    private TextView eventDate;
    private TextView eventHour;
    private MapView mapView;
    private GoogleMap mMap;
    private LatLng myLocation;


    public NextEventDialog(@NonNull Context context, MyEvent event) {
        super(context);
        this.context = context;
        this.myEvent = event;
        LatLng temp = new LatLng(event.getEventLocation().getLatitude()
                ,event.getEventLocation().getLongitude());
        this.myLocation =temp;
    }

    private void initMap() {
        Log.d(TAG, "initMap: initing map");
        mapView = findViewById(R.id.nextEvent_MAP_map);
        mapView.onCreate(onSaveInstanceState());
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_nextevent);
        initViews();
        initMap();
    }

    private void initViews() {
        eventTitle=findViewById(R.id.nextEvent_LBL_title);
        eventTitle.setText(myEvent.getEventName());
        eventAddress=findViewById(R.id.nextEvent_LBL_eventAddress);
        eventAddress.setText(myEvent.getLocationName());
        eventDate=findViewById(R.id.nextEvent_LBL_eventDate);
        String myDate = new java.text.SimpleDateFormat("dd/MM/yyyy")
                .format(new java.util.Date(myEvent.getEventDate() * 1000));
        eventDate.setText(myDate);
        eventHour=findViewById(R.id.nextEvent_LBL_eventTime);
        eventHour.setText(myEvent.getEventTime());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;
        moveCamera(myLocation, DEFAULT_ZOOM);
        mMap.setMyLocationEnabled(true);
        /** Remove center location button*/
        addPlaceLocationMarker(myLocation);
    }
    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: Moving the camera to: lat: " + latLng.latitude + " lon: " +
                latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
    /**
     * A method to add the place location marker on the map
     */
    private void addPlaceLocationMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mMap.clear();
        markerOptions.title("Current Position");
        markerOptions.getPosition();
        markerOptions.title(myEvent.getLocationName());
        mMap.addMarker(markerOptions);
    }
}
