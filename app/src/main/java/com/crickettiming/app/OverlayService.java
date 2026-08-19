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
import android.provider.Settings;
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

    private TextView timerText;
    private TextView statusText;

    private Handler handler = new Handler();

    private long startTime = 0;
    private boolean running = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if (running) {

                long elapsed = System.currentTimeMillis() - startTime;

                double seconds = elapsed / 1000.0;

                timerText.setText(
                        String.format(Locale.US, "%.3f s", seconds)
                );

                handler.postDelayed(this, 30);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        startForeground(1001, createNotification());

        showOverlay();
    }

    private void showOverlay() {

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        /*
         * Main overlay container
         */
        LinearLayout box = new LinearLayout(this);

        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(18, 14, 18, 14);

        box.setBackgroundColor(Color.rgb(20, 28, 45));

        /*
         * Title
         */
        TextView title = new TextView(this);

        title.setText("🏏 Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);

        /*
         * Timer
         */
        timerText = new TextView(this);

        timerText.setText("0.000 s");
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(25);
        timerText.setGravity(Gravity.CENTER);

        /*
         * Status
         */
        statusText = new TextView(this);

        statusText.setText("Ready");
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(15);
        statusText.setGravity(Gravity.CENTER);

        /*
         * START button
         */
        Button startButton = new Button(this);

        startButton.setText("START");

        startButton.setOnClickListener(v -> startTimer());

        /*
         * HIT button
         */
        Button hitButton = new Button(this);

        hitButton.setText("HIT");

        hitButton.setOnClickListener(v -> hitTimer());

        /*
         * CLOSE button
         */
        Button closeButton = new Button(this);

        closeButton.setText("CLOSE");

        closeButton.setOnClickListener(v -> stopSelf());

        /*
         * Add everything
         */
        box.addView(title);
        box.addView(timerText);
        box.addView(statusText);
        box.addView(startButton);
        box.addView(hitButton);
        box.addView(closeButton);

        overlayView = box;

        /*
         * Overlay position
         */
        WindowManager.LayoutParams params;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            params = new WindowManager.LayoutParams(
                    250,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

        } else {

            params = new WindowManager.LayoutParams(
                    250,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
        }

        params.gravity = Gravity.TOP | Gravity.RIGHT;

        params.x = 20;
        params.y = 120;

        windowManager.addView(overlayView, params);

        /*
         * Make the title draggable.
         */
        title.setOnTouchListener(new View.OnTouchListener() {

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

                        return true;

                    case MotionEvent.ACTION_MOVE:

                        params.x =
                                initialX +
                                (int) (initialTouchX - event.getRawX());

                        params.y =
                                initialY +
                                (int) (event.getRawY() - initialTouchY);

                        windowManager.updateViewLayout(
                                overlayView,
                                params
                        );

                        return true;
                }

                return false;
            }
        });
    }

    private void startTimer() {

        if (running) {
            return;
        }

        startTime = System.currentTimeMillis();

        running = true;

        statusText.setText("Timing...");

        handler.post(timerRunnable);
    }

    private void hitTimer() {

        if (!running) {
            return;
        }

        long elapsed =
                System.currentTimeMillis() - startTime;

        double seconds =
                elapsed / 1000.0;

        running = false;

        handler.removeCallbacks(timerRunnable);

        timerText.setText(
                String.format(
                        Locale.US,
                        "%.3f s",
                        seconds
                )
        );

        statusText.setText("HIT!");
    }

    private Notification createNotification() {

        String channelId = "cricket_timing";

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder =
                    new Notification.Builder(
                            this,
                            channelId
                    );

        } else {

            builder =
                    new Notification.Builder(this);
        }

        return builder
                .setContentTitle("Cricket Timing")
                .setContentText("Floating timing overlay is active")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            "cricket_timing",
                            "Cricket Timing",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {

        running = false;

        handler.removeCallbacks(timerRunnable);

        if (overlayView != null && windowManager != null) {

            try {
                windowManager.removeView(overlayView);
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
