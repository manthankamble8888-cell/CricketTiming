package com.crickettiming.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private LinearLayout overlay;
    private WindowManager.LayoutParams overlayParams;

    private TextView timerText;
    private TextView statusText;
    private TextView statsText;
    private EditText targetInput;
    private TimingBar timingBar;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private long startTime;
    private boolean running = false;
    private boolean viewAdded = false;

    private double currentTime = 0.0;
    private double targetTime = 1.650;

    private static final double PERFECT_TOLERANCE = 0.050;

    private final ArrayList<Double> shots =
            new ArrayList<>();

    private final Runnable timerRunnable =
            new Runnable() {

                @Override
                public void run() {

                    if (!running ||
                            timerText == null ||
                            timingBar == null) {
                        return;
                    }

                    currentTime =
                            (SystemClock.elapsedRealtime()
                                    - startTime) / 1000.0;

                    try {

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

                    } catch (Exception ignored) {
                    }

                    if (running) {
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

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        return START_STICKY;
    }

    private void createOverlay() {

        if (overlay != null) {
            return;
        }

        overlay =
                new LinearLayout(this);

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
                Color.rgb(
                        17,
                        24,
                        39
                )
        );

        // TITLE

        TextView title =
                new TextView(this);

        title.setText(
                "🏏 Timing"
        );

        title.setTextColor(
                Color.WHITE
        );

        title.setTextSize(
                20
        );

        title.setGravity(
                Gravity.CENTER
        );

        overlay.addView(title);

        // TIMER

        timerText =
                new TextView(this);

        timerText.setText(
                "0.000 s"
        );

        timerText.setTextColor(
                Color.WHITE
        );

        timerText.setTextSize(
                28
        );

        timerText.setGravity(
                Gravity.CENTER
        );

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

        timingBar =
                new TimingBar(this);

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

        statusText =
                new TextView(this);

        statusText.setText(
                "Ready"
        );

        statusText.setTextColor(
                Color.LTGRAY
        );

        statusText.setTextSize(
                15
        );

        statusText.setGravity(
                Gravity.CENTER
        );

        overlay.addView(
                statusText
        );

        // TARGET LABEL

        TextView targetLabel =
                new TextView(this);

        targetLabel.setText(
                "Target (seconds)"
        );

        targetLabel.setTextColor(
                Color.WHITE
        );

        targetLabel.setTextSize(
                13
        );

        targetLabel.setGravity(
                Gravity.CENTER
        );

        overlay.addView(
                targetLabel
        );

        // EDITABLE TARGET

        targetInput =
                new EditText(this);

        targetInput.setText(
                "1.650"
        );

        targetInput.setTextColor(
                Color.WHITE
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

        targetInput.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        overlay.addView(
                targetInput
        );

        // START BUTTON

        Button startButton =
                new Button(this);

        startButton.setText(
                "START"
        );

        overlay.addView(
                startButton
        );

        // HIT BUTTON

        Button hitButton =
                new Button(this);

        hitButton.setText(
                "HIT"
        );

        hitButton.setEnabled(
                false
        );

        overlay.addView(
                hitButton
        );

        // NEW SESSION

        Button newSessionButton =
                new Button(this);

        newSessionButton.setText(
                "NEW SESSION"
        );

        overlay.addView(
                newSessionButton
        );

        // CLOSE

        Button closeButton =
                new Button(this);

        closeButton.setText(
                "CLOSE"
        );

        overlay.addView(
                closeButton
        );

        // STATS

        statsText =
                new TextView(this);

        statsText.setText(
                "Shots: 0\nBest: --\nAverage: --"
        );

        statsText.setTextColor(
                Color.WHITE
        );

        statsText.setTextSize(
                15
        );

        statsText.setGravity(
                Gravity.CENTER
        );

        statsText.setPadding(
                0,
                8,
                0,
                0
        );

        overlay.addView(
                statsText
        );

        // WINDOW
        //
        // IMPORTANT:
        // FLAG_NOT_FOCUSABLE has intentionally been removed.
        // This allows targetInput to receive focus and open
        // the keyboard for editing.

        overlayParams =
                new WindowManager.LayoutParams(
                        300,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                );

        overlayParams.gravity =
                Gravity.TOP |
                Gravity.RIGHT;

        overlayParams.x = 15;
        overlayParams.y = 80;

        overlayParams.softInputMode =
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

        try {

            windowManager.addView(
                    overlay,
                    overlayParams
            );

            viewAdded = true;

        } catch (Exception e) {

            viewAdded = false;
            overlay = null;

            stopSelf();
            return;
        }

        // TARGET FOCUS

        targetInput.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

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
                }
        );

        // START

        startButton.setOnClickListener(
                v -> {

                    if (running) {
                        return;
                    }

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

                    handler.removeCallbacks(
                            timerRunnable
                    );

                    handler.post(
                            timerRunnable
                    );
                }
        );

        // HIT

        hitButton.setOnClickListener(
                v -> {

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
                }
        );

        // NEW SESSION

        newSessionButton.setOnClickListener(
                v -> {

                    stopTiming();

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

                    updateStats();
                }
        );

        // CLOSE

        closeButton.setOnClickListener(
                v -> stopSelf()
        );

        // DRAG

        makeDraggable(
                title,
                overlay,
                overlayParams
        );
    }

    private void stopTiming() {

        running = false;

        handler.removeCallbacks(
                timerRunnable
        );
    }

    // =========================
    // READ EDITED TARGET
    // =========================

    private void readTarget() {

        if (targetInput == null) {
            return;
        }

        try {

            String value =
                    targetInput
                            .getText()
                            .toString()
                            .trim();

            if (value.isEmpty()) {
                return;
            }

            double parsed =
                    Double.parseDouble(
                            value
                    );

            if (Double.isFinite(parsed) &&
                    parsed > 0.0 &&
                    parsed < 20.0) {

                targetTime =
                        parsed;

                if (timingBar != null) {

                    timingBar.setValues(
                            currentTime,
                            targetTime
                    );
                }
            }

        } catch (Exception ignored) {

            // Keep previous valid target.
        }
    }

    // =========================
    // RESULT
    // =========================

    private void showResult() {

        double difference =
                currentTime -
                targetTime;

        double absoluteDifference =
                Math.abs(
                        difference
                );

        if (absoluteDifference <=
                PERFECT_TOLERANCE) {

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

    // =========================
    // STATS
    // =========================

    private void updateStats() {

        if (statsText == null) {
            return;
        }

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
                total /
                shots.size();

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

    // =========================
    // DRAG OVERLAY
    // =========================

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
                                event.getActionMasked()
                        ) {

                            case MotionEvent.ACTION_DOWN:

                                initialX =
                                        params.x;

                                initialY =
                                        params.y;

                                        @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event
                    ) {

                        switch (
                                event.getActionMasked()
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

                                if (viewAdded &&
                                        overlayView != null) {

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
                                }

                                return true;

                            case MotionEvent.ACTION_UP:

                            case MotionEvent.ACTION_CANCEL:

                                return true;

                            default:

                                return false;
                        }
                    }
                }
        );
    }

    // =========================
    // SERVICE BINDER
    // =========================

    @Override
    public IBinder onBind(
            Intent intent
    ) {

        return null;
    }

    @Override
    public void onDestroy() {

        stopTiming();

        handler.removeCallbacksAndMessages(
                null
        );

        if (overlay != null &&
                viewAdded) {

            try {

                windowManager.removeView(
                        overlay
                );

            } catch (
                    Exception ignored
            ) {
            }
        }

        viewAdded = false;

        overlay = null;
        timingBar = null;
        targetInput = null;
        timerText = null;
        statusText = null;
        statsText = null;

        super.onDestroy();
    }

    // ============================================================
    // TIMING BAR
    // ============================================================

    private static class TimingBar
            extends View {

        private final Paint paint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        private double current = 0.0;

        private double target = 1.650;

        TimingBar(
                Context context
        ) {

            super(context);

            paint.setStrokeCap(
                    Paint.Cap.ROUND
            );

            setLayerType(
                    View.LAYER_TYPE_SOFTWARE,
                    null
            );
        }

        void setValues(
                double currentTime,
                double targetTime
        ) {

            current =
                    Math.max(
                            0.0,
                            currentTime
                    );

            target =
                    Math.max(
                            0.001,
                            targetTime
                    );

            postInvalidate();
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

            float centerY =
                    getHeight() /
                    2f;

            if (width <= 20) {
                return;
            }

            float left = 10f;

            float right =
                    width - 10f;

            // Background

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                    10f
            );

            paint.setColor(
                    Color.rgb(
                            70,
                            78,
                            95
                    )
            );

            canvas.drawLine(
                    left,
                    centerY,
                    right,
                    centerY,
                    paint
            );

            // Range

            double maxTime =
                    Math.max(
                            target * 1.8,
                            2.0
                    );

            if (current > maxTime) {

                maxTime =
                        current * 1.05;
            }

            // Target position

            float targetX =
                    left +
                    (float) (
                            (target / maxTime) *
                            (right - left)
                    );

            // Target marker

            paint.setStrokeWidth(
                    6f
            );

            paint.setColor(
                    Color.rgb(
                            255,
                            210,
                            60
                    )
            );

            canvas.drawLine(
                    targetX,
                    centerY - 15f,
                    targetX,
                    centerY + 15f,
                    paint
            );

            // Current position

            float currentX =
                    left +
                    (float) (
                            (current / maxTime) *
                            (right - left)
                    );

            currentX =
                    Math.max(
                            left,
                            Math.min(
                                    right,
                                    currentX
                            )
                    );

            paint.setStyle(
                    Paint.Style.FILL
            );

            if (Math.abs(
                    current - target
            ) <= PERFECT_TOLERANCE) {

                paint.setColor(
                        Color.rgb(
                                50,
                                220,
                                100
                        )
                );

            } else if (current < target) {

                paint.setColor(
                        Color.rgb(
                                70,
                                170,
                                255
                        )
                );

            } else {

                paint.setColor(
                        Color.rgb(
                                255,
                                80,
                                80
                        )
                );
            }

            canvas.drawCircle(
                    currentX,
                    centerY,
                    9f,
                    paint
            );
        }
    }
                
}
