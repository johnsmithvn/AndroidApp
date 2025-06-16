package com.mylocalmanga.app.offline;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mylocalmanga.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OfflineListActivity extends AppCompatActivity {
    private final List<File> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_list);

        RecyclerView rv = findViewById(R.id.recycler_offline);
        rv.setLayoutManager(new LinearLayoutManager(this));

        File base = new File(getExternalFilesDir(null), "offline_manga");
        if (base.exists()) {
            File[] dirs = base.listFiles(File::isDirectory);
            if (dirs != null) {
                items.addAll(Arrays.asList(dirs));
            }
        }

        OfflineAdapter adapter = new OfflineAdapter(items, dir -> {
            Intent i = new Intent(this, OfflineReaderActivity.class);
            i.putExtra("dir", dir.getAbsolutePath());
            startActivity(i);
        });
        rv.setAdapter(adapter);
    }
}
