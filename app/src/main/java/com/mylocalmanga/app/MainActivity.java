package com.mylocalmanga.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.widget.EditText;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {
    private WebView web;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout rootLayout;
    private FrameLayout fullscreenContainer;
    private ImageButton ipSwitchBtn;
    private ImageButton downloadBtn;
    private ImageButton viewOfflineBtn;

    private final String IP_1 = "http://desktop-v88j9e0.tail2b3d3b.ts.net:3000";
    private final String IP_2 = "http://192.168.1.99:3000";

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_LAST_IP = "last_used_ip";
    private static final String OFFLINE_ROOT = DownloadWorker.OFFLINE_ROOT;
    private static final int STORAGE_REQUEST = 1001;
    private String pendingFolder;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Tạo layout gốc
        rootLayout = new FrameLayout(this);
        web = new WebView(this);
        rootLayout.addView(web);

        // ✅ Tạo nút đổi IP
        ipSwitchBtn = new ImageButton(this);
        ipSwitchBtn.setImageResource(android.R.drawable.ic_menu_manage);
        ipSwitchBtn.setBackgroundColor(Color.TRANSPARENT);
        ipSwitchBtn.setVisibility(View.GONE);

        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END
        );
        btnParams.setMargins(16, 64, 16, 16);
        rootLayout.addView(ipSwitchBtn, btnParams);

        // ✅ Nút tải offline
        downloadBtn = new ImageButton(this);
        downloadBtn.setImageResource(android.R.drawable.stat_sys_download);
        downloadBtn.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams downloadParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.START
        );
        downloadParams.setMargins(16, 16, 16, 80);
        rootLayout.addView(downloadBtn, downloadParams);

        // ✅ Nút xem offline
        viewOfflineBtn = new ImageButton(this);
        viewOfflineBtn.setImageResource(android.R.drawable.ic_menu_gallery);
        viewOfflineBtn.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams viewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.END
        );
        viewParams.setMargins(16, 16, 16, 80);
        rootLayout.addView(viewOfflineBtn, viewParams);

        setContentView(rootLayout);

        // ✅ Cấu hình WebView
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // ✅ Bắt lỗi trang
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ipSwitchBtn.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "🌐 Web lỗi: " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                ipSwitchBtn.setVisibility(View.GONE);
            }
        });

        // ✅ Xử lý fullscreen video HTML5
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                // 👉 Ẩn system UI
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

                mCustomView = view;
                mCustomViewCallback = callback;

                fullscreenContainer = new FrameLayout(MainActivity.this);
                fullscreenContainer.setBackgroundColor(Color.BLACK);
                fullscreenContainer.addView(view);

                rootLayout.addView(fullscreenContainer);
                web.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView == null) return;

                rootLayout.removeView(fullscreenContainer);
                fullscreenContainer = null;

                mCustomView = null;
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;

                // 👉 Reset UI
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                web.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (title != null && title.toLowerCase().contains("không khả dụng")) {
                    ipSwitchBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        // ✅ Nút đổi IP
        ipSwitchBtn.setOnClickListener(v -> {
            String[] options = {"📡 Dùng IP Tailscale", "💻 Dùng Localhost (127.0.0.1)"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn server:");
            builder.setItems(options, (dialog, which) -> {
                String selectedIp = (which == 0) ? IP_1 : IP_2;

                // ✅ Lưu IP đã chọn
                getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_LAST_IP, selectedIp)
                        .apply();

                web.loadUrl(selectedIp);
            });
            builder.show();
        });

        // ✅ Tải ảnh offline
        downloadBtn.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setHint("Folder path");
            new AlertDialog.Builder(this)
                    .setTitle("Tải offline")
                    .setView(input)
                    .setPositiveButton("OK", (d, w) -> requestPermissionAndDownload(input.getText().toString()))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // ✅ Mở thư mục offline
        viewOfflineBtn.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setHint("Folder name");
            new AlertDialog.Builder(this)
                    .setTitle("Mở offline")
                    .setView(input)
                    .setPositiveButton("OK", (d, w) -> openOffline(input.getText().toString()))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // ✅ Giao tiếp với JS để mở ExoPlayer
        web.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void openExoPlayer(String url) {
                Intent intent = new Intent(MainActivity.this, ExoPlayerActivity.class);
                intent.putExtra("videoUrl", url);
                startActivity(intent);
            }
        }, "Android");

        // ✅ Load IP đã lưu (nếu có), mặc định IP_1
        String lastIp = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getString(KEY_LAST_IP, IP_1);
        web.loadUrl(lastIp);
    }

    private void requestPermissionAndDownload(String folder) {
        pendingFolder = folder;
        if (Build.VERSION.SDK_INT >= 23) {
            String perm = (Build.VERSION.SDK_INT >= 33)
                    ? android.Manifest.permission.READ_MEDIA_IMAGES
                    : android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{perm}, STORAGE_REQUEST);
                return;
            }
        }
        startDownload(folder);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pendingFolder != null) {
                startDownload(pendingFolder);
            }
        }
    }

    private void startDownload(String folderPath) {
        String lastIp = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getString(KEY_LAST_IP, IP_1);
        String api = lastIp + "/api/folder-cache?path=" + URLEncoder.encode(folderPath, StandardCharsets.UTF_8);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(api).build();
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    java.lang.reflect.Type listType = new TypeToken<List<String>>(){}.getType();
                    List<String> urls = new Gson().fromJson(json, listType);
                    Data inputData = new Data.Builder()
                            .putStringArray("imageUrls", urls.toArray(new String[0]))
                            .putString("folderName", folderPath)
                            .build();
                    OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(DownloadWorker.class)
                            .setInputData(inputData)
                            .build();
                    WorkManager.getInstance(this).enqueue(work);
                    runOnUiThread(() -> Toast.makeText(this, "Đang tải " + urls.size() + " ảnh", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi tải danh sách", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openOffline(String folderName) {
        Intent intent = new Intent(this, OfflineReaderActivity.class);
        intent.putExtra("folderName", folderName);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
