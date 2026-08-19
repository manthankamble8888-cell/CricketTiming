package com.crickettiming.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView timerText;
    private TextView statusText;
    private TextView resultsText;

    private Button startButton;
    private Button hitButton;
    private Button sessionButton;

    private Handler handler = new Handler();

    private boolean running = false;
    private long startTime = 0;

    private ArrayList<Double> shots = new ArrayList<>();

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                double seconds =
                        (SystemClock.elapsedRealtime() - startTime) / 1000.0;

                timerText.setText(
                        String.format(Locale.US, "%.3f seconds", seconds)
                );

                handler.postDelayed(this, 16);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        layout.setPadding(40, 50, 40, 40);
        layout.setBackgroundColor(Color.rgb(17, 24, 39));

        // TITLE
        TextView title = new TextView(this);
        title.setText("🏏 Cricket Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(38);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 35);

        // TIMER
        timerText = new TextView(this);
        timerText.setText("0.000 seconds");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(42);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 0, 0, 20);

        // STATUS
        statusText = new TextView(this);
        statusText.setText("Ready");
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(24);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 0, 0, 35);

        // START BUTTON
        startButton = new Button(this);
        startButton.setText("START");
        startButton.setTextSize(20);

        // HIT BUTTON
        hitButton = new Button(this);
        hitButton.setText("HIT");
        hitButton.setTextSize(20);

        // NEW SESSION BUTTON
        sessionButton = new Button(this);
        sessionButton.setText("NEW SESSION");
        sessionButton.setTextSize(20);

        // RESULTS
        resultsText = new TextView(this);
        resultsText.setText("Results");
        resultsText.setTextColor(Color.WHITE);
        resultsText.setTextSize(24);
        resultsText.setPadding(0, 40, 0, 0);

        // ADD VIEWS
        layout.addView(title);
        layout.addView(timerText);
        layout.addView(statusText);

        layout.addView(startButton);
        layout.addView(hitButton);
        layout.addView(sessionButton);

        layout.addView(resultsText);

        setContentView(layout);

        // START
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!running) {

                    startTime = SystemClock.elapsedRealtime();
                    running = true;

                    statusText.setText("Timing...");

                    handler.removeCallbacks(timerRunnable);
                    handler.post(timerRunnable);
                }
            }
        });

        // HIT
        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (running) {

                    long elapsed =
                            SystemClock.elapsedRealtime() - startTime;

                    double seconds = elapsed / 1000.0;

                    running = false;

                    handler.removeCallbacks(timerRunnable);

                    timerText.setText(
                            String.format(
                                    Locale.US,
                                    "%.3f seconds",
                                    seconds
                            )
                    );

                    statusText.setText("HIT recorded");

                    shots.add(seconds);

                    updateResults();
                }
            }
        });

        // NEW SESSION
        sessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                running = false;

                handler.removeCallbacks(timerRunnable);

                shots.clear();

                timerText.setText("0.000 seconds");
                statusText.setText("Ready");

                resultsText.setText("Results");
            }
        });
    }

    private void updateResults() {

        StringBuilder text = new StringBuilder();

        text.append("Results\n\n");

        double total = 0;
        double best = Double.MAX_VALUE;

        for (int i = 0; i < shots.size(); i++) {

            double shot = shots.get(i);

            total += shot;

            if (shot < best) {
                best = shot;
            }

            text.append("Shot ")
                    .append(i + 1)
                    .append(" — ")
                    .append(String.format(
                            Locale.US,
                            "%.3f",
                            shot
                    ))
                    .append(" s\n");
        }

        if (!shots.isEmpty()) {

            double average = total / shots.size();

            text.append("\nBest — ")
                    .append(String.format(
                            Locale.US,
                            "%.3f",
                            best
                    ))
                    .append(" s\n");

            text.append("Average — ")
                    .append(String.format(
                            Locale.US,
                            "%.3f",
                            average
                    ))
                    .append(" s");
        }

        resultsText.setText(text.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        running = false;
        handler.removeCallbacks(timerRunnable);
    }
}
