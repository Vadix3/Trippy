package com.example.trippy.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trippy.Dialogs.EventLocationDialog;
import com.example.trippy.Objects.MyEvent;
import com.example.trippy.R;
import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

public class RecyclerViewEventAdapter extends RecyclerView.Adapter<RecyclerViewEventAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MyEvent> dailyEvents;
    private ImageView openLocationBtn;
    public static final String TAG = "pttt";

    public RecyclerViewEventAdapter(Context context, ArrayList<MyEvent> dailyEvents) {
        this.context = context;
        this.dailyEvents = dailyEvents;
    }


    @NonNull
    @Override
    public RecyclerViewEventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.event_recyclerview_row, parent, false);
        return new RecyclerViewEventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewEventAdapter.ViewHolder holder, int position) {
        MyEvent temp = dailyEvents.get(position);
        String name = temp.getEventName();
        String time = temp.getEventTime();
        String type = temp.getEventType();

        Instant instant = Instant.ofEpochMilli(temp.getEventDate() * 1000);
        LocalDate tempDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String myDate = tempDate.format(formatter);
        openLocationBtn = holder.locationButton;

        if (temp.getEventLocation() != null) {
            final LatLng eventLocation = new LatLng(temp.getEventLocation().getLatitude()
                    , temp.getEventLocation().getLongitude());
            Log.d(TAG, "onBindViewHolder: Event location available: " + eventLocation.toString());
            openLocationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openLocationBtn.setEnabled(false);
                    openEventLocationOnMap(eventLocation, temp.getLocationName());
                }
            });
        } else {
            Log.d(TAG, "onBindViewHolder: Event location unavailable");
            openLocationBtn.setVisibility(View.INVISIBLE);
        }

        holder.eventTitle.setText(name);
        holder.eventHour.setText(time);
        holder.eventDate.setText(myDate);
        checkEventType(type, holder.eventType);
    }

    /**
     * A method to set the type of the event
     */
    private void checkEventType(String type, ImageView eventType) {
        Log.d(TAG, "checkEventType: " + type);
        ImageView typeImage = eventType;
        switch (type) {
            case "Travel":
                Glide.with(typeImage).load(R.drawable.ic_travel).into(typeImage);
                break;
            case "Meeting":
                Glide.with(typeImage).load(R.drawable.ic_meeting).into(typeImage);
                break;
            case "Bar":
                Glide.with(typeImage).load(R.drawable.ic_pub).into(typeImage);
                break;
            case "Food":
                Glide.with(typeImage).load(R.drawable.ic_food).into(typeImage);
                break;
            case "Attraction":
                Glide.with(typeImage).load(R.drawable.ic_attraction).into(typeImage);
                break;
            case "Other":
                Glide.with(typeImage).load(R.drawable.ic_other).into(typeImage);
                break;
        }
    }

    /**
     * A method to open the event location on the map
     */
    private void openEventLocationOnMap(LatLng latLng, String locationName) {
        Log.d(TAG, "openEventLocationOnMap: Opening event location: " + latLng.toString());
        EventLocationDialog dialog = new EventLocationDialog(context, latLng, locationName);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.getWindow().setDimAmount(0.8f);
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                openLocationBtn.setEnabled(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dailyEvents.size();
    }


    /**
     * An inner class to specify each row contents
     */
    public class ViewHolder extends RecyclerView.ViewHolder { // To hold each row

        TextView eventTitle, eventDate, eventHour;
        ImageView eventType, locationButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        /**
         * A method to initialize the widgets of the row
         */
        private void initViews() {
            eventTitle = itemView.findViewById(R.id.eventRow_LBL_eventName);
            eventDate = itemView.findViewById(R.id.eventRow_LBL_eventDate);
            eventHour = itemView.findViewById(R.id.eventRow_LBL_eventTime);
            eventType = itemView.findViewById(R.id.eventRow_IMG_icon);
            locationButton = itemView.findViewById(R.id.eventRow_IMG_location);
        }
    }


}
