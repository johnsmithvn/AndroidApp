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

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView web;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout rootLayout;
    private FrameLayout fullscreenContainer;
    private ImageButton ipSwitchBtn;
    private ImageButton offlineBtn;

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

        String offlineFile = getIntent().getStringExtra("offlineFile");

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

        // ❇️ Nút mở offline
        offlineBtn = new ImageButton(this);
        offlineBtn.setImageResource(android.R.drawable.ic_menu_save);
        offlineBtn.setBackgroundColor(Color.TRANSPARENT);
        offlineBtn.setVisibility(View.GONE);
        FrameLayout.LayoutParams btnParams2 = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.START
        );
        btnParams2.setMargins(16, 64, 16, 16);
        rootLayout.addView(offlineBtn, btnParams2);

        setContentView(rootLayout);

        // ✅ Cấu hình WebView
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        if (offlineFile != null) {
            web.loadUrl("file://" + offlineFile);
            return;
        }

        // ✅ Bắt lỗi trang
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ipSwitchBtn.setVisibility(View.VISIBLE);
                offlineBtn.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "🌐 Web lỗi: " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                ipSwitchBtn.setVisibility(View.GONE);
                offlineBtn.setVisibility(View.GONE);
                injectOfflineButton();
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

        // ❇️ Mở OfflineActivity
        offlineBtn.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, OfflineActivity.class);
            startActivity(i);
        });

        // ✅ Giao tiếp với JS
        web.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void openExoPlayer(String url) {
                Intent intent = new Intent(MainActivity.this, ExoPlayerActivity.class);
                intent.putExtra("videoUrl", url);
                startActivity(intent);
            }

            @android.webkit.JavascriptInterface
            public void downloadImages(String json) {
                saveImages(json);
            }
        }, "Android");

        // ✅ Load IP đã lưu (nếu có), mặc định IP_1
        String lastIp = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getString(KEY_LAST_IP, IP_1);
        web.loadUrl(lastIp);
    }

    private void injectOfflineButton() {
        String js = "(function(){" +
                "if(window.location.pathname.includes('reader')){" +
                "var b=document.getElementById('androidOfflineBtn');" +
                "if(!b){" +
                " b=document.createElement('button');b.id='androidOfflineBtn';" +
                " b.innerText='Tải Offline';" +
                " b.style.position='fixed';b.style.bottom='20px';b.style.right='20px';" +
                " b.style.zIndex='9999';" +
                " b.onclick=function(){var imgs=[].slice.call(document.querySelectorAll('img')).map(i=>i.src);Android.downloadImages(JSON.stringify(imgs));};" +
                " document.body.appendChild(b);" +
                "}" +
                "}" +
                "})();";
        web.evaluateJavascript(js, null);
    }

    private void saveImages(String json) {
        new Thread(() -> {
            com.google.gson.Gson g = new com.google.gson.Gson();
            String[] urls = g.fromJson(json, String[].class);
            java.io.File dir = new java.io.File(getExternalFilesDir(null), "offline");
            dir.mkdirs();
            int count = 0;
            for (String u : urls) {
                try {
                    java.net.URL url = new java.net.URL(u);
                    java.io.InputStream in = url.openStream();
                    java.io.File out = new java.io.File(dir, "img_" + count + ".jpg");
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(out);
                    byte[] buf = new byte[8192];
                    int n; while ((n = in.read(buf)) != -1) { fos.write(buf, 0, n); }
                    fos.close();
                    in.close();
                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int finalCount = count;
            runOnUiThread(() ->
                    Toast.makeText(MainActivity.this, "Đã lưu " + finalCount + " ảnh", Toast.LENGTH_LONG).show()
            );
        }).start();
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
