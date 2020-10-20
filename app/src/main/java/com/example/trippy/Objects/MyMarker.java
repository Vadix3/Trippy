package com.example.trippy.Objects;

import com.google.android.gms.internal.maps.zzt;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;

public class MyMarker {
    Place place;
    Marker marker;

    public MyMarker() {
    }

    public MyMarker(Marker marker, Place place) {
        this.marker = marker;
        this.place = place;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}
