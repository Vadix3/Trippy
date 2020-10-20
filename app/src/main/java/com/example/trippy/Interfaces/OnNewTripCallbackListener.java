package com.example.trippy.Interfaces;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.List;

/**
 * Callback Interface for getting days array and trip name from newTrip dialog
 */
public interface OnNewTripCallbackListener {
    void getResult(List<CalendarDay> tripDates, String tripName);
}