package com.mylocalmanga.app.offline;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
/**
 * Utility for downloading manga chapters for offline viewing.
 */
public class OfflineDownloader {

    /**
     * Fetches image URLs from the given API endpoint.
     *
     * @param apiUrl URL to the folder-cache API
     * @param baseUrl base URL of the website to prefix relative paths
     * @return list of absolute image URLs
     */
    public static List<String> fetchImageUrls(String apiUrl, String baseUrl) throws Exception {
        List<String> result = new ArrayList<>();
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        try (InputStream is = new BufferedInputStream(conn.getInputStream());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            JSONObject obj = new JSONObject(baos.toString());
            JSONArray arr = obj.getJSONArray("images");
            for (int i = 0; i < arr.length(); i++) {
                String p = arr.getString(i);
                if (p.startsWith("http")) {
                    result.add(p);
                } else {
                    result.add(baseUrl + p);
                }
            }
        } finally {
            conn.disconnect();
        }
        return result;
    }

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
