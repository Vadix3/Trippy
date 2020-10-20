package com.example.trippy.Objects;

import java.io.Serializable;

public class MyUser implements Serializable {
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String country;
    private String countryCode;
    private String currencyCode;
    private String password;
    private float currencyRate;

    public MyUser() {
    }

    public MyUser(String firstName, String lastName, String emailAddress, String password, String country
            , String countryCode, String currencyCode, float currencyRate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password=password;
        this.country = country;
        this.countryCode = countryCode;
        this.currencyCode = currencyCode;
        this.currencyRate = currencyRate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstMame) {
        this.firstName = firstMame;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
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

    public float getCurrencyRate() {
        return currencyRate;
    }

    public void setCurrencyRate(float currencyRate) {
        this.currencyRate = currencyRate;
    }

    @Override
    public String toString() {
        return "MyUser{" +
                "firstMame='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", country='" + country + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", currencyRate=" + currencyRate +
                '}';
    }
}
