package com.example.trippy.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.trippy.Adapters.RecyclerViewEventAdapter;
import com.example.trippy.Adapters.TripDatesDecorator;
import com.example.trippy.Dialogs.NewEventDialog;
import com.example.trippy.Fragments.CalendarFragment;
import com.example.trippy.Fragments.EventListFragment;
import com.example.trippy.Interfaces.NewEventCallback;
import com.example.trippy.Objects.MyEvent;
import com.example.trippy.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalendarActivity extends AppCompatActivity implements NewEventCallback {

    private static final String TAG = "pttt";
    private MaterialCalendarView calendarView;
    private List<CalendarDay> tripDates;
    private ArrayList<MyEvent> events;
    private LatLng myLocation;
    private List<CalendarDay> tripEventsDates;
    private CalendarDay selectedDay;
    private FloatingActionButton floatingActionButton;
    private boolean isAddEventOpen = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        checkReceivedData();
        initViews();
        initCalendar();

    }

    /**
     * A method to initialize the views
     */
    private void initViews() {
        Log.d(TAG, "initViews: Initializing views");

        floatingActionButton = findViewById(R.id.calendarActivity_BTN_addEventButton);
        floatingActionButton.setVisibility(View.INVISIBLE);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddEventDialog();
            }
        });


        if (events == null)
            events = new ArrayList<>();


    }

    /**
     * A method to initialize the calendar
     */
    private void initCalendar() {
        Log.d(TAG, "initCalendar: Initing calendar");
        calendarView = findViewById(R.id.calendarActivity_CAL_calendar);
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        calendarView.setSelectionColor(getColor(R.color.colorPrimary));
        calendarView.setDynamicHeightEnabled(true); // 5 rows instead of 6
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Log.d(TAG, "onDateSelected: Selected date: " + date.toString() + " boolean: " + selected);

                if (floatingActionButton.getVisibility() == View.INVISIBLE
                        && !isAddEventOpen) {
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
                displayDailyEvents(date);
            }
        });
        calendarView.state().edit()
                .setFirstDayOfWeek(DayOfWeek.SUNDAY)
                .setMinimumDate(CalendarDay.from(LocalDate.now()))
                .setMaximumDate(CalendarDay.from(LocalDate.now().getYear() + 1, 5, 12))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        if (tripDates != null) { // Paint trip days if available
            paintTripDates();
        }
        if (tripEventsDates != null) {
            paintTripPlans();
        }
    }

    /** A method to display the daily events below the calendar
     * @param date*/
    private void displayDailyEvents(CalendarDay date) {
        Log.d(TAG, "displayDailyEvents: Displaying events");

        ArrayList<MyEvent> dailyEvents = null;

        if (tripEventsDates != null) {
            if (tripEventsDates.contains(date)) {
                Log.d(TAG, "onDateSelected: There is event on this date: " + date.toString());
                dailyEvents = new ArrayList<>();
                for (MyEvent event : events) {
                    Instant instant = Instant.ofEpochMilli(event.getEventDate() * 1000);
                    LocalDate tempDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                    if (CalendarDay.from(tempDate).equals(date)) {
                        dailyEvents.add(event);
                    }
                }
            }
        }
        initListFragment(dailyEvents);
        selectedDay = date;
    }

    /**
     * A method to populate the daily events recyclerview
     */
    private void initListFragment(ArrayList<MyEvent> dailyEvents) {
        EventListFragment eventListFragment = new EventListFragment(dailyEvents);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.calendarActivity_LAY_listFrame, eventListFragment);
        transaction.commit();
    }

    /**
     * A method to add the input event to the events array
     */
    private void addEventToEventsArray(MyEvent event) {
        Log.d(TAG, "addEventToEventsArray: Adding event to events array: " + event.toString());
        CalendarDay tempDay = selectedDay;
        //Add to myEvents array
        //Sort by date
        events.add(event);
        Collections.sort(events);
        Log.d(TAG, "addEventToEventsArray: Added to trip events");
        if (tripEventsDates == null) {
            tripEventsDates = new ArrayList<>();
        }
        tripEventsDates.add(tempDay);
        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "addEventToEventsArray: Events array: " + events.toString());
        paintTripPlans();
    }

    /**
     * A method to open the new event dialog
     */
    private void openAddEventDialog() {
        Log.d(TAG, "openAddEventDialon: Opening new event dialog");
        NewEventDialog newEventDialog = new NewEventDialog(this, myLocation);
        newEventDialog.show();
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
        newEventDialog.getWindow().setLayout(width, height);
        newEventDialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        newEventDialog.getWindow().setDimAmount(0.9f);
        isAddEventOpen = true;
    }

    /**
     * A method to check what data the activity received. It can get both event and dates array,
     * One of them or none.
     */
    private void checkReceivedData() {
        Log.d(TAG, "checkReceivedData: Checking for received data");
        Intent intent = getIntent();
        String myLocationString = intent.getStringExtra("location");
        String tripDatesString = intent.getStringExtra("selectedDays");
        String eventDaysString = intent.getStringExtra("eventDays");
        Gson gson = new Gson();
        Type locationType = new TypeToken<LatLng>() {
        }.getType();
        Type longType = new TypeToken<List<Long>>() {
        }.getType();
        Type eventType = new TypeToken<ArrayList<MyEvent>>() {
        }.getType();
        myLocation = gson.fromJson(myLocationString, locationType);
        Log.d(TAG, "checkReceivedData: Received location: " + myLocation.toString());

        if (tripDatesString == null && eventDaysString == null) {
            Log.d(TAG, "checkReceivedData: Got both null arrays");
            tripDates = null;
            events = null;

        } else if (tripDatesString == null) {
            Log.d(TAG, "checkReceivedData: Only trip dates is null");
            tripDates = null;
            events = gson.fromJson(eventDaysString, eventType);

        } else if (eventDaysString == null) {
            Log.d(TAG, "checkReceivedData: Only event dates is null");
            events = null;
            List<Long> longTripDates = gson.fromJson(tripDatesString, longType);
            tripDates = new ArrayList<>();
            for (long l : longTripDates) {
                tripDates.add(CalendarDay.from(LocalDate.ofEpochDay(l)));
            }

        } else {
            Log.d(TAG, "checkReceivedData: Got both stuff");
            List<Long> longTripDates = gson.fromJson(tripDatesString, longType);
            events = gson.fromJson(eventDaysString, eventType);
            tripDates = new ArrayList<>();
            for (long l : longTripDates) {
                tripDates.add(CalendarDay.from(LocalDate.ofEpochDay(l)));
            }
        }

        tripEventsDates = new ArrayList<CalendarDay>();

        if (events != null) {
            Log.d(TAG, "checkReceivedData: Trip events available:");
            for (MyEvent event : events) {
                Long tempLong = event.getEventDate();

                Instant instant = Instant.ofEpochMilli(tempLong * 1000);
                LocalDate tempDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

                Log.d(TAG, "checkReceivedData: Temp date: " + tempDate.toString());

                tripEventsDates.add(CalendarDay.from(tempDate));
            }
            Log.d(TAG, "checkReceivedData: Events: " + tripEventsDates);
        } else {
            Log.d(TAG, "checkReceivedData: Trip events unavailable");
        }
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        if(!events.isEmpty()){
            Intent resultIntent = new Intent();
            Gson gson = new Gson();
            String jsonEvents = gson.toJson(events);
            Log.d(TAG, "onBackPressed: Sending back Json: " + jsonEvents);
            resultIntent.putExtra("events", jsonEvents);
            setResult(52, resultIntent);
        }
        finish();
    }

    /**
     * A method to paint given trip dates
     */
    private void paintTripDates() {
        Log.d(TAG, "paintTripDates: Painting given dates: " + this.tripDates.toString());
        TripDatesDecorator eventDecorator = new TripDatesDecorator(getColor(R.color.colorPrimary), tripDates);
        calendarView.addDecorator(eventDecorator);
    }

    /**
     * A method to paint trip event days
     */
    private void paintTripPlans() {
        Log.d(TAG, "paintTripEvents: Painting plans: " + tripEventsDates.toString());
        if (tripEventsDates != null) {
            TripDatesDecorator decorator = new TripDatesDecorator(getColor(R.color.eventColor), tripEventsDates);
            calendarView.addDecorator(decorator);
        }
    }

    @Override
    public void getNewEvent(MyEvent event, boolean setAlarm) {
        Log.d(TAG, "getNewEvent: Got event start: " + event.toString());

        Instant instant = selectedDay.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        long timeInMillis = instant.toEpochMilli() / 1000;

        Log.d(TAG, "getNewEvent: Time in epoch: " + timeInMillis);

        event.setEventDate(timeInMillis);

        Log.d(TAG, "getNewEvent: Got event: " + event.toString());
        if (setAlarm) {
            setAlarmOnPhone(event.getEventTime(), event.getEventDate());
        }
        addEventToEventsArray(event);
        displayDailyEvents(selectedDay);
    }

    /**
     * A method to create alarm on selected day
     */
    private void setAlarmOnPhone(String eventTime, Long eventDate) {
        Log.d(TAG, "setAlarmOnPhone: Setting alarm on: " + eventDate.toString() + " ," + eventTime);
    }
}
