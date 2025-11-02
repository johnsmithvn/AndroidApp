package com.mylocalmanga.app;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
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
    private static final int REQUEST_WRITE_STORAGE = 112;

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

        // ✅ THÊM MỚI: Xử lý download trong WebView
        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    // Lấy tên file từ Content-Disposition hoặc URL
                    String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                    
                    // Tạo download request
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimetype);
                    
                    // Thêm cookies nếu có (để maintain session)
                    String cookies = CookieManager.getInstance().getCookie(url);
                    if (cookies != null) {
                        request.addRequestHeader("Cookie", cookies);
                    }
                    request.addRequestHeader("User-Agent", userAgent);
                    
                    // Set notification và destination
                    request.setDescription("Đang tải xuống nhạc...");
                    request.setTitle(fileName);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, fileName);
                    
                    // Bắt đầu download
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    if (dm != null) {
                        dm.enqueue(request);
                        Toast.makeText(MainActivity.this, "📥 Bắt đầu tải: " + fileName, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "❌ Lỗi download: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
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

        // ✅ Giao tiếp với JS để mở ExoPlayer và download
        web.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void openExoPlayer(String url) {
                Intent intent = new Intent(MainActivity.this, ExoPlayerActivity.class);
                intent.putExtra("videoUrl", url);
                startActivity(intent);
            }
            
            @android.webkit.JavascriptInterface
            public void downloadFile(String url, String fileName, String mimeType) {
                runOnUiThread(() -> {
                    try {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setMimeType(mimeType != null ? mimeType : "audio/mpeg");
                        
                        // Thêm cookies và headers
                        String cookies = CookieManager.getInstance().getCookie(url);
                        if (cookies != null) {
                            request.addRequestHeader("Cookie", cookies);
                        }
                        
                        // Lấy loại media từ MIME type hoặc extension
                        String mediaType = getMediaTypeFolder(mimeType, fileName);
                        
                        // Lấy thư mục ngày hiện tại (format: YYYYMMDD)
                        String dateFolder = getCurrentDateFolder();
                        
                        // Tạo đường dẫn: AppDownload/[Music|Video|Picture]/YYYYMMDD/
                        String relativePath = "AppDownload/" + mediaType + "/" + dateFolder + "/";
                        
                        // Set notification và destination
                        request.setDescription("Đang tải xuống " + mediaType.toLowerCase() + "...");
                        request.setTitle(fileName);
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, relativePath + fileName);
                        
                        // Start download
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        if (dm != null) {
                            dm.enqueue(request);
                            
                            // Toast với thông tin chi tiết
                            String fullPath = "/Download/" + relativePath + fileName;
                            String message = "📥 Đang tải " + mediaType + "\n" +
                                           "📅 " + dateFolder + "\n" +
                                           "📂 " + fullPath;
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });
            }
            
            @android.webkit.JavascriptInterface
            public boolean isWebView() {
                return true;
            }
        }, "Android");

        // ✅ Request storage permission cho Android 6.0+ (API 23+)
        checkStoragePermission();

        // ✅ Load IP đã lưu (nếu có), mặc định IP_1
        String lastIp = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getString(KEY_LAST_IP, IP_1);
        web.loadUrl(lastIp);
    }

    // ✅ THÊM MỚI: Kiểm tra và yêu cầu quyền storage
    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Hiện dialog giải thích tại sao cần quyền này
                new AlertDialog.Builder(this)
                    .setTitle("Cần quyền truy cập bộ nhớ")
                    .setMessage("App cần quyền này để tải nhạc về máy. Bạn có đồng ý không?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        requestPermissions(new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        }, REQUEST_WRITE_STORAGE);
                    })
                    .setNegativeButton("Từ chối", (dialog, which) -> {
                        Toast.makeText(MainActivity.this, 
                            "⚠️ Không thể tải nhạc nếu không có quyền storage", 
                            Toast.LENGTH_LONG).show();
                    })
                    .show();
            }
        }
    }

    // ✅ THÊM MỚI: Xử lý kết quả request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Đã cấp quyền! Bạn có thể tải nhạc về máy", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Không có quyền storage, không thể tải nhạc", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    // ✅ Helper: Lấy thư mục ngày hiện tại (format: YYYYMMDD)
    private String getCurrentDateFolder() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
    
    // ✅ Helper: Xác định loại media từ MIME type hoặc file extension
    private String getMediaTypeFolder(String mimeType, String fileName) {
        // Check MIME type trước
        if (mimeType != null) {
            if (mimeType.startsWith("audio/")) return "Music";
            if (mimeType.startsWith("video/")) return "Video";
            if (mimeType.startsWith("image/")) return "Picture";
        }
        
        // Fallback: Check extension
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".mp3") || lowerFileName.endsWith(".flac") || 
            lowerFileName.endsWith(".wav") || lowerFileName.endsWith(".m4a") || 
            lowerFileName.endsWith(".ogg") || lowerFileName.endsWith(".aac")) {
            return "Music";
        }
        if (lowerFileName.endsWith(".mp4") || lowerFileName.endsWith(".mkv") || 
            lowerFileName.endsWith(".avi") || lowerFileName.endsWith(".mov") || 
            lowerFileName.endsWith(".webm")) {
            return "Video";
        }
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") || 
            lowerFileName.endsWith(".png") || lowerFileName.endsWith(".gif") || lowerFileName.endsWith(".ifjf") || 
            lowerFileName.endsWith(".webp")) {
            return "Picture";
        }
        
        // Default
        return "Other";
    }
}
