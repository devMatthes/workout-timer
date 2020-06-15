package com.example.workouttimer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androdocs.httprequest.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private String CITY = "wroclaw,pl";
    private String API_KEY = "e83e18c6808093d5149fb0519759662c";
    private TextView addressTxt, dateTxt, tempTxt, windTxt, pressureTxt, humidityTxt;
    private Button boxingBtn;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressTxt = findViewById(R.id.address);
        dateTxt = findViewById(R.id.date);
        tempTxt = findViewById(R.id.temp);
        windTxt = findViewById(R.id.windSpeed);
        pressureTxt = findViewById(R.id.pressure);
        humidityTxt = findViewById(R.id.humidity);

        boxingBtn = findViewById(R.id.button);
        boxingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBoxingDialog(MainActivity.this);
            }
        });
        new weatherTask().execute();
    }

    public void openTimer() {
        Intent intent = new Intent(MainActivity.this, Timer.class);
        startActivity(intent);
    }

    class weatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.address).setVisibility(View.GONE);
            findViewById(R.id.date).setVisibility(View.GONE);
            findViewById(R.id.temp).setVisibility(View.GONE);
            findViewById(R.id.windSpeed).setVisibility(View.GONE);
            findViewById(R.id.pressure).setVisibility(View.GONE);
            findViewById(R.id.humidity).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric&appid=" + API_KEY);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                JSONObject sys = jsonObject.getJSONObject("sys");
                JSONObject wind = jsonObject.getJSONObject("wind");

                String updatedAtText = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date());
                String temp = main.getString("temp") + "°C";
                String pressure = main.getString("pressure") + " hPa";
                String humidity = main.getString("humidity") + "%";
                String windSpeed = wind.getString("speed") + " km/h";
                String address = jsonObject.getString("name") + ", " + sys.getString("country");

                addressTxt.setText(address);
                dateTxt.setText(updatedAtText);
                tempTxt.setText(temp);
                windTxt.setText(windSpeed);
                pressureTxt.setText(pressure);
                humidityTxt.setText(humidity);

                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.address).setVisibility(View.VISIBLE);
                findViewById(R.id.date).setVisibility(View.VISIBLE);
                findViewById(R.id.temp).setVisibility(View.VISIBLE);
                findViewById(R.id.windSpeed).setVisibility(View.VISIBLE);
                findViewById(R.id.pressure).setVisibility(View.VISIBLE);
                findViewById(R.id.humidity).setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }
        }
    }

    public void showBoxingDialog (Activity activity) {
        dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnStart = dialog.findViewById(R.id.button3);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimer();
                dialog.dismiss();
            }
        });

        Button btnExit = dialog.findViewById(R.id.exitBtn);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}