package com.mylocalmanga.app;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Map;
import java.util.HashMap;
public class MainActivity extends AppCompatActivity {
    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);

        // 👉 Bật JS và các quyền truy cập cần thiết
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // ✅ Cho phép localStorage nếu web có dùng
        webSettings.setAllowFileAccess(true);    // ✅ Cho phép load file nếu có
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // ✅ Cho phép HTTP nội bộ

        web.setWebViewClient(new WebViewClient());
        Map<String, String> headers = new HashMap<>();

//
        String IP="http://desktop-v88j9e0.tail2b3d3b.ts.net:3000";
        web.loadUrl(IP); // Thay đúng IP Tailscale cua PC

        setContentView(web);
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
