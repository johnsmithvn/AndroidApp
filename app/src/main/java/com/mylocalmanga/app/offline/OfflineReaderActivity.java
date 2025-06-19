package com.mylocalmanga.app.offline;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mylocalmanga.app.R;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class OfflineReaderActivity extends AppCompatActivity {

    private static final int PAGE_SIZE = 200;
    private File[] images;
    private int page = 0;
    private LinearLayout container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_reader);

        String folderName = getIntent().getStringExtra("folder");
        File dir = new File(getExternalFilesDir("manga"), folderName);

        container = findViewById(R.id.container);
        images = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if (images != null) {
            Arrays.sort(images, Comparator.comparing(File::getName));
        } else {
            images = new File[0];
        }

        Button next = findViewById(R.id.next);
        Button prev = findViewById(R.id.prev);

        next.setOnClickListener(v -> {
            if ((page + 1) * PAGE_SIZE < images.length) {
                page++;
                loadPage();
            }
        });

        prev.setOnClickListener(v -> {
            if (page > 0) {
                page--;
                loadPage();
            }
        });

        loadPage();
    }

    private void loadPage() {
        container.removeAllViews();
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, images.length);
        for (int i = start; i < end; i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            iv.setAdjustViewBounds(true);
            Glide.with(this).load(images[i]).into(iv);
            container.addView(iv);
        }
    }
}
