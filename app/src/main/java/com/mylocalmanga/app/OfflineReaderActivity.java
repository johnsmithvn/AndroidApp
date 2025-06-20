package com.mylocalmanga.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.view.ViewGroup;
import android.widget.ImageView;

public class OfflineReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setContentView(recyclerView);

        String folderName = getIntent().getStringExtra("folderName");
        File root = new File(getExternalFilesDir(null), DownloadWorker.OFFLINE_ROOT);
        File folder = new File(root, folderName);
        File[] files = folder.listFiles();
        List<File> list = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files);
            list.addAll(Arrays.asList(files));
        }
        recyclerView.setAdapter(new ImageAdapter(list));
    }

    static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {
        private final List<File> files;

        ImageAdapter(List<File> files) {
            this.files = files;
        }

        static class VH extends RecyclerView.ViewHolder {
            ImageView image;
            VH(@NonNull ImageView v) {
                super(v);
                image = v;
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            iv.setAdjustViewBounds(true);
            return new VH(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Glide.with(holder.image.getContext())
                    .load(files.get(position))
                    .into(holder.image);
        }

        @Override
        public int getItemCount() {
            return files.size();
        }
    }
}
