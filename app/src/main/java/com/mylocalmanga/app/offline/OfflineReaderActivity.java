package com.mylocalmanga.app.offline;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class OfflineReaderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);

        String folder = getIntent().getStringExtra("folder");
        File root = OfflineUtils.getOfflineRoot(this);
        File pageDir = new File(root, folder);
        File index = new File(pageDir, "index.html");
        webView.loadUrl("file://" + index.getAbsolutePath());
    }
}
