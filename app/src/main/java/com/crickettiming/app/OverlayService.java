package com.crickettiming.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Main floating box
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(20, 15, 20, 15);
        box.setGravity(Gravity.CENTER);
        box.setBackgroundColor(Color.rgb(25, 35, 50));

        // Title
        TextView title = new TextView(this);
        title.setText("🏏 Timing");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);

        // Timer display
        TextView timer = new TextView(this);
        timer.setText("0.000 s");
        timer.setTextColor(Color.WHITE);
        timer.setTextSize(20);
        timer.setGravity(Gravity.CENTER);

        // START button
        Button startButton = new Button(this);
        startButton.setText("START");

        // HIT button
        Button hitButton = new Button(this);
        hitButton.setText("HIT");

        // Add views
        box.addView(title);
        box.addView(timer);
        box.addView(startButton);
        box.addView(hitButton);

        overlayView = box;

        // Window settings
        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 30;
        params.y = 150;

        // Make the overlay appear
        windowManager.addView(overlayView, params);

        // Timer variables
        final long[] startTime = {0};
        final boolean[] running = {false};

        // Start button
        startButton.setOnClickListener(v -> {
            startTime[0] = System.nanoTime();
            running[0] = true;
            timer.setText("0.000 s");
        });

        // Hit button
        hitButton.setOnClickListener(v -> {
            if (running[0]) {
                long elapsed = System.nanoTime() - startTime[0];

                double seconds = elapsed / 1_000_000_000.0;

                timer.setText(String.format("%.3f s", seconds));

                running[0] = false;
            }
        });

        // Allow the overlay to be dragged
        box.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        return true;

                    case MotionEvent.ACTION_MOVE:

                        params.x = initialX +
                                (int) (event.getRawX() - initialTouchX);

                        params.y = initialY +
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

    @Override
    public void onDestroy() {

        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
