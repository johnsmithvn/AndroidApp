package com.mylocalmanga.app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    WebView web;

    String IP_1 = "http://desktop-v88j9e0.tail2b3d3b.ts.net:3000"; // IP Tailscale
    String IP_2 = "http://192.168.1.192323:3000";                         // Localhost
    boolean useIP1 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        setContentView(web); // ✅ Không dùng SwipeRefreshLayout

        // 👉 Bật JS và các quyền truy cập cần thiết
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // ✅ localStorage
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // ✅ Bật cache

        web.setWebViewClient(new WebViewClient());

        web.loadUrl(IP_1); // ✅ Load mặc định
    }

    // ✅ Thêm nút menu "Chọn địa chỉ"
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Chọn địa chỉ");
        return true;
    }

    // ✅ Show dialog chọn IP
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Chọn địa chỉ")) {
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
            return true;
        }
        return super.onOptionsItemSelected(item);
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
