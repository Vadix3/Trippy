package com.example.trippy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trippy.Objects.DailyWeather;
import com.example.trippy.R;

import java.util.ArrayList;

public class RecyclerViewWeatherAdapter extends RecyclerView.Adapter<RecyclerViewWeatherAdapter.ViewHolder> {

    private Context context;
    private ArrayList<DailyWeather> dailyWeatherArray;
    public static final String TAG = "pttt";


    public RecyclerViewWeatherAdapter(Context context, ArrayList<DailyWeather> dailyWeatherArray) {
        this.context = context;
        this.dailyWeatherArray = dailyWeatherArray;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.forecast_recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Updating dynamically the contents of a row in the Recycler view, using given position
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String date = dailyWeatherArray.get(position).getDate().toString();
        String description = dailyWeatherArray.get(position).getDescription();
        String realTemp = Math.round(dailyWeatherArray.get(position).getTemp()) + "°C";
        String feelsTemp = "(" + Math.round(dailyWeatherArray.get(position).getFeelsTemp()) + ")";
        String humidity = "Humidity: " + dailyWeatherArray.get(position).getHumidity()+"%";
        String iconID = dailyWeatherArray.get(position).getIcon();
        String[] words = date.split("\\W+");
        String dateToPrint = words[0] + " " + words[1] + " " + words[2];


        holder.day.setText(dateToPrint);
        holder.feelsTemp.setText(feelsTemp);
        holder.realTemp.setText(realTemp);
        holder.description.setText(description);
        holder.humidity.setText(humidity);
        String iconUrl = "https://openweathermap.org/img/wn/" + iconID + "@4x.png";
        Glide.with(holder.icon).load(iconUrl).into(holder.icon);
    }

    @Override
    public int getItemCount() {
        return dailyWeatherArray.size();
    }


    /**
     * An inner class to specify each row contents
     */
    public class ViewHolder extends RecyclerView.ViewHolder { // To hold each row

        TextView day, description, realTemp, feelsTemp, humidity;
        ImageView icon;
        RelativeLayout mainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        /**
         * A method to initialize the widgets of the row
         */
        private void initViews() {
            mainLayout = itemView.findViewById(R.id.row_LAY_mainLayout);
            icon = itemView.findViewById(R.id.row_IMG_icon);
            day = itemView.findViewById(R.id.row_LBL_day);
            description = itemView.findViewById(R.id.row_LBL_description);
            realTemp = itemView.findViewById(R.id.row_LBL_realTemp);
            feelsTemp = itemView.findViewById(R.id.row_LBL_feelsLike);
            humidity = itemView.findViewById(R.id.row_LBL_humidity);
        }
    }

}
