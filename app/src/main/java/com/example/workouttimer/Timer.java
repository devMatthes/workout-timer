package com.example.workouttimer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androdocs.httprequest.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Timer extends AppCompatActivity {
    String CITY = "wroclaw,pl";
    String API_KEY = "e83e18c6808093d5149fb0519759662c";

    private static final long START_IN_MILLIS = 10000;
    private static final long BREAK_IN_MILLIS = 5000;
    private static final int SETS = 5;
    private Dialog dialog;
    private TextView mCountdownTextView;
    private TextView mSetsTextView;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private Button mButtonShowWorkout;
    private int mCurrSet = 0;
    private ToneGenerator toneG;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis = START_IN_MILLIS;
    private long mBreakTimeLeftInMillis = BREAK_IN_MILLIS;

    TextView addressTxt, dateTxt, tempTxt, windTxt, pressureTxt, humidityTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer);

        addressTxt = findViewById(R.id.address);
        dateTxt = findViewById(R.id.date);
        tempTxt = findViewById(R.id.temp);
        windTxt = findViewById(R.id.windSpeed);
        pressureTxt = findViewById(R.id.pressure);
        humidityTxt = findViewById(R.id.humidity);

        mCountdownTextView = findViewById(R.id.timerTxtV);
        mSetsTextView = findViewById(R.id.setsTxtVw);
        mButtonStartPause = findViewById(R.id.startBtn);
        mButtonReset = findViewById(R.id.resetBtn);
        mButtonShowWorkout = findViewById(R.id.btn_showWorkout);
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        mButtonShowWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBoxingDialog(Timer.this);
            }
        });

        updateCountDownText(mTimeLeftInMillis);
        new weatherTask().execute();
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

    private void startTimer() {
        updateSetsText();
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                if (mTimeLeftInMillis <= 6000 && mTimeLeftInMillis > 1000) {
                    toneG.startTone(ToneGenerator.TONE_CDMA_PIP, 200);
                }
                else if (mTimeLeftInMillis <= 1000) {
                    toneG.startTone(ToneGenerator.TONE_SUP_BUSY, 1000);
                }
                updateCountDownText(mTimeLeftInMillis);
            }

            @Override
            public void onFinish() {
                mCurrSet++;
                breakTimer();
                resetStartTimer();
            }
        }.start();

        mTimerRunning = true;
        mButtonStartPause.setBackgroundResource(R.drawable.ic_pause_timer);
        mButtonReset.setVisibility(View.INVISIBLE);
    }

    private void breakTimer() {
        mSetsTextView.setText("BREAK");
        mCountDownTimer = new CountDownTimer(mBreakTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mBreakTimeLeftInMillis = millisUntilFinished;
                if (mBreakTimeLeftInMillis <= 4000 && mBreakTimeLeftInMillis > 1000) {
                    toneG.startTone(ToneGenerator.TONE_CDMA_PIP, 200);
                }
                else if (mBreakTimeLeftInMillis <= 1000) {
                    toneG.startTone(ToneGenerator.TONE_SUP_BUSY, 1000);
                }
                updateCountDownText(mBreakTimeLeftInMillis);
            }

            @Override
            public void onFinish() {
                if (mCurrSet < SETS) {
                    startTimer();
                    resetBreakTimer();
                } else {
                    mTimerRunning = false;
                    mSetsTextView.setText("SETS LEFT: 0");
                    mButtonStartPause.setBackgroundResource(R.drawable.ic_start_timer);
                    mButtonStartPause.setVisibility(View.INVISIBLE);
                    mButtonReset.setVisibility(View.VISIBLE);
                }
            }
        }.start();

        mTimerRunning = true;
        mButtonStartPause.setBackgroundResource(R.drawable.ic_pause_timer);
        mButtonReset.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        mButtonStartPause.setBackgroundResource(R.drawable.ic_start_timer);
        mButtonReset.setVisibility(View.VISIBLE);
    }

    private void resetStartTimer() {
        mTimeLeftInMillis = START_IN_MILLIS;
        updateCountDownText(mTimeLeftInMillis);
        mButtonReset.setVisibility(View.INVISIBLE);
        mButtonStartPause.setVisibility(View.VISIBLE);
    }

    private void resetBreakTimer() {
        mBreakTimeLeftInMillis = BREAK_IN_MILLIS;
        updateCountDownText(mBreakTimeLeftInMillis);
        mButtonReset.setVisibility(View.INVISIBLE);
        mButtonStartPause.setVisibility(View.VISIBLE);
    }

    private void updateCountDownText(long milliseconds) {
        int minutes = (int) (milliseconds / 1000) / 60;
        int seconds = (int) (milliseconds / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mCountdownTextView.setText(timeLeftFormatted);
    }

    private void updateSetsText() {
        int setsLeft = SETS - mCurrSet;
        String setsLeftFormatted = String.format(Locale.getDefault(), "SETS LEFT: %d", setsLeft);
        mSetsTextView.setText(setsLeftFormatted);
    }

    private void resetTimer() {
        mCurrSet = 0;
        updateSetsText();
        mTimeLeftInMillis = START_IN_MILLIS;
        mBreakTimeLeftInMillis = BREAK_IN_MILLIS;
        updateCountDownText(mTimeLeftInMillis);
        mButtonReset.setVisibility(View.INVISIBLE);
        mButtonStartPause.setVisibility(View.VISIBLE);
    }

    public void showBoxingDialog (Activity activity) {
        dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnStart = dialog.findViewById(R.id.button3);
        btnStart.setVisibility(View.INVISIBLE);

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