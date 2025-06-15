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
import android.widget.Toast;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ExoPlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private ImageButton btnClose, btnRotate, btnRatio;
    private ImageButton btnPrev, btnNext, btnSpeed;
    private boolean isZoomed = false; // track chế độ fit/zoom
    private GestureDetector gestureDetector;
    private List<String> videoList = new ArrayList<>();
    private int currentIndex = 0;
    private final float[] playbackSpeeds = new float[]{0.5f, 1f, 1.5f, 2f};
    private int speedIndex = 1;

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
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnSpeed = findViewById(R.id.btn_speed);

        // ✅ Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra("videoUrl");
        String videoListJson = intent.getStringExtra("videoListJson");

        if (videoListJson != null && !videoListJson.isEmpty()) {
            Type type = new TypeToken<List<String>>(){}.getType();
            videoList = new Gson().fromJson(videoListJson, type);
            currentIndex = videoList.indexOf(videoUrl);
            if (currentIndex < 0) {
                videoList.add(videoUrl);
                currentIndex = videoList.size() - 1;
            }
        } else {
            videoList.add(videoUrl);
            currentIndex = 0;
        }

        // ❌ Nếu không có URL thì thoát
        if (videoUrl == null || videoUrl.isEmpty()) {
            finish();
            return;
        }

        // ✅ Khởi tạo ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playCurrent();

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

            @Override
            public boolean onDown(MotionEvent e) {
                currentSeekPosition = player.getCurrentPosition();
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
                }
                return false;
            }
        });

        playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        player.addListener(new com.google.android.exoplayer2.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == com.google.android.exoplayer2.Player.STATE_ENDED) {
                    if (currentIndex < videoList.size() - 1) {
                        currentIndex++;
                        playCurrent();
                    }
                }
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                playCurrent();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < videoList.size() - 1) {
                currentIndex++;
                playCurrent();
            }
        });

        btnSpeed.setOnClickListener(v -> {
            speedIndex = (speedIndex + 1) % playbackSpeeds.length;
            player.setPlaybackSpeed(playbackSpeeds[speedIndex]);
            Toast.makeText(this, playbackSpeeds[speedIndex] + "x", Toast.LENGTH_SHORT).show();
        });

        if (videoList.size() <= 1) {
            btnPrev.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
        }
    }

    private void playCurrent() {
        String url = videoList.get(currentIndex);
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.setPlayWhenReady(true);
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
