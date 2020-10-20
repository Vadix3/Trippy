package com.example.trippy.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trippy.Adapters.RecyclerViewEventAdapter;
import com.example.trippy.Objects.MyEvent;
import com.example.trippy.R;

import java.util.ArrayList;

public class EventListFragment extends Fragment {
    public static final String TAG = "pttt";
    protected View view;
    private RecyclerView recyclerView;
    private ArrayList<MyEvent> dailyEvents;

    public EventListFragment() {
    }

    public EventListFragment(ArrayList<MyEvent> dailyEvents) {
        this.dailyEvents = dailyEvents;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_eventlist, container, false);
        }
        populateEventList(dailyEvents);
        return view;
    }


    /**
     * A method to populate the daily events recyclerview
     */
    private void populateEventList(ArrayList<MyEvent> dailyEvents) {
        recyclerView = view.findViewById(R.id.dailyEventFragment_LST_recyclerview);
        if (dailyEvents != null) {
            Log.d(TAG, "populateEventList: " + dailyEvents.toString());
            recyclerView.setVisibility(View.VISIBLE);
            RecyclerViewEventAdapter adapter = new RecyclerViewEventAdapter(getContext(), dailyEvents);
            recyclerView.setAdapter(adapter);
        } else {
            Log.d(TAG, "populateEventList: No events to show");
            recyclerView.setVisibility(View.GONE);
        }
    }
}
