package com.crickettiming.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.InputType;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView timerText;
    private TextView statusText;
    private TextView resultsText;
    private EditText targetInput;

    private long startTime = 0;
    private boolean timing = false;

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
        title.setTextSize(34);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 20);

        timerText = new TextView(this);
        timerText.setText("0.000 seconds");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(38);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 20, 0, 10);

        statusText = new TextView(this);
        statusText.setText("Set your target time");
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(22);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 10, 0, 20);

        targetInput = new EditText(this);
        targetInput.setHint("Target time (e.g. 1.650)");
        targetInput.setHintTextColor(Color.GRAY);
        targetInput.setTextColor(Color.WHITE);
        targetInput.setTextSize(20);
        targetInput.setGravity(Gravity.CENTER);
        targetInput.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        Button startButton = new Button(this);
        startButton.setText("START");

        Button hitButton = new Button(this);
        hitButton.setText("HIT");

        Button newSessionButton = new Button(this);
        newSessionButton.setText("NEW SESSION");

        resultsText = new TextView(this);
        resultsText.setText("Results");
        resultsText.setTextColor(Color.WHITE);
        resultsText.setTextSize(22);
        resultsText.setPadding(0, 25, 0, 10);

        layout.addView(title);
        layout.addView(timerText);
        layout.addView(statusText);
        layout.addView(targetInput);
        layout.addView(startButton);
        layout.addView(hitButton);
        layout.addView(newSessionButton);
        layout.addView(resultsText);

        setContentView(layout);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hitTimer();
            }
        });

        newSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSession();
            }
        });
    }

    private void startTimer() {
        startTime = System.nanoTime();
        timing = true;

        statusText.setText("Timing...");

        timerText.post(new Runnable() {
            @Override
            public void run() {
                if (timing) {
                    double seconds =
                            (System.nanoTime() - startTime) / 1_000_000_000.0;

                    timerText.setText(
                            String.format(Locale.US, "%.3f seconds", seconds)
                    );

                    timerText.postDelayed(this, 20);
                }
            }
        });
    }

    private void hitTimer() {
        if (!timing) {
            return;
        }

        double seconds =
                (System.nanoTime() - startTime) / 1_000_000_000.0;

        timing = false;

        timerText.setText(
                String.format(Locale.US, "%.3f seconds", seconds)
        );

        shots.add(seconds);

        double target = getTargetTime();

        if (target > 0) {
            double difference = seconds - target;

            if (Math.abs(difference) < 0.001) {
                statusText.setText("🎯 PERFECT!");
            } else if (difference < 0) {
                statusText.setText(
                        String.format(
                                Locale.US,
                                "⚡ %.3f s early",
                                Math.abs(difference)
                        )
                );
            } else {
                statusText.setText(
                        String.format(
                                Locale.US,
                                "⏱ %.3f s late",
                                difference
                        )
                );
            }
        } else {
            statusText.setText("HIT recorded");
        }

        updateResults();
    }

    private double getTargetTime() {
        String text = targetInput.getText().toString().trim();

        if (text.isEmpty()) {
            return 0;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateResults() {
        StringBuilder result = new StringBuilder();

        result.append("Results\n\n");

        for (int i = 0; i < shots.size(); i++) {
            result.append(
                    String.format(
                            Locale.US,
                            "Shot %d — %.3f s\n",
                            i + 1,
                            shots.get(i)
                    )
            );
        }

        if (!shots.isEmpty()) {
            double best = shots.get(0);
            double total = 0;

            for (double shot : shots) {
                if (shot < best) {
                    best = shot;
                }
                total += shot;
            }

            double average = total / shots.size();

            result.append("\n");
            result.append(
                    String.format(
                            Locale.US,
                            "Best — %.3f s\n",
                            best
                    )
            );

            result.append(
                    String.format(
                            Locale.US,
                            "Average — %.3f s",
                            average
                    )
            );
        }

        resultsText.setText(result.toString());
    }

    private void newSession() {
        timing = false;
        startTime = 0;

        shots.clear();

        timerText.setText("0.000 seconds");
        statusText.setText("Set your target time");
        resultsText.setText("Results");
    }
}
