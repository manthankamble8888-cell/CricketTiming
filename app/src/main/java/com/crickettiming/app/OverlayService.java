package com.crickettiming.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private LinearLayout overlay;

    private TextView timerText;
    private TextView statusText;
    private EditText targetInput;

    private Button startButton;
    private Button hitButton;
    private Button closeButton;

    private Handler handler = new Handler();

    private long startTime = 0;
    private boolean running = false;

    private double currentTime = 0.0;

    // Default target timing.
    private double targetTime = 1.650;

    // How close the hit must be to target to be PERFECT.
    private final double perfectTolerance = 0.050;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if (running) {
                currentTime =
                        (SystemClock.elapsedRealtime() - startTime) / 1000.0;

                timerText.setText(
                        String.format(Locale.US, "%.3f s", currentTime)
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
        overlay.setPadding(18, 18, 18, 18);
        overlay.setBackgroundColor(Color.rgb(17, 24, 39));

        // ---------------- TITLE ----------------

        TextView title = new TextView(this);
        title.setText("🏏 Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);

        overlay.addView(title);

        // ---------------- TIMER ----------------

        timerText = new TextView(this);
        timerText.setText("0.000 s");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(27);
        timerText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        timerParams.setMargins(0, 8, 0, 4);

        overlay.addView(timerText, timerParams);

        // ---------------- STATUS ----------------

        statusText = new TextView(this);
        statusText.setText("Ready");
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(16);
        statusText.setGravity(Gravity.CENTER);

        overlay.addView(statusText);

        // ---------------- TARGET LABEL ----------------

        TextView targetLabel = new TextView(this);
        targetLabel.setText("Target timing (seconds)");
        targetLabel.setTextColor(Color.WHITE);
        targetLabel.setTextSize(14);
        targetLabel.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        labelParams.setMargins(0, 8, 0, 2);

        overlay.addView(targetLabel, labelParams);

        // ---------------- TARGET INPUT ----------------

        targetInput = new EditText(this);
        targetInput.setText("1.650");
        targetInput.setTextColor(Color.WHITE);
        targetInput.setTextSize(16);
        targetInput.setGravity(Gravity.CENTER);
        targetInput.setSingleLine(true);
        targetInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        overlay.addView(targetInput);

        // ---------------- START ----------------

        startButton = new Button(this);
        startButton.setText("START");

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(0, 8, 0, 0);

        overlay.addView(startButton, buttonParams);

        // ---------------- HIT ----------------

        hitButton = new Button(this);
        hitButton.setText("HIT");
        hitButton.setEnabled(false);

        overlay.addView(hitButton, buttonParams);

        // ---------------- CLOSE ----------------

        closeButton = new Button(this);
        closeButton.setText("CLOSE");

        overlay.addView(closeButton, buttonParams);

        // ---------------- START BUTTON ----------------

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                readTarget();

                startTime = SystemClock.elapsedRealtime();

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
            }
        });

        // ---------------- HIT BUTTON ----------------

        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                showResult();

                startButton.setEnabled(true);
                hitButton.setEnabled(false);
                targetInput.setEnabled(true);
            }
        });

        // ---------------- CLOSE BUTTON ----------------

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                running = false;

                handler.removeCallbacks(timerRunnable);

                if (overlay != null) {
                    windowManager.removeView(overlay);
                }

                stopSelf();
            }
        });

        // ---------------- WINDOW ----------------

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        260,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.x = 15;
        params.y = 80;

        windowManager.addView(overlay, params);

        // ---------------- DRAG OVERLAY ----------------

        makeDraggable(overlay, params);
    }

    private void readTarget() {

        try {

            String value =
                    targetInput.getText().toString().trim();

            if (!value.isEmpty()) {

                double parsed =
                        Double.parseDouble(value);

                if (parsed > 0.0 && parsed < 20.0) {
                    targetTime = parsed;
                }
            }

        } catch (Exception ignored) {
        }
    }

    private void showResult() {

        double difference = currentTime - targetTime;

        double absoluteDifference = Math.abs(difference);

        if (absoluteDifference <= perfectTolerance) {

            statusText.setText(
                    String.format(
                            Locale.US,
                            "🟢 PERFECT  %.3f s",
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
                            "🔵 EARLY  %.3f s",
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
                            "🔴 LATE  %.3f s",
                            difference
                    )
            );

            statusText.setTextColor(
                    Color.rgb(255, 80, 80)
            );
        }
    }

    private void makeDraggable(
            View view,
            final WindowManager.LayoutParams params
    ) {

        view.setOnTouchListener(
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

                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();

                                return false;

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
                                        overlay,
                                        params
                                );

                                return false;
                        }

                        return false;
                    }
                }
        );
    }

    @Override
    public void onDestroy() {

        running = false;

        handler.removeCallbacks(timerRunnable);

        if (overlay != null) {

            try {
                windowManager.removeView(overlay);
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
