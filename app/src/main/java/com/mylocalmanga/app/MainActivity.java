package com.mylocalmanga.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView web;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private ImageButton ipSwitchBtn;
    private FrameLayout rootLayout;

    private final String IP_1 = "http://desktop-v88j9e0.tail2b3d3b.ts.net:3000"; // Tailscale
    private final String IP_2 = "http://192.168.1.99:3000";                      // Localhost
    private boolean useIP1 = true;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Layout gốc chứa WebView và các view phụ
        rootLayout = new FrameLayout(this);
        web = new WebView(this);
        rootLayout.addView(web);

        // ✅ Tạo nút chọn IP (ban đầu ẩn)
        ipSwitchBtn = new ImageButton(this);
        ipSwitchBtn.setImageResource(android.R.drawable.ic_menu_manage);
        ipSwitchBtn.setBackgroundColor(Color.TRANSPARENT);
        ipSwitchBtn.setVisibility(View.GONE); // mặc định ẩn

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

        // ✅ Bắt lỗi toàn trang (main frame)
        web.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ipSwitchBtn.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "🌐 Web lỗi: " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                ipSwitchBtn.setVisibility(View.GONE); // ✅ ẩn khi load thành công
            }
        });

        // ✅ Fullscreen video + fallback xử lý lỗi DNS
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mCustomView = view;
                mCustomViewCallback = callback;
                rootLayout.addView(view); // overlay full video
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView == null) return;

                rootLayout.removeView(mCustomView);
                mCustomView = null;
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                // ✅ Nếu WebView hiện trang lỗi hệ thống (DNS, domain sai)
                if (title != null && title.toLowerCase().contains("không khả dụng")) {
                    ipSwitchBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        // ✅ Sự kiện nút chọn IP
        ipSwitchBtn.setOnClickListener(v -> {
            String[] options = {"📡 Dùng IP Tailscale", "💻 Dùng Localhost (127.0.0.1)"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn server:");
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    useIP1 = true;
                    web.loadUrl(IP_1);
                } else {
                    useIP1 = false;
                    web.loadUrl(IP_2);
                }
            });
            builder.show();
        });

        // ✅ Load trang mặc định
        web.loadUrl(IP_1);

        // ✅ Cho phép JS gọi native mở ExoPlayer
        web.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void openExoPlayer(String url) {
                Intent intent = new Intent(MainActivity.this, ExoPlayerActivity.class);
                intent.putExtra("videoUrl", url);
                startActivity(intent);
            }
        }, "Android");
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
