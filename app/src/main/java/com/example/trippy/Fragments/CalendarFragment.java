package com.example.trippy.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trippy.Activities.CalendarActivity;
import com.example.trippy.Dialogs.NextEventDialog;
import com.example.trippy.Interfaces.EventsArrayCallback;
import com.example.trippy.Interfaces.NewEventCallback;
import com.example.trippy.Objects.MyEvent;
import com.example.trippy.Objects.MyTrip;
import com.example.trippy.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.card.MaterialCardView;
import com.google.api.Distribution;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    public static final String TAG = "pttt";
    protected View view;
    private TextView date;
    private TextView nextPlan;
    private RelativeLayout dateLayout;
    private LinearLayout planLayout;


    private MyTrip myCurrentTrip;
    private LatLng myLocation;

    public CalendarFragment() {
    }

    public CalendarFragment(MyTrip myCurrentTrip, LatLng myLocation) {
        this.myCurrentTrip = myCurrentTrip;
        this.myLocation = myLocation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_calendar, container, false);
        }

        date = view.findViewById(R.id.calendarFragment_LBL_dateLabel);
        nextPlan = view.findViewById(R.id.calendarFragment_LBL_nextPlan);
        if (myCurrentTrip.getEvents() == null) {
            nextPlan.setText("No plans!");
        } else {
            String myDate = new java.text.SimpleDateFormat("dd/MM")
                    .format(new java.util.Date(myCurrentTrip.getEvents().get(0).getEventDate() * 1000));

            String nextPlanText = myCurrentTrip.getEvents().get(0).getEventName() + "\n"
                    + myCurrentTrip.getEvents().get(0).getEventTime() + " "
                    + myDate;
            nextPlan.setText(nextPlanText);
        }
        String currentDate = new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(new Date());
        Log.d(TAG, "onCreateView: gotDate: " + currentDate);
        date.setText(currentDate);

        dateLayout = view.findViewById(R.id.calendarFragment_LAY_dateLayout);
        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalendarActivity();
                dateLayout.setEnabled(false);
            }
        });

        planLayout = view.findViewById(R.id.calendarFragment_LAY_planLayout);
        planLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myCurrentTrip.getEvents()!=null){
                    openPlanDetails();
                    planLayout.setEnabled(false);
                }else{
                    Log.d(TAG, "onClick: events are empty");
                }
            }
        });

        return view;
    }

    /**
     * A method to open the plan details dialog
     */
    private void openPlanDetails() {
        Log.d(TAG, "openPlanDetails: Opening plan details");
        NextEventDialog dialog = new NextEventDialog(getContext(),myCurrentTrip.getEvents().get(0));
        dialog.show();
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
        dialog.getWindow().setLayout(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        dialog.getWindow().setDimAmount(0.9f);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                planLayout.setEnabled(true);
            }
        });
    }

    /**
     * A method to open the calendar activity
     */
    private void openCalendarActivity() {
        Log.d(TAG, "openCalendarActivity: Opening calendar activity");
        Intent intent = new Intent(getActivity(), CalendarActivity.class);
        Gson gson = new Gson();

        String myLocationString = gson.toJson(this.myLocation);
        intent.putExtra("location", myLocationString);
        List<Long> tempTripDates = myCurrentTrip.getTripDates();
        ArrayList<MyEvent> tempEvents = myCurrentTrip.getEvents();

        if (tempTripDates == null && tempEvents == null) {
            Log.d(TAG, "openCalendarActivity: Both arrays are null");
            startActivityForResult(intent, 52);

        } else if (tempTripDates == null) {
            Log.d(TAG, "openCalendarActivity: only trip dates are nul");

            String jsonEventDates = gson.toJson(tempEvents);
            intent.putExtra("eventDays", jsonEventDates);
            startActivityForResult(intent, 52);

        } else if (tempEvents == null) {
            Log.d(TAG, "openCalendarActivity: only event dates are null");

            String jsonTripDates = gson.toJson(tempTripDates);
            intent.putExtra("selectedDays", jsonTripDates);
            startActivityForResult(intent, 52);

        } else {
            Log.d(TAG, "openCalendarActivity: both arrays are good");

            String jsonTripDates = gson.toJson(tempTripDates);
            String jsonEventDates = gson.toJson(tempEvents);
            intent.putExtra("selectedDays", jsonTripDates);
            intent.putExtra("eventDays", jsonEventDates);
            startActivityForResult(intent, 52);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 52:
                dateLayout.setEnabled(true);
                Log.d(TAG, "onActivityResult: Got from calendar activity and Im in main!");
                if (resultCode == 52) {
                    String eventJson = data.getStringExtra("events");
                    Log.d(TAG, "onActivityResult: Got event json: " + eventJson);
                    Gson gson = new Gson();
                    Type eventType = new TypeToken<ArrayList<MyEvent>>() {
                    }.getType();
                    ArrayList<MyEvent> sentEvents = gson.fromJson(eventJson, eventType);
                    Log.d(TAG, "onActivityResult: Events array: " + sentEvents.toString());
                    myCurrentTrip.setEvents(sentEvents);
                    Log.d(TAG, "onActivityResult: Events in trip: " + myCurrentTrip.getEvents().toString());


                    String myDate = new java.text.SimpleDateFormat("dd/MM/yyyy")
                            .format(new java.util.Date(myCurrentTrip.getEvents().get(0).getEventDate() * 1000));

                    String nextPlanText = myCurrentTrip.getEvents().get(0).getEventName() + "\n"
                            + myDate + "\n"
                            + myCurrentTrip.getEvents().get(0).getEventTime();
                    nextPlan.setText(nextPlanText);

                    sendEventsArrayToMainActivity();
                } else {
                    Log.d(TAG, "onActivityResult: Result not ok");
                }
                break;
        }
    }

    /**
     * A method to send the events array to the main activity
     */
    private void sendEventsArrayToMainActivity() {
        Log.d(TAG, "sendEventsArrayToMainActivity: Sending events array to main activity");

        EventsArrayCallback eventsArrayCallback;
        try {
            eventsArrayCallback = (EventsArrayCallback) getContext();
        } catch (ClassCastException e) {
            throw new ClassCastException(getContext().toString() + "Must implement dialog listener");
        }
        eventsArrayCallback.getUpdatedEventsArray(myCurrentTrip.getEvents());
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }
}
