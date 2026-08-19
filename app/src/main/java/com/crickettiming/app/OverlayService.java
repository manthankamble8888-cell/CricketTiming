package com.crickettiming.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
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

import java.util.ArrayList;
import java.util.Locale;

public class OverlayService extends Service {

    private static final String CHANNEL_ID = "CricketTimingService";
    private static final int NOTIFICATION_ID = 1001;

    private WindowManager windowManager;
    private LinearLayout overlay;

    private TextView timerText;
    private TextView statusText;
    private TextView statsText;
    private EditText targetInput;

    private TimingBar timingBar;

    private final Handler handler = new Handler();

    private long startTime = 0;
    private boolean running = false;

    private double currentTime = 0.0;
    private double targetTime = 1.650;

    private final double perfectTolerance = 0.050;

    private final ArrayList<Double> shots = new ArrayList<>();

    private boolean overlayCreated = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if (!running || overlay == null) {
                return;
            }

            currentTime =
                    (SystemClock.elapsedRealtime() - startTime)
                            / 1000.0;

            if (timerText != null) {
                timerText.setText(
                        String.format(
                                Locale.US,
                                "%.3f s",
                                currentTime
                        )
                );
            }

            if (timingBar != null) {
                timingBar.setValues(
                        currentTime,
                        targetTime
                );
            }

            handler.postDelayed(this, 20);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        startForeground(
                NOTIFICATION_ID,
                createNotification()
        );

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        windowManager =
                (WindowManager) getSystemService(WINDOW_SERVICE);

        createOverlay();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Cricket Timing",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Keeps the cricket timing overlay running."
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            return new Notification.Builder(
                    this,
                    CHANNEL_ID
            )
                    .setContentTitle("Cricket Timing")
                    .setContentText("Timing overlay is running")
                    .setSmallIcon(
                            android.R.drawable.ic_media_play
                    )
                    .setOngoing(true)
                    .build();

        } else {

            return new Notification.Builder(this)
                    .setContentTitle("Cricket Timing")
                    .setContentText("Timing overlay is running")
                    .setSmallIcon(
                            android.R.drawable.ic_media_play
                    )
                    .setOngoing(true)
                    .build();
        }
    }

    private void createOverlay() {

        if (overlayCreated) {
            return;
        }

        overlay = new LinearLayout(this);

        overlay.setOrientation(
                LinearLayout.VERTICAL
        );

        overlay.setGravity(
                Gravity.CENTER
        );

        overlay.setPadding(
                18,
                14,
                18,
                14
        );

        overlay.setBackgroundColor(
                Color.rgb(17, 24, 39)
        );

        // TITLE
        TextView title = new TextView(this);

        title.setText("🏏 Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);

        overlay.addView(title);

        // TIMER
        timerText = new TextView(this);

        timerText.setText("0.000 s");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(28);
        timerText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        timerParams.setMargins(
                0,
                5,
                0,
                3
        );

        overlay.addView(
                timerText,
                timerParams
        );

        // TIMING BAR
        timingBar = new TimingBar();

        LinearLayout.LayoutParams barParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        45
                );

        barParams.setMargins(
                0,
                4,
                0,
                6
        );

        overlay.addView(
                timingBar,
                barParams
        );

        // STATUS
        statusText = new TextView(this);

        statusText.setText("Ready");
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(15);
        statusText.setGravity(Gravity.CENTER);

        overlay.addView(statusText);

        // TARGET LABEL
        TextView targetLabel = new TextView(this);

        targetLabel.setText(
                "Target (seconds)"
        );

        targetLabel.setTextColor(Color.WHITE);
        targetLabel.setTextSize(13);
        targetLabel.setGravity(Gravity.CENTER);

        overlay.addView(targetLabel);

        // TARGET INPUT
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

        // START
        Button startButton = new Button(this);

        startButton.setText("START");

        overlay.addView(startButton);

        // HIT
        Button hitButton = new Button(this);

        hitButton.setText("HIT");
        hitButton.setEnabled(false);

        overlay.addView(hitButton);

        // NEW SESSION
        Button newSessionButton = new Button(this);

        newSessionButton.setText(
                "NEW SESSION"
        );

        overlay.addView(newSessionButton);

        // CLOSE
        Button closeButton = new Button(this);

        closeButton.setText("CLOSE");

        overlay.addView(closeButton);

        // STATS
        statsText = new TextView(this);

        statsText.setText(
                "Shots: 0\nBest: --\nAverage: --"
        );

        statsText.setTextColor(Color.WHITE);
        statsText.setTextSize(15);
        statsText.setGravity(Gravity.CENTER);

        statsText.setPadding(
                0,
                8,
                0,
                0
        );

        overlay.addView(statsText);

        // WINDOW
        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        280,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity =
                Gravity.TOP | Gravity.RIGHT;

        params.x = 15;
        params.y = 80;

        params.softInputMode =
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

        try {

            windowManager.addView(
                    overlay,
                    params
            );

            overlayCreated = true;

        } catch (Exception e) {

            overlay = null;
            overlayCreated = false;

            stopSelf();
            return;
        }

        // START BUTTON
        startButton.setOnClickListener(v -> {

            readTarget();

            startTime =
                    SystemClock.elapsedRealtime();

            currentTime = 0.0;

            running = true;

            timerText.setText("0.000 s");

            statusText.setText(
                    "Timing..."
            );

            statusText.setTextColor(
                    Color.WHITE
            );

            timingBar.setValues(
                    0.0,
                    targetTime
            );

            startButton.setEnabled(false);
            hitButton.setEnabled(true);
            targetInput.setEnabled(false);

            handler.removeCallbacks(
                    timerRunnable
            );

            handler.post(
                    timerRunnable
            );
        });

        // HIT BUTTON
        hitButton.setOnClickListener(v -> {

            if (!running) {
                return;
            }

            currentTime =
                    (SystemClock.elapsedRealtime()
                            - startTime)
                            / 1000.0;

            running = false;

            handler.removeCallbacks(
                    timerRunnable
            );

            timerText.setText(
                    String.format(
                            Locale.US,
                            "%.3f s",
                            currentTime
                    )
            );

            timingBar.setValues(
                    currentTime,
                    targetTime
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

            handler.removeCallbacks(
                    timerRunnable
            );

            shots.clear();

            currentTime = 0.0;

            timerText.setText(
                    "0.000 s"
            );

            statusText.setText(
                    "Ready"
            );

            statusText.setTextColor(
                    Color.LTGRAY
            );

            timingBar.setValues(
                    0.0,
                    targetTime
            );

            startButton.setEnabled(true);
            hitButton.setEnabled(false);
            targetInput.setEnabled(true);

            updateStats();
        });

        // CLOSE
        closeButton.setOnClickListener(v -> {

            running = false;

            handler.removeCallbacks(
                    timerRunnable
            );

            stopSelf();
        });

        // DRAG
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

                    timingBar.setValues(
                            currentTime,
                            targetTime
                    );
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
                            "🟢 PERFECT  %.3f",
                            difference
                    )
            );

            statusText.setTextColor(
                    Color.rgb(
                            50,
                            220,
                            100
                    )
            );

        } else if (difference < 0) {

            statusText.setText(
                    String.format(
                            Locale.US,
                            "🔵 EARLY  %.3f",
                            Math.abs(difference)
                    )
            );

            statusText.setTextColor(
                    Color.rgb(
                            70,
                            170,
                            255
                    )
            );

        } else {

            statusText.setText(
                    String.format(
                            Locale.US,
                            "🔴 LATE  %.3f",
                            difference
                    )
            );

            statusText.setTextColor(
                    Color.rgb(
                            255,
                            80,
                            80
                    )
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

                        switch (
                                event.getAction()
                        ) {

                            case MotionEvent.ACTION_DOWN:

                                initialX =
                                        params.x;

                                initialY =
                                        params.y;

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

                                try {

                                    windowManager
                                            .updateViewLayout(
                                                    overlayView,
                                                    params
                                            );

                                } catch (Exception ignored) {
                                }

                                return true;

                            case MotionEvent.ACTION_UP:

                                return true;
                        }

                        return false;
                    }
                }
        );
    }

    // ============================================================
    // LIVE TIMING BAR
    // ============================================================

    private class TimingBar extends View {

        private final Paint paint;

        private double barTime = 0.0;
        private double barTarget = 1.650;

        public TimingBar() {

            super(
                    OverlayService.this
            );

            paint =
                    new Paint(
                            Paint.ANTI_ALIAS_FLAG
                    );
        }

        public void setValues(
                double current,
                double target
        ) {

            barTime = current;
            barTarget = target;

            invalidate();
        }

        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(canvas);

            float width =
                    getWidth();

            float height =
                    getHeight();

            float centerY =
                    height / 2f;

            double maximumTime =
                    barTarget * 1.30;

            if (maximumTime <= 0) {
                maximumTime = 2.0;
            }

            // Background
            paint.setColor(
                    Color.rgb(
                            70,
                            78,
                            95
                    )
            );

            paint.setStrokeWidth(
                    10f
            );

            canvas.drawLine(
                    10,
                    centerY,
                    width - 10,
                    centerY,
                    paint
            );

            // Target
            float targetX =
                    10 +
                    (float) (
                            (barTarget /
                
