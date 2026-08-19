package com.crickettiming.app;

import android.app.Activity;
import android.os.Bundle;
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

    private long startTime = 0;
    private boolean running = false;

    private final ArrayList<Double> shots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.rgb(17, 24, 39));

        TextView title = new TextView(this);
        title.setText("🏏 Cricket Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);

        timerText = new TextView(this);
        timerText.setText("0.000 seconds");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(42);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 40, 0, 20);

        statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(22);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 0, 0, 20);

        Button startButton = new Button(this);
        startButton.setText("START");

        Button hitButton = new Button(this);
        hitButton.setText("HIT");

        Button resetButton = new Button(this);
        resetButton.setText("NEW SESSION");

        resultsText = new TextView(this);
        resultsText.setTextColor(Color.WHITE);
        resultsText.setTextSize(20);
        resultsText.setPadding(0, 30, 0, 0);

        layout.addView(title);
        layout.addView(timerText);
        layout.addView(statusText);
        layout.addView(startButton);
        layout.addView(hitButton);
        layout.addView(resetButton);
        layout.addView(resultsText);

        setContentView(layout);

        startButton.setOnClickListener(v -> {
            startTime = SystemClock.elapsedRealtime();
            running = true;

            timerText.setText("0.000 seconds");
            statusText.setText("Timing...");
        });

        hitButton.setOnClickListener(v -> {
            if (!running) {
                return;
            }

            long elapsed = SystemClock.elapsedRealtime() - startTime;
            double seconds = elapsed / 1000.0;

            shots.add(seconds);
            running = false;

            timerText.setText(String.format(
                    Locale.US, "%.3f seconds", seconds
            ));

            statusText.setText("HIT recorded");

            updateResults();
        });

        resetButton.setOnClickListener(v -> {
            shots.clear();
            running = false;
            startTime = 0;

            timerText.setText("0.000 seconds");
            statusText.setText("");
            resultsText.setText("");
        });
    }

    private void updateResults() {

        if (shots.isEmpty()) {
            resultsText.setText("");
            return;
        }

        double best = shots.get(0);
        double total = 0;

        StringBuilder text = new StringBuilder();
        text.append("Results\n\n");

        for (int i = 0; i < shots.size(); i++) {

            double shot = shots.get(i);

            if (shot < best) {
                best = shot;
            }

            total += shot;

            text.append("Shot ")
                .append(i + 1)
                .append(" — ")
                .append(String.format(Locale.US, "%.3f", shot))
                .append(" s\n");
        }

        double average = total / shots.size();

        text.append("\nBest — ")
            .append(String.format(Locale.US, "%.3f", best))
            .append(" s\n");

        text.append("Average — ")
            .append(String.format(Locale.US, "%.3f", average))
            .append(" s");

        resultsText.setText(text.toString());
    }
}
