package com.example.trippy.Objects;

/** Firebase does not allow the default latlng class to be uploaded, so I need to create one of my own*/
public class MyLatLng {
    private Double latitude;
    private Double longitude;

    public MyLatLng() {}

    public MyLatLng(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "MyLatLng{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
