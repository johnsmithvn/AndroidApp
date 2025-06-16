package com.mylocalmanga.app.offline;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mylocalmanga.app.R;

import java.io.File;
import java.util.List;

public class OfflineAdapter extends RecyclerView.Adapter<OfflineAdapter.Holder> {
    public interface OnClick {
        void open(File dir);
    }

    private final List<File> data;
    private final OnClick listener;

    public OfflineAdapter(List<File> data, OnClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offline, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        File dir = data.get(position);
        holder.title.setText(dir.getName());
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            Glide.with(holder.thumb).load(files[0]).into(holder.thumb);
        }
        holder.itemView.setOnClickListener(v -> listener.open(dir));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView thumb;
        TextView title;
        Holder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.img_thumb);
            title = itemView.findViewById(R.id.txt_title);
        }
    }
}
