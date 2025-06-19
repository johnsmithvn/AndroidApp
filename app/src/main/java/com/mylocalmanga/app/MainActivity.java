package com.mylocalmanga.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
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
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mylocalmanga.app.offline.OfflineDownloader;
import com.mylocalmanga.app.offline.OfflineListActivity;

public class MainActivity extends AppCompatActivity {
    private WebView web;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout rootLayout;
    private FrameLayout fullscreenContainer;
    private ImageButton ipSwitchBtn;
    private ImageButton downloadBtn;

    private final String IP_1 = "http://desktop-v88j9e0.tail2b3d3b.ts.net:3000";
    private final String IP_2 = "http://192.168.1.99:3000";

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_LAST_IP = "last_used_ip";

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
        downloadBtn.setVisibility(View.GONE);

        FrameLayout.LayoutParams dlParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.END
        );
        dlParams.setMargins(16, 16, 16, 32);
        rootLayout.addView(downloadBtn, dlParams);

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
                downloadBtn.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "🌐 Web lỗi: " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                ipSwitchBtn.setVisibility(View.GONE);
                if (url.contains("reader")) {
                    downloadBtn.setVisibility(View.VISIBLE);
                } else {
                    downloadBtn.setVisibility(View.GONE);
                }
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
            String[] options = {"📡 Dùng IP Tailscale", "💻 Dùng Localhost (127.0.0.1)", "📂 Truyện offline"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn server:");
            builder.setItems(options, (dialog, which) -> {
                if (which == 2) {
                    startActivity(new Intent(MainActivity.this, OfflineListActivity.class));
                    return;
                }
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

        // ✅ Nút tải chương hiện tại
        downloadBtn.setOnClickListener(v -> {
            String js = "(function(){var imgs=document.querySelectorAll('img');var arr=[];for(var i=0;i<imgs.length;i++){if(imgs[i].src)arr.push(imgs[i].src);}return JSON.stringify(arr);})()";
            web.evaluateJavascript(js, value -> {
                if (value == null) return;
                if (value.startsWith("\"")) {
                    value = value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\n", "").replace("\\", "");
                }
                Gson gson = new Gson();
                Type type = new TypeToken<List<String>>(){}.getType();
                List<String> urls = gson.fromJson(value, type);
                String folder = sanitizeFileName(web.getTitle());
                if (folder.isEmpty()) {
                    folder = "chapter" + System.currentTimeMillis();
                }
                OfflineDownloader.downloadImages(MainActivity.this, folder, urls, () ->
                        Toast.makeText(MainActivity.this, "Tải xong " + folder, Toast.LENGTH_SHORT).show());
            });
        });

        // ✅ Giao tiếp với JS để mở ExoPlayer
        web.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void openExoPlayer(String url) {
                Intent intent = new Intent(MainActivity.this, ExoPlayerActivity.class);
                intent.putExtra("videoUrl", url);
                startActivity(intent);
            }

            @android.webkit.JavascriptInterface
            public void downloadOffline(String folder, String jsonPaths) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<String>>(){}.getType();
                List<String> urls = gson.fromJson(jsonPaths, type);
                OfflineDownloader.downloadImages(MainActivity.this, folder, urls, () ->
                        Toast.makeText(MainActivity.this, "Tải xong " + folder, Toast.LENGTH_SHORT).show());
            }
        }, "Android");

        // ✅ Load IP đã lưu (nếu có), mặc định IP_1
        String lastIp = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getString(KEY_LAST_IP, IP_1);
        web.loadUrl(lastIp);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null) return "";
        return Pattern.compile("[\\\\/:*?\"<>|]").matcher(name).replaceAll("_");
    }
}
