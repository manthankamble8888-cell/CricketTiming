package com.crickettiming.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams params;

    private TextView timerText;
    private TextView statusText;

    private final Handler handler = new Handler();

    private long startTime = 0;
    private boolean running = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if (running) {

                long elapsed = System.nanoTime() - startTime;
                double seconds = elapsed / 1_000_000_000.0;

                timerText.setText(
                        String.format(Locale.US, "%.3f s", seconds)
                );

                handler.postDelayed(this, 50);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Notification notification =
                new Notification.Builder(this, "cricket_timing")
                        .setContentTitle("Cricket Timing")
                        .setContentText("Floating timing overlay is running")
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setOngoing(true)
                        .build();

        startForeground(1001, notification);

        windowManager =
                (WindowManager) getSystemService(WINDOW_SERVICE);

        createOverlay();
    }

    private void createOverlay() {

        LinearLayout box = new LinearLayout(this);

        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(18, 12, 18, 12);

        box.setBackgroundColor(
                Color.rgb(25, 35, 50)
        );

        // Title
        TextView title = new TextView(this);

        title.setText("🏏 Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);

        // Timer
        timerText = new TextView(this);

        timerText.setText("0.000 s");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(22);
        timerText.setGravity(Gravity.CENTER);

        // Status
        statusText = new TextView(this);

        statusText.setText("Ready");
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);

        // START button
        Button startButton = new Button(this);

        startButton.setText("START");

        // HIT button
        Button hitButton = new Button(this);

        hitButton.setText("HIT");

        // STOP button
        Button stopButton = new Button(this);

        stopButton.setText("CLOSE");

        box.addView(title);
        box.addView(timerText);
        box.addView(statusText);
        box.addView(startButton);
        box.addView(hitButton);
        box.addView(stopButton);

        overlayView = box;

        int overlayType;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType =
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            overlayType =
                    WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity =
                Gravity.TOP | Gravity.START;

        params.x = 30;
        params.y = 180;

        windowManager.addView(
                overlayView,
                params
        );

        // START
        startButton.setOnClickListener(v -> {

            startTime = System.nanoTime();

            running = true;

            statusText.setText("Timing...");

            timerText.setText("0.000 s");

            handler.removeCallbacks(timerRunnable);

            handler.post(timerRunnable);
        });

        // HIT
        hitButton.setOnClickListener(v -> {

            if (!running) {
                return;
            }

            long elapsed =
                    System.nanoTime() - startTime;

            double seconds =
                    elapsed / 1_000_000_000.0;

            running = false;

            handler.removeCallbacks(timerRunnable);

            timerText.setText(
                    String.format(
                            Locale.US,
                            "%.3f s",
                            seconds
                    )
            );

            statusText.setText("HIT recorded");
        });

        // CLOSE
        stopButton.setOnClickListener(v -> {

            stopSelf();
        });

        // Drag the overlay
        box.setOnTouchListener(
                new View.OnTouchListener() {

                    private int initialX;
                    private int initialY;

                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event) {

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
                                                        event.getRawX()
                                                                - initialTouchX
                                                );

                                params.y =
                                        initialY +
                                                (int) (
                                                        event.getRawY()
                                                                - initialTouchY
                                                );

                                windowManager.updateViewLayout(
                                        overlayView,
                                        params
                                );

                                return true;
                        }

                        return false;
                    }
                }
        );
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            "cricket_timing",
                            "Cricket Timing",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Keeps the Cricket Timing floating overlay running"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }

    @Override
    public void onDestroy() {

        running = false;

        handler.removeCallbacks(
                timerRunnable
        );

        if (overlayView != null &&
                windowManager != null) {

            try {
                windowManager.removeView(
                        overlayView
                );
            } catch (Exception ignored) {
            }
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
