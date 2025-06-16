package com.mylocalmanga.app.offline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OfflineListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        ImageButton btnOnline = new ImageButton(this);
        btnOnline.setImageResource(android.R.drawable.ic_menu_rotate);
        btnOnline.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        listView.addHeaderView(btnOnline);

        setContentView(listView);

        btnOnline.setOnClickListener(v -> finish());

        File root = OfflineUtils.getOfflineRoot(this);
        File[] dirs = root.listFiles(File::isDirectory);
        List<String> items = new ArrayList<>();
        if (dirs != null) {
            for (File d : dirs) {
                items.add(d.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String folder = items.get(position);
            Intent intent = new Intent(this, OfflineReaderActivity.class);
            intent.putExtra("folder", folder);
            startActivity(intent);
        });
    }
}
