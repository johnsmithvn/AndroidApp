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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.webkit.MimeTypeMap;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView web;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout rootLayout;
    private FrameLayout fullscreenContainer;
    private ImageButton ipSwitchBtn;

    private final String IP_1 = "http://desktop-v88j9e0.tail2b3d3b.ts.net:3000";
    private final String IP_2 = "http://192.168.1.99:3000";

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_LAST_IP = "last_used_ip";

    private File pagesDir;
    private File imagesDir;

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

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

        pagesDir = new File(getFilesDir(), "offline_cache/pages");
        imagesDir = new File(getFilesDir(), "offline_cache/images");
        pagesDir.mkdirs();
        imagesDir.mkdirs();

        // ✅ Cấu hình WebView
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
        webSettings.setDatabaseEnabled(true);
        if (isNetworkAvailable()) {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            Toast.makeText(this, "Đang ở chế độ offline", Toast.LENGTH_SHORT).show();
        }

        // ✅ Bắt lỗi trang và lưu cache
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ipSwitchBtn.setVisibility(View.VISIBLE);
                web.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                File pageFile = new File(pagesDir, md5(failingUrl) + ".html");
                if (pageFile.exists()) {
                    view.loadUrl("file://" + pageFile.getAbsolutePath());
                    return;
                }
                Toast.makeText(MainActivity.this, "🌐 Web lỗi: " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                String mime = getMimeFromUrl(url);
                if (mime != null && mime.startsWith("image/")) {
                    String ext = MimeTypeMap.getFileExtensionFromUrl(url);
                    File imgFile = new File(imagesDir, md5(url) + "." + ext);
                    if (imgFile.exists()) {
                        try {
                            return new WebResourceResponse(mime, "UTF-8", new FileInputStream(imgFile));
                        } catch (Exception e) {
                            // ignore
                        }
                    } else if (isNetworkAvailable()) {
                        try {
                            URLConnection conn = new URL(url).openConnection();
                            InputStream in = conn.getInputStream();
                            byte[] data = readAll(in);
                            in.close();
                            try (FileOutputStream fos = new FileOutputStream(imgFile)) {
                                fos.write(data);
                            }
                            return new WebResourceResponse(mime, "UTF-8", new ByteArrayInputStream(data));
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                } else if (request.isForMainFrame()) {
                    File pageFile = new File(pagesDir, md5(url) + ".html");
                    if (!isNetworkAvailable() && pageFile.exists()) {
                        try {
                            return new WebResourceResponse("text/html", "UTF-8", new FileInputStream(pageFile));
                        } catch (Exception e) {
                            // ignore
                        }
                    } else if (isNetworkAvailable() && !pageFile.exists()) {
                        try {
                            URLConnection conn = new URL(url).openConnection();
                            InputStream in = conn.getInputStream();
                            byte[] data = readAll(in);
                            in.close();
                            try (FileOutputStream fos = new FileOutputStream(pageFile)) {
                                fos.write(data);
                            }
                            return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(data));
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request);
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

    private String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(s.hashCode());
        }
    }

    private byte[] readAll(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        int n;
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        while ((n = in.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }

    private String getMimeFromUrl(String url) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(url);
        if (ext == null || ext.isEmpty()) return null;
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
    }

}
