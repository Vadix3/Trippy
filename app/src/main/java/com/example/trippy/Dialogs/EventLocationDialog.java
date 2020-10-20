package com.example.trippy.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trippy.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class EventLocationDialog extends Dialog implements OnMapReadyCallback {

    private static final String TAG = "pttt";
    private static final float DEFAULT_ZOOM = 15f;
    private Context context;
    private GoogleMap mMap;
    private MapView mapView;
    private Double lat;
    private Double lon;
    private String locationName;

    public EventLocationDialog(@NonNull Context context, LatLng myLocation, String locationName) {
        super(context);
        this.lat = myLocation.latitude;
        this.lon = myLocation.longitude;
        this.context = context;
        this.locationName = locationName;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_eventmap);
        initMap();
    }

    private void initMap() {
        Log.d(TAG, "initMap: initing map");

        mapView = findViewById(R.id.maps_MAP_map);
        mapView.onCreate(onSaveInstanceState());
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: Moving the camera to: lat: " + latLng.latitude + " lon: " +
                latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;
        LatLng latLng = new LatLng(lat, lon);
        moveCamera(latLng, DEFAULT_ZOOM);
        mMap.setMyLocationEnabled(true);
        /** Remove center location button*/
//        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        addPlaceLocationMarker(latLng);
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
        markerOptions.title(locationName);
        mMap.addMarker(markerOptions);
    }
}