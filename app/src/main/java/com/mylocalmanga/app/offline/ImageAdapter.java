package com.mylocalmanga.app.offline;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mylocalmanga.app.R;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> {
    private final List<File> files;

    public ImageAdapter(List<File> files) {
        this.files = files;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Glide.with(holder.img).load(files.get(position)).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView img;
        Holder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_page);
        }
    }
}
