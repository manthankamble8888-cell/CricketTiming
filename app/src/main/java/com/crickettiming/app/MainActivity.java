package com.crickettiming.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView timerText;
    private TextView statusText;

    private Handler handler = new Handler();
    private long startTime = 0;
    private boolean running = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                long elapsed = SystemClock.elapsedRealtime() - startTime;

                double seconds = elapsed / 1000.0;

                timerText.setText(String.format("%.3f seconds", seconds));

                handler.postDelayed(this, 10);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Main layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.rgb(17, 24, 39));

        // Title
        TextView title = new TextView(this);
        title.setText("🏏 Cricket Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        // Timer
        timerText = new TextView(this);
        timerText.setText("0.000 seconds");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(40);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 40, 0, 40);

        // Status
        statusText = new TextView(this);
        statusText.setText("Ready");
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(18);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 10, 0, 30);

        // START button
        Button startButton = new Button(this);
        startButton.setText("START");

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startTime = SystemClock.elapsedRealtime();
                running = true;

                statusText.setText("Timing...");

                handler.removeCallbacks(timerRunnable);
                handler.post(timerRunnable);
            }
        });

        // HIT button
        Button hitButton = new Button(this);
        hitButton.setText("HIT");

        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (running) {

                    long elapsed =
                            SystemClock.elapsedRealtime() - startTime;

                    double seconds = elapsed / 1000.0;

                    timerText.setText(
                            String.format("%.3f seconds", seconds)
                    );

                    statusText.setText("HIT recorded");

                    running = false;
                    handler.removeCallbacks(timerRunnable);
                }
            }
        });

        // RESET button
        Button resetButton = new Button(this);
        resetButton.setText("RESET");

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                running = false;
                handler.removeCallbacks(timerRunnable);

                startTime = 0;

                timerText.setText("0.000 seconds");
                statusText.setText("Ready");
            }
        });

        // Button spacing
        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(0, 10, 0, 10);

        layout.addView(title);
        layout.addView(timerText);
        layout.addView(statusText);

        layout.addView(startButton, buttonParams);
        layout.addView(hitButton, buttonParams);
        layout.addView(resetButton, buttonParams);

        setContentView(layout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        running = false;
        handler.removeCallbacks(timerRunnable);
    }
}
