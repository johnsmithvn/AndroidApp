package com.mylocalmanga.app;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OfflineActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageButton switchBtn;
    private OfflineAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        recyclerView = findViewById(R.id.recyclerView);
        switchBtn = findViewById(R.id.btn_switch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OfflineAdapter(loadImages());
        recyclerView.setAdapter(adapter);

        switchBtn.setOnClickListener(v -> {
            // Close offline and return to main
            finish();
        });
    }

    private List<File> loadImages() {
        File dir = new File(getExternalFilesDir(null), "offline");
        List<File> list = new ArrayList<>();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) list.add(f);
                }
            }
        }
        return list;
    }

    private static class OfflineAdapter extends RecyclerView.Adapter<OfflineViewHolder> {
        private final List<File> files;
        OfflineAdapter(List<File> f) { files = f; }
        @Override public OfflineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = View.inflate(parent.getContext(), R.layout.list_item_offline_image, null);
            return new OfflineViewHolder(v);
        }
        @Override public void onBindViewHolder(OfflineViewHolder holder, int position) {
            File f = files.get(position);
            Glide.with(holder.img.getContext()).load(f).into(holder.img);
            holder.itemView.setOnClickListener(v -> {
                // open MainActivity to view at original page
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("offlineFile", f.getAbsolutePath());
                v.getContext().startActivity(intent);
            });
        }
        @Override public int getItemCount() { return files.size(); }
    }

    private static class OfflineViewHolder extends RecyclerView.ViewHolder {
        android.widget.ImageView img;
        OfflineViewHolder(View item) {
            super(item);
            img = item.findViewById(R.id.imageView);
        }
    }
}
