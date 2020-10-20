package com.example.trippy.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trippy.Adapters.RecyclerViewWeatherAdapter;
import com.example.trippy.Objects.DailyWeather;
import com.example.trippy.R;

import java.util.ArrayList;

public class ForecastDialog extends Dialog {

    public static final String TAG = "pttt";
    private Context context;
    private ArrayList<DailyWeather> dailyWeatherArray;
    private RecyclerView recyclerView;

    public ForecastDialog(@NonNull Context context, ArrayList<DailyWeather> dailyWeatherArray) {
        super(context);
        this.context = context;
        this.dailyWeatherArray = dailyWeatherArray;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_forecast);
        Log.d(TAG, "onCreate: Got days: " + dailyWeatherArray.toString());
        initViews();
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initing views");
        recyclerView = findViewById(R.id.forecast_LST_recyclerview);
        RecyclerViewWeatherAdapter adapter = new RecyclerViewWeatherAdapter(context, dailyWeatherArray);
        recyclerView.setAdapter(adapter);
        ImageView exitBtn = findViewById(R.id.placeDialog_BTN_exitButton);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }


}
