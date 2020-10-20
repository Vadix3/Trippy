package com.example.trippy.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.trippy.Dialogs.ForecastDialog;
import com.example.trippy.Objects.DailyWeather;
import com.example.trippy.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.card.MaterialCardView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherFragment extends Fragment {

    public static final String TAG = "pttt";
    protected View view;

    private TextView weatherTempLabel;
    private TextView weatherDescriptionLabel;
    private ImageView weatherIcon;
    private MaterialCardView weatherCardView;
    private ForecastDialog forecastDialog;


    private String weatherTemp = "";
    private String weatherDesc = "";
    private String iconID = "";
    private LatLng city;

    public WeatherFragment() {
    }

    public WeatherFragment(String weatherTemp, String weatherDesc, String iconID, LatLng city) {
        this.weatherTemp = weatherTemp;
        this.weatherDesc = weatherDesc;
        this.iconID = iconID;
        this.city = city;
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
            view = inflater.inflate(R.layout.fragment_weather, container, false);
        }

        initViews();
        return view;
    }

    private void initViews() {
        Log.d(TAG, "initViews: initing weatherCard");

        // Weather fragment
        weatherCardView = view.findViewById(R.id.weatherfragment_LAY_cardview);
        weatherIcon = view.findViewById(R.id.weatherfragment_IMG_weatherIcon);
        weatherDescriptionLabel = view.findViewById(R.id.weatherfragment_LBL_weatherDescription);
        weatherTempLabel = view.findViewById(R.id.weatherfragment_LBL_weatherTemp);
        if (weatherTemp == null) {
            weatherTempLabel.setText("N/A");
            weatherDescriptionLabel.setText("");
        } else {
            weatherTempLabel.setText(weatherTemp);
            weatherDescriptionLabel.setText(weatherDesc);
        }
        if (iconID != null) {
            Log.d(TAG, "initViews: Getting image resource");
            String iconUrl = "https://www.weatherbit.io/static/img/icons/" + iconID + ".png";
            Log.d(TAG, "initViews: Fetching icon: " + iconUrl);
            Glide.with(weatherIcon).load(iconUrl).into(weatherIcon);
        }

        weatherCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Weather card clicked");
                weatherCardView.setEnabled(false);
                openWeatherForecastDialog();
            }
        });

    }

    /**
     * A method to open forecast dialog
     */
    private void openWeatherForecastDialog() {
        Log.d(TAG, "openWeatherDetailsDialog: Opening forecast dialog");

        String url = "https://api.openweathermap.org/data/2.5/onecall?lat="
                + city.latitude + "&lon=" + city.longitude
                + "&units=metric&exclude=current,minutely,hourly,alerts&appid="
                + getActivity().getString(R.string.open_weather_api_key);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: Exception: " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();
                parseJson(responseString);
            }
        });
    }

    /**
     * A method to parse the given json response
     */
    private void parseJson(String responseString) {
        Log.d(TAG, "parseJson: Parsing response: " + responseString);
        try {
            ArrayList<DailyWeather> dailyWeatherArray = new ArrayList<>();

            JSONObject all = new JSONObject(responseString);
            JSONArray daily = (JSONArray) all.get("daily");

            ArrayList<JSONObject> dailyArray = new ArrayList<>();

            for (int i = 1; i < daily.length(); i++) {
                dailyArray.add((JSONObject) daily.get(i));
            }

            for (JSONObject object : dailyArray) {
                JSONObject temperature = (JSONObject) object.get("temp");
                JSONObject feelsLike = (JSONObject) object.get("feels_like");
                JSONArray weatherDescArray = (JSONArray) object.get("weather");
                JSONObject weatherDesc = (JSONObject) weatherDescArray.get(0);
                Date tempDate = new Date(Long.valueOf((Integer) object.get("dt")) * 1000);
                Log.d(TAG, "parseJson: realTemp type: " + temperature.get("day").getClass());
                double realTemp = (double) temperature.get("day");
                Log.d(TAG, "parseJson: feelsTemp type: " + temperature.get("day").getClass());
                double realFeelsLike = (double) feelsLike.get("day");
                Log.d(TAG, "parseJson: humidity type: " + temperature.get("day").getClass());
                int humidity = (Integer) object.get("humidity");

                String description = (String) weatherDesc.get("description");
                String icon = (String) weatherDesc.get("icon");

                dailyWeatherArray.add(new DailyWeather(tempDate, realTemp, realFeelsLike
                        , humidity, description, icon));
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createDialog(dailyWeatherArray);
                }
            });
        } catch (JSONException e) {
            Log.d(TAG, "parseJson: Exception: " + e.getMessage());
        }
    }


    private void createDialog(ArrayList<DailyWeather> dailyWeatherArray) {
        if (city == null) {
            Toast.makeText(getActivity(), "City Unavailable!", Toast.LENGTH_SHORT).show();
        } else {
            forecastDialog = new ForecastDialog(getActivity(), dailyWeatherArray);
            forecastDialog.show();
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            forecastDialog.getWindow().setLayout(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
            forecastDialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            forecastDialog.getWindow().setDimAmount(0.9f);
            forecastDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            forecastDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    weatherCardView.setEnabled(true);
                }
            });

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d("pttt", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

}
