package com.example.trippy.Objects;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to contain my location attributes such as city, country, and other
 */
public class MyTrip implements Serializable {
    private String city = "";
    private String country = "";
    private String countryCode = "";
    private String currencyCode = "";
    private String tripName = "";
    private float currencyRate = 0;
    private List<Long> tripDates;
    private ArrayList<MyEvent> events;

    public ArrayList<MyEvent> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<MyEvent> events) {
        this.events = events;
    }

    public MyTrip() {
    }

    public MyTrip(String city, String country, String countryCode, String currencyCode
            , float currencyRate, List<Long> tripDates, String tripName, ArrayList<String> events) {
        this.city = city;
        this.countryCode = countryCode;
        this.country = country;
        this.currencyCode = currencyCode;
        this.currencyRate = currencyRate;
        this.tripDates = tripDates;
        this.tripName = tripName;
    }

    public float getCurrencyRate() {
        return currencyRate;
    }

    public void setCurrencyRate(float currencyRate) {
        this.currencyRate = currencyRate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public List<Long> getTripDates() {
        return tripDates;
    }

    public void setTripDates(List<Long> tripDates) {
        this.tripDates = tripDates;
    }

    @Override
    public String toString() {
        return "MyTrip{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", tripName='" + tripName + '\'' +
                ", currencyRate=" + currencyRate +
                '}';
    }
}
