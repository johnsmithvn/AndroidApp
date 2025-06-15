package com.mylocalmanga.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class ExoPlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private StyledPlayerView playerView;
    private ImageButton btnClose;

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

        // ⚙️ Giao diện YouTube style

        // ⛔ Nút đóng
        btnClose.setOnClickListener(v -> finish());

        // ✨ StyledPlayerView đã hỗ trợ tự điều chỉnh orientation và zoom giống YouTube
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
