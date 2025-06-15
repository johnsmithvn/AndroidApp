package com.mylocalmanga.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

public class ExoPlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private ImageButton btnClose, btnRotate, btnRatio;
    private boolean isZoomed = false; // track chế độ fit/zoom
    private GestureDetector gestureDetector;

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer); // Layout này cần có nền đen

        // ✅ Fullscreen toàn diện: ẩn thanh trạng thái + thanh điều hướng
        hideSystemUI();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // ✅ Gắn layout components
        playerView = findViewById(R.id.player_view);
        btnClose = findViewById(R.id.btn_close);
        btnRotate = findViewById(R.id.btn_rotate);
        btnRatio = findViewById(R.id.btn_ratio);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    long offsetMs = Math.max(10000, Math.min(60000, (long) (Math.abs(diffX) / 50) * 1000));
                    if (diffX > 0) {
                        seekBy(offsetMs);
                    } else {
                        seekBy(-offsetMs);
                    }
                    return true;
                }
                return false;
            }
        });
        gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                seekBy(10000);
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
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
        });

        playerView.setControllerHideOnTouch(false);
        playerView.setControllerAutoShow(false);
        playerView.showController();
        playerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

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
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void seekBy(long offsetMs) {
        if (player == null) return;
        long newPosition = player.getCurrentPosition() + offsetMs;
        if (newPosition > player.getDuration()) newPosition = player.getDuration();
        if (newPosition < 0) newPosition = 0;
        player.seekTo(newPosition);
    }
}
