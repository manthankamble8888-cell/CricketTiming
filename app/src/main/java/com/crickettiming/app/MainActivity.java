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
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView timerText;
    private TextView statusText;
    private TextView resultsText;

    private Handler handler = new Handler();

    private long startTime = 0;
    private boolean running = false;

    private ArrayList<Double> results = new ArrayList<>();

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if (running) {

                long elapsed =
                        SystemClock.elapsedRealtime() - startTime;

                double seconds = elapsed / 1000.0;

                timerText.setText(
                        String.format(Locale.US,
                                "%.3f seconds",
                                seconds)
                );

                handler.postDelayed(this, 10);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(40, 50, 40, 40);
        layout.setBackgroundColor(Color.rgb(17, 24, 39));

        // TITLE
        TextView title = new TextView(this);
        title.setText("🏏 Cricket Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        // TIMER
        timerText = new TextView(this);
        timerText.setText("0.000 seconds");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(40);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 35, 0, 20);

        // STATUS
        statusText = new TextView(this);
        statusText.setText("Ready");
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(18);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 0, 0, 25);

        // START BUTTON
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

        // HIT BUTTON
        Button hitButton = new Button(this);
        hitButton.setText("HIT");

        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!running) {
                    statusText.setText("Press START first");
                    return;
                }

                long elapsed =
                        SystemClock.elapsedRealtime() - startTime;

                double seconds = elapsed / 1000.0;

                timerText.setText(
                        String.format(Locale.US,
                                "%.3f seconds",
                                seconds)
                );

                results.add(seconds);

                running = false;
                handler.removeCallbacks(timerRunnable);

                statusText.setText("HIT recorded");

                updateResults();
            }
        });

        // RESET BUTTON
        Button resetButton = new Button(this);
        resetButton.setText("RESET");

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                running = false;

                handler.removeCallbacks(timerRunnable);

                startTime = 0;

                results.clear();

                timerText.setText("0.000 seconds");
                statusText.setText("Ready");

                updateResults();
            }
        });

        // RESULTS TITLE
        TextView resultsTitle = new TextView(this);
        resultsTitle.setText("Results");
        resultsTitle.setTextColor(Color.WHITE);
        resultsTitle.setTextSize(24);
        resultsTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        resultsTitle.setPadding(0, 30, 0, 10);

        // RESULTS
        resultsText = new TextView(this);
        resultsText.setText("No shots recorded yet.");
        resultsText.setTextColor(Color.WHITE);
        resultsText.setTextSize(18);
        resultsText.setPadding(0, 5, 0, 30);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(0, 8, 0, 8);

        layout.addView(title);
        layout.addView(timerText);
        layout.addView(statusText);

        layout.addView(startButton, buttonParams);
        layout.addView(hitButton, buttonParams);
        layout.addView(resetButton, buttonParams);

        layout.addView(resultsTitle);
        layout.addView(resultsText);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        setContentView(scrollView);
    }

    private void updateResults() {

        if (results.size() == 0) {
            resultsText.setText("No shots recorded yet.");
            return;
        }

        StringBuilder text = new StringBuilder();

        double total = 0;
        double best = Double.MAX_VALUE;

        for (int i = 0; i < results.size(); i++) {

            double time = results.get(i);

            total += time;

            if (time < best) {
                best = time;
            }

            text.append("Shot ")
                    .append(i + 1)
                    .append(" — ")
                    .append(String.format(Locale.US,
                            "%.3f s",
                            time))
                    .append("\n");
        }

        double average = total / results.size();

        text.append("\n");
        text.append("Best — ")
                .append(String.format(Locale.US,
                        "%.3f s",
                        best))
                .append("\n");

        text.append("Average — ")
                .append(String.format(Locale.US,
                        "%.3f s",
                        average));

        resultsText.setText(text.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        running = false;
        handler.removeCallbacks(timerRunnable);
    }
                            }
