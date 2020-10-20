package com.example.trippy.Objects;

import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.LocalDate;

import java.io.Serializable;

/**
 * An event class for trip events
 */
public class MyEvent implements Comparable<MyEvent>, Serializable {

    private String eventName;
    private String eventTime;
    private String eventType;
    private MyLatLng eventLocation;
    private Long eventDate;
    private String locationName;


    public MyEvent() {
    }

    public MyEvent(String eventName, String eventTime, String eventType, MyLatLng eventLocation
            , Long eventDate, String locationName) {
        this.eventName = eventName;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.eventLocation = eventLocation;
        this.eventDate = eventDate;
        this.locationName = locationName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public MyLatLng getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(MyLatLng eventLocation) {
        this.eventLocation = eventLocation;
    }

    public Long getEventDate() {
        return eventDate;
    }

    public void setEventDate(Long eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public String toString() {
        return "MyEvent{" +
                "eventName='" + eventName + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventLocation=" + eventLocation +
                ", eventDate=" + eventDate +
                '}';
    }

    @Override
    public int compareTo(MyEvent myEvent) {
        Long myDay = this.getEventDate();
        Long objectDay = myEvent.getEventDate();

        LocalDate myLocalDate = LocalDate.ofEpochDay(myDay);
        LocalDate objectLocalDate = LocalDate.ofEpochDay(objectDay);

        if (myLocalDate.isBefore(objectLocalDate)) {
            return -1;
        } else if (myLocalDate.isAfter(objectLocalDate)) {
            return 1;
        } else { // Both events are at the same date, check hour
            String myTime = this.eventTime;
            String checkTime = myEvent.eventTime;

            String[] myTimeArr = myTime.split(":");
            String[] myCheckTimeArr = checkTime.split(":");

            int myHour = Integer.parseInt(myTimeArr[0].trim());
            int myMin = Integer.parseInt(myTimeArr[1].trim());

            int checkHour = Integer.parseInt(myCheckTimeArr[0].trim());
            int checkMin = Integer.parseInt(myCheckTimeArr[1].trim());

            if (myHour < checkHour) {
                return -1;
            }
            if (myHour > checkHour) {
                return 1;
            } else { // both events are at the same hour
                if (myMin < checkMin) {
                    return -1;
                }
                if (myMin > checkMin) {
                    return 1;
                } else return 0; // both events at the same time
            }
        }
    }
}
