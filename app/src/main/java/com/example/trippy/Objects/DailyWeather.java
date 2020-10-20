package com.example.trippy.Objects;

import java.util.Date;

/**
 * A class to represent daily weather with few parameters
 */
public class DailyWeather {

    private Date date;
    private double temp;
    private double feelsTemp;
    private int humidity;
    private String description;
    private String icon;


    public DailyWeather() {
    }

    public DailyWeather(Date date, double temp, double feelsTemp, int humidity, String description, String icon) {
        this.date = date;
        this.temp = temp;
        this.feelsTemp = feelsTemp;
        this.humidity = humidity;
        this.description = description;
        this.icon = icon;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getFeelsTemp() {
        return feelsTemp;
    }

    public void setFeelsTemp(double feelsTemp) {
        this.feelsTemp = feelsTemp;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "DailyWeather{" +
                "date=" + date +
                ", temp=" + temp +
                ", feelsTemp=" + feelsTemp +
                ", humidity=" + humidity +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
