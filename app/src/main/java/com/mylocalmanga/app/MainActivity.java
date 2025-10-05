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

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView web;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout rootLayout;
    private FrameLayout fullscreenContainer;
    private ImageButton ipSwitchBtn;

    private final String IP_1 = "http://desktop-v88j9e0.tail2b3d3b.ts.net:3000";
    private final String IP_2 = "http://desktop-v88j9e0.tail2b3d3b.ts.net:3001";
    private final String IP_3 = "https://desktop-v88j9e0.tail2b3d3b.ts.net:3000";
    private final String IP_4 = "https://desktop-v88j9e0.tail2b3d3b.ts.net:3001";
    private final String IP_5 = "http://192.168.1.99:3000";
    private final String IP_6 = "http://192.168.1.99:3001";

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

        setContentView(rootLayout);

        // ✅ Cấu hình WebView
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // ✅ THÊM MỚI: Enable zoom support
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false); // Ẩn nút zoom, chỉ dùng pinch-to-zoom
        
        // ✅ THÊM MỚI: Enable viewport meta tag
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // ✅ Bắt lỗi trang + bỏ qua SSL error
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

            @Override
            public void onReceivedSslError(WebView view,
                                           android.webkit.SslErrorHandler handler,
                                           android.net.http.SslError error) {
                // ⚠️ Bỏ qua cảnh báo SSL cho HTTPS tự ký
                handler.proceed();
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
            String[] options = {"tailscale http","tailscale https","tailscale https (port 3000)", "tailscale https (port 3001)", "local http", "local http (port 3001)"};
            String[] urls    = {IP_1, IP_2, IP_3, IP_4, IP_5, IP_6};

            new AlertDialog.Builder(this)
                    .setTitle("Chọn server:")
                    .setItems(options, (d, which) -> {
                        String url = urls[which];
                        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                                .putString(KEY_LAST_IP, url).apply();
                        web.loadUrl(url);
                    })
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

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
