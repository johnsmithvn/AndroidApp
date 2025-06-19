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
    private int currentPage = 0;
    private File[] images;
    private LinearLayout container;
    private ScrollView scrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_reader);

        String folderName = getIntent().getStringExtra("folder");
        File dir = new File(getExternalFilesDir("manga"), folderName);

        container = findViewById(R.id.container);
        scrollView = findViewById(R.id.scroll);

        images = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if (images != null) {
            Arrays.sort(images, Comparator.comparing(File::getName));
        } else {
            images = new File[0];
        }

        Button prev = findViewById(R.id.prev);
        Button next = findViewById(R.id.next);

        prev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadPage();
            }
        });
        next.setOnClickListener(v -> {
            if ((currentPage + 1) * PAGE_SIZE < images.length) {
                currentPage++;
                loadPage();
            }
        });

        loadPage();
    }

    private void loadPage() {
        container.removeAllViews();
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, images.length);
        for (int i = start; i < end; i++) {
            File img = images[i];
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            iv.setAdjustViewBounds(true);
            Glide.with(this).load(img).into(iv);
            container.addView(iv);
        }
        scrollView.scrollTo(0, 0);
    }
}
