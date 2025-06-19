package com.mylocalmanga.app.offline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mylocalmanga.app.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OfflineListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_list);
        ListView listView = findViewById(R.id.list);

        File dir = new File(getExternalFilesDir("manga"), "");
        File[] folders = dir.exists() ? dir.listFiles(File::isDirectory) : new File[0];
        List<String> names = new ArrayList<>();
        if (folders != null) {
            for (File f : folders) {
                names.add(f.getName());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String folderName = names.get(position);
                Intent intent = new Intent(OfflineListActivity.this, OfflineReaderActivity.class);
                intent.putExtra("folder", folderName);
                startActivity(intent);
            }
        });
    }
}
