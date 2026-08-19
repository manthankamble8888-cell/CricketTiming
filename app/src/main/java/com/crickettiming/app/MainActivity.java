package com.crickettiming.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView timerText;
    private Handler handler;
    private long startTime = 0;
    private boolean running = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                long elapsed = System.nanoTime() - startTime;
                double seconds = elapsed / 1_000_000_000.0;

                timerText.setText(String.format("%.3f seconds", seconds));

                handler.postDelayed(this, 10);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(Looper.getMainLooper());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.rgb(17, 24, 39));

        TextView title = new TextView(this);
        title.setText("🏏 Cricket Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);

        timerText = new TextView(this);
        timerText.setText("0.000 seconds");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(30);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 40, 0, 40);

        Button startButton = new Button(this);
        startButton.setText("START");

        Button hitButton = new Button(this);
        hitButton.setText("HIT");

        Button resetButton = new Button(this);
        resetButton.setText("RESET");

        startButton.setOnClickListener(v -> {
            startTime = System.nanoTime();
            running = true;
            handler.removeCallbacks(timerRunnable);
            handler.post(timerRunnable);
        });

        hitButton.setOnClickListener(v -> {
            if (running) {
                running = false;

                long elapsed = System.nanoTime() - startTime;
                double seconds = elapsed / 1_000_000_000.0;

                timerText.setText(String.format("%.3f seconds", seconds));
            }
        });

        resetButton.setOnClickListener(v -> {
            running = false;
            handler.removeCallbacks(timerRunnable);
            timerText.setText("0.000 seconds");
        });

        layout.addView(title);
        layout.addView(timerText);
        layout.addView(startButton);
        layout.addView(hitButton);
        layout.addView(resetButton);

        setContentView(layout);
    }
}
