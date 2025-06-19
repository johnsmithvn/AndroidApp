package com.mylocalmanga.app.offline;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Utility for downloading manga chapters for offline viewing.
 */
public class OfflineDownloader {

    /**
     * Downloads all images from the given URLs into a folder inside external files dir "manga".
     *
     * @param context context
     * @param folderName name of the manga folder
     * @param urls list of image URLs
     * @param callback runnable to run on completion
     */
    public static void downloadImages(Context context, String folderName, List<String> urls, Runnable callback) {
        new Thread(() -> {
            try {
                File dir = new File(context.getExternalFilesDir("manga"), folderName);
                if (!dir.exists() && !dir.mkdirs()) {
                    return;
                }
                int index = 0;
                for (String urlStr : urls) {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    try (InputStream is = conn.getInputStream();
                         FileOutputStream fos = new FileOutputStream(new File(dir, index + ".jpg"))) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    } finally {
                        conn.disconnect();
                    }
                    index++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(callback);
                }
            }
        }).start();
    }
}
