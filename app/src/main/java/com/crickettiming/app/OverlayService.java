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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

    private long startTime = 0;
    private boolean running = false;
    private boolean viewAdded = false;
    private boolean destroyed = false;

    private double currentTime = 0.0;
    private double targetTime = 1.650;

    private static final double PERFECT_TOLERANCE = 0.050;

    private final ArrayList<Double> shots =
            new ArrayList<>();

    private final Runnable timerRunnable =
            new Runnable() {

                @Override
                public void run() {

                    if (destroyed ||
                            !running ||
                            timerText == null ||
                            timingBar == null) {
                        return;
                    }

                    currentTime =
                            (SystemClock.elapsedRealtime()
                                    - startTime)
                                    / 1000.0;

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

                    if (running && !destroyed) {

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

        destroyed = false;

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

        if (overlay != null ||
                destroyed) {
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

        // EDITABLE TARGET INPUT

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

        targetInput.setFocusable(
                true
        );

        targetInput.setFocusableInTouchMode(
                true
        );

        targetInput.setClickable(
                true
        );

        targetInput.setCursorVisible(
                true
        );

        targetInput.setSelectAllOnFocus(
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

        // NEW SESSION BUTTON

        Button newSessionButton =
                new Button(this);

        newSessionButton.setText(
                "NEW SESSION"
        );

        overlay.addView(
                newSessionButton
        );

        // CLOSE BUTTON

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
        // Do NOT use FLAG_NOT_FOCUSABLE here.
        // The target EditText needs focus.

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

                    if (!hasFocus ||
                            destroyed ||
                            targetInput == null) {
                        return;
                    }

                    targetInput.postDelayed(
                            () -> {

                                if (destroyed ||
                                        targetInput == null) {
                                    return;
                                }

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

                            },
                            150
                    );
                }
        );

        // TARGET TOUCH
        //
        // Return false so EditText keeps normal cursor/
        // selection/touch behavior.

        targetInput.setOnTouchListener(
                (v, event) -> {

                    if (destroyed ||
                            targetInput == null) {

                        return false;
                    }

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        targetInput.setEnabled(
                                true
                        );

                        targetInput.setFocusable(
                                true
                        );

                        targetInput.setFocusableInTouchMode(
                                true
                        );

                        targetInput.requestFocus();

                        targetInput.postDelayed(
                                () -> {

                                    if (destroyed ||
                                            targetInput == null) {
                                        return;
                                    }

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

                                },
                                100
                        );
                    }

                    return false;
                }
        );

        // TARGET VALUE CHANGES LIVE

        targetInput.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        if (destroyed ||
                                targetInput == null) {
                            return;
                        }

                        try {

                            String value =
                                    s.toString()
                                            .trim();

                            if (!value.isEmpty()) {

                                double parsed =
                                        Double.parseDouble(
                                                value
                                        );

                                if (Double.isFinite(
                                        parsed
                                ) &&
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
                            }

                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        // START

        startButton.setOnClickListener(
                v -> {

                    if (destroyed ||
                            running) {
                        return;
                    }

                    // IMPORTANT:
                    // Target is NOT disabled.
                    targetInput.setEnabled(
                            true
                    );

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

                    if (destroyed ||
                            !running) {
                        return;
                    }

                    currentTime =
                            (SystemClock
                                    .elapsedRealtime()
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

                    // Keep editable.
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
                });
                // CLOSE
        closeButton.setOnClickListener(
                v -> {

                    running = false;

                    handler.removeCallbacks(
                            timerRunnable
                    );

                    stopSelf();
                }
        );

        // DRAG
        makeDraggable(
                title,
                overlay,
                params
        );
    }

    // =========================
    // READ EDITED TARGET
    // =========================

    private void readTarget() {

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
                    Double.parseDouble(value);

            if (parsed > 0.0 &&
                    parsed < 20.0) {

                targetTime = parsed;

                if (timingBar != null) {
                    timingBar.setValues(
                            currentTime,
                            targetTime
                    );
                }
            }

        } catch (NumberFormatException ignored) {
            // Keep the previous target if input is invalid.
        }
    }

    // =========================
    // RESULT
    // =========================

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

    // =========================
    // STATS
    // =========================

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
        }

        super.onDestroy();
    }
}
