package com.crickettiming.app;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
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
    private TimingBar timingBar;

    private WindowManager.LayoutParams windowParams;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private long startTime = 0;
    private boolean running = false;

    private double currentTime = 0.0;
    private double targetTime = 1.650;

    private final double perfectTolerance = 0.050;

    private final ArrayList<Double> shots =
            new ArrayList<>();

    private final Runnable timerRunnable =
            new Runnable() {

        @Override
        public void run() {

            if (running &&
                    timerText != null &&
                    timingBar != null) {

                currentTime =
                        (SystemClock.elapsedRealtime()
                                - startTime) / 1000.0;

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

                handler.postDelayed(
                        this,
                        20
                );
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // ========================================================
        // NOTIFICATION
        // ========================================================

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

            manager.createNotificationChannel(
                    channel
            );
        }

        Notification notification =
                new Notification.Builder(
                        this,
                        "cricket_timing"
                )
                        .setContentTitle(
                                "Cricket Timing"
                        )
                        .setContentText(
                                "Timing overlay is running"
                        )
                        .setSmallIcon(
                                android.R.drawable
                                        .ic_menu_info_details
                        )
                        .build();

        startForeground(
                1,
                notification
        );

        // ========================================================
        // OVERLAY PERMISSION
        // ========================================================

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        windowManager =
                (WindowManager)
                        getSystemService(
                                WINDOW_SERVICE
                        );

        createOverlay();
    }

    private void createOverlay() {

        // ========================================================
        // MAIN OVERLAY
        // ========================================================

        overlay =
                new LinearLayout(this);

        overlay.setOrientation(
                LinearLayout.VERTICAL
        );

        overlay.setGravity(
                Gravity.CENTER
        );

        overlay.setPadding(
                10,
                8,
                10,
                8
        );

        overlay.setBackgroundColor(
                Color.rgb(
                        17,
                        24,
                        39
                )
        );

        // ========================================================
        // TITLE / DRAG HANDLE
        // ========================================================

        TextView title =
                new TextView(this);

        title.setText(
                "🏏 Timing"
        );

        title.setTextColor(
                Color.WHITE
        );

        title.setTextSize(
                18
        );

        title.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        42
                );

        overlay.addView(
                title,
                titleParams
        );

        // ========================================================
        // TIMER
        // ========================================================

        timerText =
                new TextView(this);

        timerText.setText(
                "0.000 s"
        );

        timerText.setTextColor(
                Color.WHITE
        );

        timerText.setTextSize(
                24
        );

        timerText.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        45
                );

        overlay.addView(
                timerText,
                timerParams
        );

        // ========================================================
        // TIMING BAR
        // ========================================================

        timingBar =
                new TimingBar();

        LinearLayout.LayoutParams barParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        38
                );

        barParams.setMargins(
                0,
                2,
                0,
                4
        );

        overlay.addView(
                timingBar,
                barParams
        );

        // ========================================================
        // STATUS
        // ========================================================

        statusText =
                new TextView(this);

        statusText.setText(
                "Ready"
        );

        statusText.setTextColor(
                Color.LTGRAY
        );

        statusText.setTextSize(
                13
        );

        statusText.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        32
                );

        overlay.addView(
                statusText,
                statusParams
        );

        // ========================================================
        // TARGET LABEL
        // ========================================================

        TextView targetLabel =
                new TextView(this);

        targetLabel.setText(
                "Target (seconds)"
        );

        targetLabel.setTextColor(
                Color.WHITE
        );

        targetLabel.setTextSize(
                12
        );

        targetLabel.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        28
                );

        overlay.addView(
                targetLabel,
                labelParams
        );

        // ========================================================
        // TARGET INPUT
        // ========================================================

        targetInput =
                new EditText(this);

        targetInput.setText(
                "1.650"
        );

        targetInput.setTextColor(
                Color.WHITE
        );

        targetInput.setHintTextColor(
                Color.GRAY
        );

        targetInput.setTextSize(
                16
        );

        targetInput.setGravity(
                Gravity.CENTER
        );

        targetInput.setSingleLine(
                true
        );

        targetInput.setSelectAllOnFocus(
                true
        );

        targetInput.setFocusable(
                true
        );

        targetInput.setFocusableInTouchMode(
                true
        );

        targetInput.setClickable(
                true
        );

        targetInput.setLongClickable(
                true
        );

        targetInput.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        42
                );

        inputParams.setMargins(
                10,
                0,
                10,
                3
        );

        overlay.addView(
                targetInput,
                inputParams
        );

        // ========================================================
        // APPLY TARGET
        // ========================================================

        Button applyButton =
                new Button(this);

        applyButton.setText(
                "APPLY TARGET"
        );

        setSmallButtonHeight(
                applyButton
        );

        overlay.addView(
                applyButton
        );

        // ========================================================
        // START
        // ========================================================

        Button startButton =
                new Button(this);

        startButton.setText(
                "START"
        );

        setSmallButtonHeight(
                startButton
        );

        overlay.addView(
                startButton
        );

        // ========================================================
        // HIT
        // ========================================================

        Button hitButton =
                new Button(this);

        hitButton.setText(
                "HIT"
        );

        hitButton.setEnabled(
                false
        );

        setSmallButtonHeight(
                hitButton
        );

        overlay.addView(
                hitButton
        );

        // ========================================================
        // NEW SESSION
        // ========================================================

        Button newSessionButton =
                new Button(this);

        newSessionButton.setText(
                "NEW SESSION"
        );

        setSmallButtonHeight(
                newSessionButton
        );

        overlay.addView(
                newSessionButton
        );

        // ========================================================
        // CLOSE
        // ========================================================

        Button closeButton =
                new Button(this);

        closeButton.setText(
                "CLOSE"
        );

        setSmallButtonHeight(
                closeButton
        );

        overlay.addView(
                closeButton
        );

        // ========================================================
        // MINIMIZE
        // ========================================================

        Button minimizeButton =
                new Button(this);

        minimizeButton.setText(
                "MINIMIZE"
        );

        setSmallButtonHeight(
                minimizeButton
        );

        overlay.addView(
                minimizeButton
        );

        // ========================================================
        // STATS
        // ========================================================

        statsText =
                new TextView(this);

        statsText.setText(
                "Shots: 0\nBest: --\nAverage: --"
        );

        statsText.setTextColor(
                Color.WHITE
        );

        statsText.setTextSize(
                13
        );

        statsText.setGravity(
                Gravity.CENTER
        );

        statsText.setPadding(
                0,
                5,
                0,
                0
        );

        LinearLayout.LayoutParams statsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        55
                );

        overlay.addView(
                statsText,
                statsParams
        );

        // ========================================================
        // WINDOW
        // ========================================================

        windowParams =
                new WindowManager.LayoutParams(
                        200,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

                        /*
                         * NOT_FOCUSABLE means:
                         *
                         * - Other apps can receive keyboard/touch
                         * - The overlay does not steal normal focus
                         * - We temporarily remove this flag when
                         *   the Target input needs the keyboard
                         */
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,

                        PixelFormat.TRANSLUCENT
                );

        windowParams.gravity =
                Gravity.TOP | Gravity.RIGHT;

        windowParams.x = 15;
        windowParams.y = 70;

        // IMPORTANT:
        // Do NOT use setScaleX or setScaleY.

        windowManager.addView(
                overlay,
                windowParams
        );

        // ========================================================
        // DRAG
        // ========================================================

        makeDraggable(
                title,
                overlay,
                windowParams
        );

        // ========================================================
        // TARGET INPUT
        // ========================================================

        targetInput.setOnClickListener(
                v -> {

                    /*
                     * Temporarily allow this overlay window
                     * to receive focus so the keyboard can appear.
                     */

                    windowParams.flags &=
                            ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

                    try {

                        windowManager.updateViewLayout(
                                overlay,
                                windowParams
                        );

                    } catch (Exception ignored) {
                    }

                    targetInput.requestFocus();

                    targetInput.selectAll();

                    InputMethodManager imm =
                            (InputMethodManager)
                                    getSystemService(
                                            Context.INPUT_METHOD_SERVICE
                                    );

                    if (imm != null) {

                        imm.showSoftInput(
                                targetInput,
                                InputMethodManager.SHOW_IMPLICIT
                        );
                    }
                }
        );

        // ========================================================
        // APPLY TARGET
        // ========================================================

        applyButton.setOnClickListener(
                v -> {

                    readTarget();

                    targetInput.clearFocus();

                    InputMethodManager imm =
                            (InputMethodManager)
                                    getSystemService(
                                            Context.INPUT_METHOD_SERVICE
                                    );

                    if (imm != null) {

                        imm.hideSoftInputFromWindow(
                                targetInput.getWindowToken(),
                                0
                        );
                    }

                    /*
                     * Give focus back to other apps.
                     */

                    windowParams.flags |=
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

                    try {

                        windowManager.updateViewLayout(
                                overlay,
                                windowParams
                        );

                    } catch (Exception ignored) {
                    }
                }
        );

        // ========================================================
        // START
        // ========================================================

        startButton.setOnClickListener(
                v -> {

                    readTarget();

                    startTime =
                            SystemClock.elapsedRealtime();

                    currentTime = 0.0;

                    running = true;

                    timerText.setText(
                            "0.000 s"
                    );

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

                    startButton.setEnabled(
                            false
                    );

                    hitButton.setEnabled(
                            true
                    );

                    targetInput.setEnabled(
                            false
                    );

                    applyButton.setEnabled(
                            false
                    );

                    handler.removeCallbacks(
                            timerRunnable
                    );

                    handler.post(
                            timerRunnable
     );
                }
        );

        // ========================================================
        // HIT
        // ========================================================

        hitButton.setOnClickListener(
                v -> {

                    if (!running) {
                        return;
                    }

                    currentTime =
                            (SystemClock.elapsedRealtime()
                                    - startTime) / 1000.0;

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

                    shots.add(
                            currentTime
                    );

                    showResult();

                    updateStats();

                    startButton.setEnabled(
                            true
                    );

                    hitButton.setEnabled(
                            false
                    );

                    targetInput.setEnabled(
                            true
                    );

                    applyButton.setEnabled(
                            true
                    );
                }
        );

        // ========================================================
        // NEW SESSION
        // ========================================================

        newSessionButton.setOnClickListener(
                v -> {

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

                    startButton.setEnabled(
                            true
                    );

                    hitButton.setEnabled(
                            false
                    );

                    targetInput.setEnabled(
                            true
                    );

                    applyButton.setEnabled(
                            true
                    );

                    updateStats();
                }
  );

        // ========================================================
        // CLOSE
        // ========================================================

        closeButton.setOnClickListener(
                v -> {

                    running = false;

                    handler.removeCallbacks(
                            timerRunnable
                    );

                    stopSelf();
                }
        );

        // ========================================================
        // MINIMIZE
        // ========================================================

        minimizeButton.setOnClickListener(
                v -> {

                    for (
                            int i = 0;
                            i < overlay.getChildCount();
                            i++
                    ) {

                        View child =
                                overlay.getChildAt(i);

                        if (child != minimizeButton) {

                            child.setVisibility(
                                    View.GONE
                            );
                        }
                    }

                    minimizeButton.setText(
                            "🏏"
                    );
                }
        );
    }

    // ============================================================
    // SMALL BUTTON SIZE
    // ============================================================

    private void setSmallButtonHeight(
            Button button
    ) {

        button.setMinHeight(0);
        button.setMinimumHeight(0);

        button.setPadding(
                5,
                0,
                5,
                0
        );

        button.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        42
                )
        );
    }

    // ============================================================
    // DRAG OVERLAY
    // ============================================================

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

                                } catch (
                                        Exception ignored
                                ) {
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
    // READ TARGET
    // ============================================================

    private void readTarget() {

        if (targetInput == null) {
            return;
        }

        String value =
                targetInput
                        .getText()
                        .toString()
                        .trim();

        if (value.isEmpty()) {
            return;
        }

        try {

            double parsed =
                    Double.parseDouble(
                            value
                    );

            if (parsed > 0.0 &&
                    parsed < 20.0) {

                targetTime =
                        parsed;

                if (timingBar != null) {

                    timingBar.setValues(
                            currentTime,
                            targetTime
                    );
                }

                statusText.setText(
                        String.format(
                                Locale.US,
                                "Target: %.3f s",
                                targetTime
                        )
                );
            }

        } catch (
                NumberFormatException ignored
        ) {
        }
    }

    // ============================================================
    // RESULT
    // ============================================================

    private void showResult() {

        double difference =
                currentTime -
                targetTime;

        double absoluteDifference =
                Math.abs(
                        difference
                );

        if (absoluteDifference <= perfectTolerance) {

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
                            Math.abs(
                                    difference
                            )
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

    // ============================================================
    // STATS
    // ============================================================

    private void updateStats() {

        if (shots.isEmpty()) {

            statsText.setText(
                    "Shots: 0\nBest: --\nAverage: --"
            );

            return;
        }

        double total = 0.0;

        double best =
                shots.get(0);

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

    // ============================================================
    // TIMING BAR
    // ============================================================

    private class TimingBar
            extends View {

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

            barTime =
                    current;

            barTarget =
                    target;

            invalidate();
        }

        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(
                    canvas
            );

            float width =
                    getWidth();

            float height =
                    getHeight();

            float centerY =
                    height / 2f;

            double maximumTime =
                    barTarget *
                    1.30;

            if (maximumTime <= 0) {
                maximumTime = 2.0;
            }

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

            float targetX =
                    10 +
                    (float) (
                            (barTarget /
                                    maximumTime)
                                    *
                                    (width - 20)
                    );

            double perfectStart =
                    Math.max(
                            0,
                            barTarget -
                                    perfectTolerance
                    );

            double perfectEnd =
                    barTarget +
                    perfectTolerance;

            float perfectStartX =
                    10 +
                    (float) (
                            (perfectStart /
                                    maximumTime)
                                    *
                                    (width - 20)
                    );

            float perfectEndX =
                    10 +
                    (float) (
                            (perfectEnd /
                                    maximumTime)
                                    *
                                    (width - 20)
                    );

            paint.setColor(
                    Color.rgb(
                            50,
                            220,
                            100
                    )
            );

            paint.setStrokeWidth(
                    14f
            );

            canvas.drawLine(
                    perfectStartX,
                    centerY,
                    perfectEndX,
                    centerY,
                    paint
            );

            paint.setColor(
                    Color.WHITE
            );

            paint.setStrokeWidth(
                    4f
            );

            canvas.drawLine(
                    targetX,
                    5,
                    targetX,
                    height - 5,
                    paint
            );

            double limitedTime =
                    Math.min(
                            Math.max(
                                    barTime,
                                    0
                            ),
                            maximumTime
                    );

            float currentX =
                    10 +
                    (float) (
                            (limitedTime /
                                    maximumTime)
                                    *
                                    (width - 20)
                    );

            paint.setColor(
                    Color.YELLOW
            );

            paint.setStrokeWidth(
                    7f
            );

            canvas.drawLine(
                    currentX,
                    2,
                    currentX,
                    height - 2,
                    paint
            );
        }
    }

    // ============================================================
    // DESTROY
    // ============================================================

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

            } catch (
                    Exception ignored
            ) {
            }

            overlay = null;
        }

        super.onDestroy();
    }

    // ============================================================
    // BINDER
    // ============================================================

    @Override
    public IBinder onBind(
            Intent intent
    ) {

        return null;
    }
}
