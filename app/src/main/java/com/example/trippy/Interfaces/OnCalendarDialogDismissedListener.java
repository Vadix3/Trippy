package com.example.trippy.Interfaces;

import com.example.trippy.Objects.MyEvent;

import java.util.ArrayList;

/**
 * Callback Interface for getting events array from Calendar dialog
 */

public interface OnCalendarDialogDismissedListener {
    void getEventsArray(ArrayList<MyEvent> tripEvents);
}
