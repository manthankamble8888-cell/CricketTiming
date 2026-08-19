package com.crickettiming.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemClock;

public class MainActivity extends Activity {

    private TextView timerText;
    private long startTime;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        timerText.setTextSize(28);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 40, 0, 40);

        Button startButton = new Button(this);
        startButton.setText("START");

        Button hitButton = new Button(this);
        hitButton.setText("HIT");

        Button resetButton = new Button(this);
        resetButton.setText("RESET");

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = SystemClock.elapsedRealtime();
                running = true;
                timerText.setText("Timing...");
            }
        });

        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    long elapsed = SystemClock.elapsedRealtime() - startTime;
                    double seconds = elapsed / 1000.0;

                    timerText.setText(String.format("%.3f seconds", seconds));
                    running = false;
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = false;
                timerText.setText("0.000 seconds");
            }
        });

        layout.addView(title);
        layout.addView(timerText);
        layout.addView(startButton);
        layout.addView(hitButton);
        layout.addView(resetButton);

        setContentView(layout);
    }
}
