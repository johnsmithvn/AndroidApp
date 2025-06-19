package com.mylocalmanga.app.offline;

import android.os.Bundle;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_reader);

        String folderName = getIntent().getStringExtra("folder");
        File dir = new File(getExternalFilesDir("manga"), folderName);

        LinearLayout container = findViewById(R.id.container);
        File[] images = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if (images != null) {
            Arrays.sort(images, Comparator.comparing(File::getName));
            for (File img : images) {
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                iv.setAdjustViewBounds(true);
                Glide.with(this).load(img).into(iv);
                container.addView(iv);
            }
        }
    }
}
