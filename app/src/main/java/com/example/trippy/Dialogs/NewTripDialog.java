package com.example.trippy.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;


import com.example.trippy.Interfaces.OnNewTripCallbackListener;
import com.example.trippy.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.List;

public class NewTripDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private static final String TAG = "pttt";
    /**
     * Views
     */
    private LinearLayout buttonLayout;
    private RelativeLayout calendarLayout;
    private TextView helloText;
    private TextView addButton;
    private EditText tripNameEdt;
    private TextView yesText;
    private TextView noText;
    private MaterialCalendarView calendarView;
    private FragmentManager fragmentManager;

    /**
     * Variables
     */
    private String tripName;
    private List<CalendarDay> tripDates;


    public NewTripDialog(@NonNull Context context, FragmentManager fragmentManager) {
        super(context);
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_newtrip);
        setCancelable(false);
        Log.d(TAG, "onCreate: Creating " + this.getClass().toString() + " Dialog");

        buttonLayout = findViewById(R.id.newtrip_LAY_tripNameLayout);
        calendarLayout = findViewById(R.id.newtrip_LAY_caneldarLAyout);

        //REMOVE HERE
        calendarLayout.setVisibility(View.GONE);

        helloText = findViewById(R.id.newtrip_LBL_helloLabel);
        addButton = findViewById(R.id.newtrip_BTN_addTripNameButton);
        tripNameEdt = findViewById(R.id.newtrip_EDT_tripName);

        yesText = findViewById(R.id.newevent_LBL_save);
        noText = findViewById(R.id.newevent_LBL_cancel);
        yesText.setOnClickListener(this);
        noText.setOnClickListener(this);
        addButton.setOnClickListener(this);
        initDatePicker();
    }

    /**
     * A method to create the date picker
     */
    private void initDatePicker() {
        Log.d(TAG, "initDatePicker: Creating a new datepicker");
        /**
         * A method to initialize the calendar
         */
        calendarView = findViewById(R.id.newtrip_CAL_calendar);
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_RANGE);
        calendarView.setSelectionColor(context.getColor(R.color.colorPrimary));
        calendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView widget, @NonNull List<CalendarDay> dates) {
                Log.d(TAG, "onRangeSelected: Selected range: " + dates.toString());
                tripDates = dates;
            }
        });
        calendarView.state().edit()
                .setFirstDayOfWeek(DayOfWeek.SUNDAY)
                .setMinimumDate(CalendarDay.from(LocalDate.now()))
                .setMaximumDate(CalendarDay.from(LocalDate.now().getYear() + 1, 5, 12))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newtrip_BTN_addTripNameButton:
                Log.d(TAG, "onClick: AddTripNameButton was clicked");
                if (isTextValid()) {
                    tripName = tripNameEdt.getText().toString();
                    Log.d(TAG, "onClick: Recieved trip name: " + tripName);
                    if (tripDates != null) {
                        Log.d(TAG, "onClick: Trip dates are not null, moving back to main " +
                                "layout with: \nTripname: " + tripName + "\nTrip dates: " + tripDates.toString());
                        /** Callback days list and name */
                        OnNewTripCallbackListener listener;
                        try {
                            listener = (OnNewTripCallbackListener) context;
                        } catch (ClassCastException e) {
                            throw new ClassCastException(context.toString() + "Must implement dialog listener");
                        }

                        // Send the trip Dates and trip name to main layout
                        listener.getResult(tripDates, tripName);

                        dismiss();
                    } else {
                        Toast.makeText(context, "Please enter dates", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tripNameEdt.setError("Please enter a valid name!");
                }
                break;
            case R.id.newevent_LBL_save:
                Log.d(TAG, "onClick: Yes button clicked");
                moveToDatePicker();
                break;
            case R.id.newevent_LBL_cancel:
                Log.d(TAG, "onClick: No button clicked");
                OnNewTripCallbackListener listener;
                try {
                    listener = (OnNewTripCallbackListener) context;
                } catch (ClassCastException e) {
                    throw new ClassCastException(context.toString() + "Must implement dialog listener");
                }
                // Send the trip Dates and trip name to main layout
                listener.getResult(null, null);
                dismiss();
                break;
        }
    }

    /**
     * A method to move to the date picker stage
     */
    private void moveToDatePicker() {
        Log.d(TAG, "moveToDatePicker: Opening date picking option");
        yesText.setVisibility(View.INVISIBLE);
        noText.setVisibility(View.INVISIBLE);
        helloText.setVisibility(View.INVISIBLE);
        calendarLayout.setVisibility(View.VISIBLE);
        buttonLayout.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.65);
        this.getWindow().setLayout(width, height);
    }


    /**
     * A method to check if EDT text is valid
     */
    private boolean isTextValid() {
        Log.d(TAG, "isTextValid: Checking if edit text input is valid");
        if (tripNameEdt.getText().toString().equals("")) {
            Log.d(TAG, "isTextValid: text is not valid");
            return false;
        }
        Log.d(TAG, "isTextValid: Test is valid");
        return true;
    }
}
