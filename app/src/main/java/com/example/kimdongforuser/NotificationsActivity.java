package com.example.kimdongforuser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NotificationsActivity extends AppCompatActivity {

    private String currentUrl = "http://mnkimdong.ddns.net/kimdong/user-mobile/hoc-phi/";
    private WebView myWebView;
    private ProgressBar progressBar;

    private ChipNavigationBar bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Init();
        Act();
        //Phuc hoi
        if (savedInstanceState != null) {
            currentUrl = savedInstanceState.getString("currentUrl");
        }
    }

    private void Act() {

        // startWebView
        startWebView();

        // swipeRefreshLayout
        // swipeRefreshLayout();


        myWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigationView.setVisibility(View.GONE);
            }
        });


    }

    private void Init() {

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setItemSelected(R.id.bottom_notifications, true);
        bottomNavigationView.showBadge(R.id.bottom_notifications);
        bottomNavigationView.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int item) {
                if (item ==  R.id.bottom_dashboard) {
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    finish();
                }
                if (item ==  R.id.bottom_camera) {
                    startActivity(new Intent(getApplicationContext(), CameraActivity.class));
                    finish();
                }
                if (item ==  R.id.bottom_notifications) {
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                    finish();
                }
                if (item ==  R.id.bottom_back) {
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    finish();
                }
                if (item ==  R.id.bottom_reload) {
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                    finish();
                }
            }
        });

        // initConfig
        initConfig();
    }
    private void checkDownloadPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(NotificationsActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(NotificationsActivity.this, "Vui lòng chấp nhận quyền truy cập bộ nhớ!", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(NotificationsActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    private void initConfig() {
        String[] permissionsStorage = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
        int requestExternalStorage = 1;
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsStorage, requestExternalStorage);
        }

        myWebView = (WebView) findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);
        Intent intent = getIntent();
        // kiểm tra có lưu prefs không
        if (intent.hasExtra("IPSERVER")) {
            currentUrl = intent.getStringExtra("IPSERVER");
            Log.e("kimdong_intent", currentUrl); // => Mầm non Kim Đồng
        }
    }


    private void startWebView() {

        // settings
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.getSettings().setAllowContentAccess(true);
        myWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        myWebView.getSettings().setLoadsImagesAutomatically(true);
        myWebView.getSettings().setGeolocationEnabled(true);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        myWebView.getSettings().setDatabaseEnabled(true);
        myWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        myWebView.getSettings().setUserAgentString(new WebView(this).getSettings().getUserAgentString());

//        myWebView.setDownloadListener(new DownloadListener() {
//            public void onDownloadStart(String url, String userAgent,
//                                        String contentDisposition, String mimetype,
//                                        long contentLength) {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
//            }
//
//        });


        myWebView.setDownloadListener(new DownloadListener() {


            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                try {
                    String fileName = (contentDisposition).replace("attachment; filename=", "");

                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                    request.setAllowedOverRoaming(false).setTitle(fileName) //Download Manager Title
                            .setDescription("Downloading...") // Download manager Discription
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS, // It can be any stanaderd directory Like DCIM,Downloads etc...
                                    "/KimDong/" + fileName // Your Custom directory name/Your Image file name
                            );
                    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Đang tải xuống..."  + fileName, Toast.LENGTH_SHORT).show();
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                }catch (Exception e){
                    Toast.makeText(NotificationsActivity.this, "Lỗi tải xuống " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("fileName", "Lỗi tải xuống " + e.getMessage());

                }

            }
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(progressBar.getVisibility() == ProgressBar.VISIBLE){
                        progressBar.setVisibility(ProgressBar.GONE);
                    }

                    Toast.makeText(getApplicationContext(), "Tải xuống hoàn tất", Toast.LENGTH_SHORT).show();
                }
            };
        });

        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                return super.onJsBeforeUnload(view, url, message, result);
            }

            public void onProgressChanged(WebView view, int progress) {
                if(progress < 100 && progressBar.getVisibility() == View.GONE){
                    progressBar.setVisibility(View.VISIBLE);
                }

                progressBar.setProgress(progress);
                if(progress > 70) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        myWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                currentUrl = url;
                view.loadUrl(currentUrl);

                Log.e("currentUrl",currentUrl.toString());

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                currentUrl = url;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    currentUrl = url;

                } catch (Exception exception) {
                    Toast.makeText(NotificationsActivity.this, "Có lỗi: " + exception.toString(), Toast.LENGTH_SHORT).show();
                    exception.printStackTrace();
                }
            }
        });

        // load url in webview
        myWebView.loadUrl(currentUrl);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentUrl", currentUrl);

    }

//        private void swipeRefreshLayout() {
//        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                startWebView();
//                swipeRefreshLayout.setRefreshing(false);
//
//            }
//        });
//    }

    private int count = 0;

    @Override
    public void onBackPressed() {
        count++;
        if (count >=1) {

            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();

            Toast.makeText(this, "Nhấn lần nữa để thoát", Toast.LENGTH_SHORT).show();
        } else {

            // resetting the counter in 2s
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    count = 0;
                }
            }, 2000);
        }
        super.onBackPressed();
    }

}