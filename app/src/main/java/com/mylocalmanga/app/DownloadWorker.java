package com.mylocalmanga.app;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadWorker extends Worker {

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String folderName = getInputData().getString("folderName");
        String[] urls = getInputData().getStringArray("imageUrls");
        if (folderName == null || urls == null || urls.length == 0) {
            return Result.failure();
        }

        File outDir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        OkHttpClient client = new OkHttpClient();

        for (String url : urls) {
            try {
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String fileName = Uri.parse(url).getLastPathSegment();
                    File outFile = new File(outDir, fileName);
                    FileOutputStream fos = new FileOutputStream(outFile);
                    fos.write(response.body().bytes());
                    fos.close();
                }
            } catch (IOException e) {
                return Result.failure();
            }
        }

        return Result.success();
    }
}
