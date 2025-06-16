package com.mylocalmanga.app.offline;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OfflineUtils {
    private static final String TAG = "OfflineUtils";

    public static File getOfflineRoot(Context context) {
        return new File(context.getExternalFilesDir(null), "offline");
    }

    public static void savePage(Context context, String title, String urlsJson) {
        try {
            JSONArray array = new JSONArray(urlsJson);
            File root = getOfflineRoot(context);
            if (!root.exists()) root.mkdirs();
            String safeTitle = title.replaceAll("[^a-zA-Z0-9._-]", "_");
            File pageDir = new File(root, safeTitle);
            if (!pageDir.exists()) pageDir.mkdirs();
            StringBuilder html = new StringBuilder();
            html.append("<html><body style='margin:0;padding:0;'>\n");
            for (int i = 0; i < array.length(); i++) {
                String urlStr = array.getString(i);
                String fileName = i + ".jpg";
                File outFile = new File(pageDir, fileName);
                downloadFile(urlStr, outFile);
                html.append("<img src='").append(fileName).append("' style='width:100%;'/>\n");
            }
            html.append("</body></html>");
            FileOutputStream fos = new FileOutputStream(new File(pageDir, "index.html"));
            fos.write(html.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "savePage", e);
        }
    }

    private static void downloadFile(String urlStr, File outFile) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream in = conn.getInputStream();
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "downloadFile", e);
        }
    }
}
