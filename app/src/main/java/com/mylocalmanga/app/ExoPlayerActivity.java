package com.mylocalmanga.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.DefaultTimeBar;

public class ExoPlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private ImageButton btnClose, btnRotate, btnRatio;
    private boolean isZoomed = false; // track chế độ fit/zoom
    private GestureDetector gestureDetector;
    private float currentBrightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer); // Layout này cần có nền đen

        // ✅ Fullscreen toàn diện: ẩn thanh trạng thái + thanh điều hướng
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // ✅ Gắn layout components
        playerView = findViewById(R.id.player_view);
        btnClose = findViewById(R.id.btn_close);
        btnRotate = findViewById(R.id.btn_rotate);
        btnRatio = findViewById(R.id.btn_ratio);

        // ✅ Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra("videoUrl");
        String videoListJson = intent.getStringExtra("videoListJson"); // (sẽ xử lý sau)

        // ❌ Nếu không có URL thì thoát
        if (videoUrl == null || videoUrl.isEmpty()) {
            finish();
            return;
        }

        // ✅ Khởi tạo ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setMediaItem(MediaItem.fromUri(videoUrl));
        player.prepare();
        player.setPlayWhenReady(true);

        // enlarge progress bar
        DefaultTimeBar timeBar = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_progress);
        if (timeBar != null) {
            int barHeight = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8,
                    getResources().getDisplayMetrics());
            timeBar.setBarHeight(barHeight);
            timeBar.setScrubberEnabledSize(barHeight);
            timeBar.setScrubberDisabledSize(barHeight);
            timeBar.setScrubberDraggedSize(barHeight);
            timeBar.setTouchTargetHeight(barHeight);
            android.view.ViewGroup.LayoutParams lp = timeBar.getLayoutParams();
            if (lp != null) {
                lp.height = barHeight;
                timeBar.setLayoutParams(lp);
            }
        }

        // dim controller background
        View controller = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_controller);
        if (controller != null) {
            controller.setBackgroundColor(Color.parseColor("#80000000"));
        }

        // ⚙️ Mặc định fit video
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        // ⛔ Nút đóng
        btnClose.setOnClickListener(v -> finish());

        // 🔁 Xoay ngang/dọc
        btnRotate.setOnClickListener(v -> {
            int orientation = getResources().getConfiguration().orientation;
            setRequestedOrientation(
                    orientation == Configuration.ORIENTATION_LANDSCAPE
                            ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            );
        });

        // 🔍 Đổi chế độ zoom/fit
        btnRatio.setOnClickListener(v -> {
            isZoomed = !isZoomed;
            playerView.setResizeMode(
                    isZoomed
                            ? AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            : AspectRatioFrameLayout.RESIZE_MODE_FIT
            );
        });

        // Auto hide controller after 3s
        playerView.setControllerShowTimeoutMs(3000);

        // Gesture handling
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private long currentSeekPosition;
            private float startX;
            private float startBrightness;

            @Override
            public boolean onDown(MotionEvent e) {
                currentSeekPosition = player.getCurrentPosition();
                startX = e.getX();
                WindowManager.LayoutParams params = getWindow().getAttributes();
                startBrightness = params.screenBrightness;
                if (startBrightness < 0f) {
                    startBrightness = 1f;
                }
                currentBrightness = startBrightness;
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (playerView.isControllerVisible()) {
                    playerView.hideController();
                } else {
                    playerView.showController();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float x = e.getX();
                int width = playerView.getWidth();
                long pos;
                if (x < width / 2f) {
                    // Rewind 10 seconds
                    pos = player.getCurrentPosition() - 10000;
                    if (pos < 0) {
                        pos = 0;
                    }
                } else {
                    // Fast forward 10 seconds
                    pos = player.getCurrentPosition() + 10000;
                    long dur = player.getDuration();
                    if (dur != C.TIME_UNSET) {
                        pos = Math.min(pos, dur);
                    }
                }
                player.seekTo(pos);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    long offset = (long) (-distanceX * 100); // 100ms per pixel
                    long newPos = currentSeekPosition + offset;
                    long dur = player.getDuration();
                    if (dur != C.TIME_UNSET) {
                        newPos = Math.max(0, Math.min(newPos, dur));
                    } else {
                        newPos = Math.max(0, newPos);
                    }
                    currentSeekPosition = newPos;
                    player.seekTo(newPos);
                    return true;
                } else if (startX > playerView.getWidth() * 0.5f) {
                    float change = -distanceY / playerView.getHeight();
                    float newBrightness = currentBrightness + change;
                    newBrightness = Math.max(0f, Math.min(1f, newBrightness));
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = newBrightness;
                    getWindow().setAttributes(params);
                    currentBrightness = newBrightness;
                    return true;
                }
                return false;
            }
        });

        playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
