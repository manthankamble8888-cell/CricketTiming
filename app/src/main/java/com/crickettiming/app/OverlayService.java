package com.crickettiming.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private LinearLayout overlay;

    private TextView timerText;
    private TextView statusText;
    private TextView statsText;

    private EditText targetInput;

    private Handler handler = new Handler();

    private long startTime = 0;
    private boolean running = false;

    private double currentTime = 0.0;
    private double targetTime = 1.650;

    private final double perfectTolerance = 0.050;

    private final ArrayList<Double> shots = new ArrayList<>();

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if (running) {

                currentTime =
                        (SystemClock.elapsedRealtime() - startTime)
                                / 1000.0;

                timerText.setText(
                        String.format(
                                Locale.US,
                                "%.3f s",
                                currentTime
                        )
                );

                handler.postDelayed(this, 20);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        windowManager =
                (WindowManager) getSystemService(WINDOW_SERVICE);

        createOverlay();
    }

    private void createOverlay() {

        overlay = new LinearLayout(this);

        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(10, 8, 10, 8);

        overlay.setBackgroundColor(
                Color.rgb(17, 24, 39)
        );

        // TITLE
        TextView title = new TextView(this);

        title.setText("🏏 Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER);

        overlay.addView(title);

        // TIMER
        timerText = new TextView(this);

        timerText.setText("0.000 s");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(22);
        timerText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        timerParams.setMargins(0, 2, 0, 1);

        overlay.addView(timerText, timerParams);

        // STATUS
        statusText = new TextView(this);

        statusText.setText("Ready");
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(12);
        statusText.setGravity(Gravity.CENTER);

        overlay.addView(statusText);

        // TARGET LABEL
        TextView targetLabel = new TextView(this);

        targetLabel.setText("Target (seconds)");
        targetLabel.setTextColor(Color.WHITE);
        targetLabel.setTextSize(10);
        targetLabel.setGravity(Gravity.CENTER);

        overlay.addView(targetLabel);

        // TARGET INPUT
        targetInput = new EditText(this);

        targetInput.setText("1.650");
        targetInput.setTextColor(Color.WHITE);
        targetInput.setTextSize(13);
        targetInput.setGravity(Gravity.CENTER);
        targetInput.setSingleLine(true);

        targetInput.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        38
                );

        overlay.addView(targetInput, inputParams);

        // START BUTTON
        Button startButton = new Button(this);

        startButton.setText("START");
        startButton.setTextSize(12);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        42
                );

        buttonParams.setMargins(0, 2, 0, 2);

        overlay.addView(startButton, buttonParams);

        // HIT BUTTON
        Button hitButton = new Button(this);

        hitButton.setText("HIT");
        hitButton.setTextSize(12);
        hitButton.setEnabled(false);

        overlay.addView(hitButton, buttonParams);

        // NEW SESSION BUTTON
        Button newSessionButton = new Button(this);

        newSessionButton.setText("NEW SESSION");
        newSessionButton.setTextSize(12);

        overlay.addView(newSessionButton, buttonParams);

        // CLOSE BUTTON
        Button closeButton = new Button(this);

        closeButton.setText("CLOSE");
        closeButton.setTextSize(12);

        overlay.addView(closeButton, buttonParams);

        // STATS
        statsText = new TextView(this);

        statsText.setText(
                "Shots: 0\nBest: --\nAverage: --"
        );

        statsText.setTextColor(Color.WHITE);
        statsText.setTextSize(11);
        statsText.setGravity(Gravity.CENTER);
        statsText.setPadding(0, 3, 0, 0);

        overlay.addView(statsText);

        // WINDOW
        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        210,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity =
                Gravity.TOP | Gravity.RIGHT;

        params.x = 8;
        params.y = 70;

        windowManager.addView(
                overlay,
                params
        );

        // START
        startButton.setOnClickListener(v -> {

            readTarget();

            startTime =
                    SystemClock.elapsedRealtime();

            currentTime = 0.0;

            running = true;

            timerText.setText("0.000 s");

            statusText.setText("Timing...");
            statusText.setTextColor(Color.WHITE);

            startButton.setEnabled(false);
            hitButton.setEnabled(true);

            targetInput.setEnabled(false);

            handler.removeCallbacks(timerRunnable);
            handler.post(timerRunnable);
        });

        // HIT
        hitButton.setOnClickListener(v -> {

            if (!running) {
                return;
            }

            currentTime =
                    (SystemClock.elapsedRealtime() - startTime)
                            / 1000.0;

            running = false;

            handler.removeCallbacks(timerRunnable);

            timerText.setText(
                    String.format(
                            Locale.US,
                            "%.3f s",
                            currentTime
                    )
            );

            shots.add(currentTime);

            showResult();

            updateStats();

            startButton.setEnabled(true);
            hitButton.setEnabled(false);

            targetInput.setEnabled(true);
        });

        // NEW SESSION
        newSessionButton.setOnClickListener(v -> {

            running = false;

            handler.removeCallbacks(timerRunnable);

            shots.clear();

            currentTime = 0.0;

            timerText.setText("0.000 s");

            statusText.setText("Ready");
            statusText.setTextColor(Color.LTGRAY);

            startButton.setEnabled(true);
            hitButton.setEnabled(false);

            targetInput.setEnabled(true);

            updateStats();
        });

        // CLOSE
        closeButton.setOnClickListener(v -> {

            running = false;

            handler.removeCallbacks(timerRunnable);

            stopSelf();
        });

        // DRAG OVERLAY
        makeDraggable(
                title,
                overlay,
                params
        );
    }

    private void readTarget() {

        try {

            String value =
                    targetInput
                            .getText()
                            .toString()
                            .trim();

            if (!value.isEmpty()) {

                double parsed =
                        Double.parseDouble(value);

                if (parsed > 0.0 &&
                        parsed < 20.0) {

                    targetTime = parsed;
                }
            }

        } catch (Exception ignored) {
        }
    }

    private void showResult() {

        double difference =
                currentTime - targetTime;

        double absoluteDifference =
                Math.abs(difference);

        if (absoluteDifference <=
                perfectTolerance) {

            statusText.setText(
                    String.format(
                            Locale.US,
                            "🟢 PERFECT %.3f",
                            difference
                    )
            );

            statusText.setTextColor(
                    Color.rgb(50, 220, 100)
            );

        } else if (difference < 0) {

            statusText.setText(
                    String.format(
                            Locale.US,
                            "🔵 EARLY %.3f",
                            Math.abs(difference)
                    )
            );

            statusText.setTextColor(
                    Color.rgb(70, 170, 255)
            );

        } else {

            statusText.setText(
                    String.format(
                            Locale.US,
                            "🔴 LATE %.3f",
                            difference
                    )
            );

            statusText.setTextColor(
                    Color.rgb(255, 80, 80)
            );
        }
    }

    private void updateStats() {

        if (shots.isEmpty()) {

            statsText.setText(
                    "Shots: 0\nBest: --\nAverage: --"
            );

            return;
        }

        double total = 0.0;
        double best = shots.get(0);

        for (double shot : shots) {

            total += shot;

            if (shot < best) {
                best = shot;
            }
        }

        double average =
                total / shots.size();

        statsText.setText(
                String.format(
                        Locale.US,
                        "Shots: %d\nBest: %.3f s\nAverage: %.3f s",
                        shots.size(),
                        best,
                        average
                )
        );
    }

    private void makeDraggable(
            View dragView,
            View overlayView,
            WindowManager.LayoutParams params
    ) {

        dragView.setOnTouchListener(
                new View.OnTouchListener() {

                    private int initialX;
                    private int initialY;

                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event
                    ) {

                        switch (event.getAction()) {

                            case MotionEvent.ACTION_DOWN:

                                initialX = params.x;
                                initialY = params.y;

                                initialTouchX =
                                        event.getRawX();

                                initialTouchY =
                                        event.getRawY();

                                return true;

                            case MotionEvent.ACTION_MOVE:

                                params.x =
                                        initialX +
                                        (int) (
                                                initialTouchX -
                                                event.getRawX()
                                        );

                                params.y =
                                        initialY +
                                        (int) (
                                                event.getRawY() -
                                                initialTouchY
                                        );

                                windowManager.updateViewLayout(
                                        overlayView,
                                        params
                                );

                                return true;

                            case MotionEvent.ACTION_UP:

                                return true;
                        }

                        return false;
                    }
                }
        );
    }

    @Override
    public void onDestroy() {

        running = false;

        handler.removeCallbacks(
                timerRunnable
        );

        if (overlay != null &&
                windowManager != null) {

            try {

                windowManager.removeView(
                        overlay
                );

            } catch (Exception ignored) {
            }

            overlay = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
                }
