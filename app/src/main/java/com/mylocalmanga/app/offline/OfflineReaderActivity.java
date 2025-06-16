package com.mylocalmanga.app.offline;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mylocalmanga.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class OfflineReaderActivity extends AppCompatActivity {
    private final List<File> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_reader);

        RecyclerView rv = findViewById(R.id.recycler_reader);
        rv.setLayoutManager(new LinearLayoutManager(this));

        String path = getIntent().getStringExtra("dir");
        if (path != null) {
            File dir = new File(path);
            File[] files = dir.listFiles((f, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName));
                images.addAll(Arrays.asList(files));
            }
        }

        rv.setAdapter(new ImageAdapter(images));
    }
}
