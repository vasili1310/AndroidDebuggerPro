package com.example.androiddebuggerpro;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ScreenTestActivity extends AppCompatActivity {
    private FrameLayout screenView;
    private int currentColorIndex = 0;
    private final int[] colors = {
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.WHITE,
            Color.BLACK
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screenView = new FrameLayout(this);
        setContentView(screenView);

        enterFullScreen();
        showNextColor();

        screenView.setOnClickListener(v -> showNextColor());
    }

    private void showNextColor() {
        screenView.setBackgroundColor(colors[currentColorIndex]);
        currentColorIndex = (currentColorIndex + 1) % colors.length;
    }

    private void enterFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());

                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }
}
