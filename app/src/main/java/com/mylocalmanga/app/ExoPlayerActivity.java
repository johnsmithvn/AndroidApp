package com.mylocalmanga.app;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

public class ExoPlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Mở mặc định ở chế độ ngang
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        playerView = findViewById(R.id.player_view);
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // ✅ Nhận URL video
        String videoUrl = getIntent().getStringExtra("videoUrl");
        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "❌ Thiếu video URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MediaItem item = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(item);
        player.prepare();
        player.play();

        // ✅ Double tap để tua
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float x = e.getX();
                float width = playerView.getWidth();
                if (x < width / 2) {
                    player.seekTo(Math.max(0, player.getCurrentPosition() - 10000));
                    Toast.makeText(ExoPlayerActivity.this, "⏪ Lùi 10s", Toast.LENGTH_SHORT).show();
                } else {
                    player.seekTo(Math.min(player.getDuration(), player.getCurrentPosition() + 10000));
                    Toast.makeText(ExoPlayerActivity.this, "⏩ Tua 10s", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        playerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // ❌ Nút đóng
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // 📐 Nút đổi tỷ lệ video
        ImageButton ratioBtn = findViewById(R.id.btn_ratio);
        final int[] modes = {
                AspectRatioFrameLayout.RESIZE_MODE_FIT,
                AspectRatioFrameLayout.RESIZE_MODE_FILL,
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        };
        final String[] labels = {"📺 Fit", "🖼 Fill", "🔍 Zoom"};
        final int[] index = {0};

        ratioBtn.setOnClickListener(v -> {
            index[0] = (index[0] + 1) % modes.length;
            playerView.setResizeMode(modes[index[0]]);
            Toast.makeText(this, "Tỷ lệ: " + labels[index[0]], Toast.LENGTH_SHORT).show();
        });

        // 🔁 Nút xoay màn hình
        ImageButton rotateBtn = findViewById(R.id.btn_rotate);
        rotateBtn.setOnClickListener(v -> {
            int current = getRequestedOrientation();
            if (current == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Toast.makeText(this, "↕️ Đã chuyển dọc", Toast.LENGTH_SHORT).show();
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Toast.makeText(this, "↔️ Đã chuyển ngang", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) player.release();
    }
}
