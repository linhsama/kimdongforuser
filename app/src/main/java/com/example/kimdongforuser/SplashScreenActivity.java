package com.example.kimdongforuser;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import static com.example.kimdongforuser.CameraActivity.MyPREFERENCES;
import static com.example.kimdongforuser.CameraActivity.SAVE_CODE;
import static com.example.kimdongforuser.CameraActivity.SAVE_RTSP;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {

    private String url_login  = "http://mnkimdong.ddns.net/kimdong/user-mobile/check-service.php/";
    private ImageView imageView;
    private SharedPreferences sharedpreferences;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        imageView = findViewById(R.id.imageView);
        imageView.animate().alpha(4000).setDuration(10);
        isConnect();
    }
    private void CheckService(String url_login) {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Hệ thống đang bảo trì")
                .setMessage("Vui lòng trở lại sau")
                .setCancelable(false)
                .setPositiveButton("Trở lại!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_login,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("res", response);
                        startActivity(new Intent(SplashScreenActivity.this, DashboardActivity.class));
                        finish();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.show();
                Log.e("VolleyError", error.getMessage());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void isConnect() {


        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            CheckService(url_login);
        }else{
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Không có kết nối internet")
                    .setMessage("Vui lòng kết nối WIFI hoặc 4G")
                    .setCancelable(false)
                    .setPositiveButton("Thử lại", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(SplashScreenActivity.this, SplashScreenActivity.class));
                        }
                    }).create();
            dialog.show();
        }
    }
}