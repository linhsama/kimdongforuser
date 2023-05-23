package com.example.kimdongforuser;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.MediaPlayer.Event;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.util.VLCVideoLayout;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CameraActivity extends AppCompatActivity{
    private String rtsp = "rtsp://admin:kimdong789@116.100.120.159:554/cam/realmonitor?channel=1&subtype=0";
    private String currentUrl = "";
    private String currentCode = "";
    private String currentId = "";
    private String url_login = "http://mnkimdong.ddns.net/kimdong/user-mobile/login.php/";

    private LibVLC libVlc;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoLayout;
    private EditText edtCode;

    private ChipNavigationBar bottomNavigationView;

    private SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "";
    public static final String SAVE_RTSP = "";
    public static final String SAVE_CODE = "";
    public static final String SAVE_ID = "";

    private BottomNavigationView layoutBottom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Init();
        InfoScreen();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        loadData();
        Act();

        Log.e("currentUrl", currentUrl);
        Log.e("currentCode", currentCode);
    }

    private void Act() {
        edtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    LoginService(url_login, s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void LoginService(String url_login, String code) {

        //if everything is fine
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_login,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("rtsp");
                            String code = jsonObject.getString("code");
                            Log.e("res", res);
                            if (res.equals("failed")) {
                                AlertDialog dialog_error = new AlertDialog.Builder(CameraActivity.this)
                                        .setTitle("Mã đăng nhập không chính xác")
                                        .setMessage("Vui lòng nhập lại")
                                        .setCancelable(false)
                                        .setPositiveButton("Nhập lại mã!", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                startActivity(new Intent(CameraActivity.this, DashboardActivity.class));
                                                finish();
                                            }
                                        }).create();
                                dialog_error.show();
                            } else {
                                currentUrl = res.replace("\"", "");
                                currentUrl = currentUrl.replace("\'", "");
                                currentUrl = currentUrl.replace("\\", "");
                                currentCode = code;
                                Toast.makeText(CameraActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                                closeStream();
                                openStream();
                                saveData();
                                Log.e("currentUrl", currentUrl);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(CameraActivity.this, "Hệ thống đang bảo trì!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("VolleyError", currentUrl);

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("code", code);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void Init() {

        edtCode = findViewById(R.id.edtCode);

        layoutBottom = findViewById(R.id.layoutBottom);

        libVlc = new LibVLC(this);
        mediaPlayer = new MediaPlayer(libVlc);
        videoLayout = findViewById(R.id.videoLayout);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setItemSelected(R.id.bottom_camera, true);
        bottomNavigationView.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int item) {
                if (item == R.id.bottom_dashboard) {
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    finish();
                }
                if (item == R.id.bottom_camera) {
                    startActivity(new Intent(getApplicationContext(), CameraActivity.class));
                    finish();
                }
                if (item == R.id.bottom_notifications) {
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                    finish();
                }

                if (item == R.id.bottom_back) {
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    finish();
                }

                if (item == R.id.bottom_reload) {
                    startActivity(new Intent(getApplicationContext(), CameraActivity.class));
                    finish();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshStream();
    }

    private void refreshStream() {
        mediaPlayer.release();
        libVlc.release();
    }

    private void closeStream() {
        mediaPlayer.stop();
        mediaPlayer.detachViews();
    }

    private void openStream() {

        mediaPlayer.attachViews(videoLayout, null, false, false);
        Media media = new Media(libVlc, Uri.parse(this.currentUrl));
        media.addOption(":network-caching=100");
        media.addOption(":clock-jitter=0");
        media.addOption(":clock-synchro=0");
        media.addOption(":fullscreen");

        mediaPlayer.setMedia(media);

        media.release();
        mediaPlayer.play();


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Đang mở dịch vụ camera")
                .setMessage("Vui lòng chờ...\nQuá trình có thể mất tối đa 1 phút")
                .setCancelable(false)
                .setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).create();

        AlertDialog dialog_error = new AlertDialog.Builder(this)
                .setTitle("Hệ thống đang bảo trì")
                .setMessage("Vui lòng trở lại sau")
                .setCancelable(false)
                .setPositiveButton("Trở lại sau", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(CameraActivity.this, DashboardActivity.class));
                        finish();
                    }
                }).create();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(dialog.isShowing()){
                    dialog.cancel();
                }
            }
        },50000);
        mediaPlayer.setEventListener(new org.videolan.libvlc.MediaPlayer.EventListener() {
            @Override
            public void onEvent(Event event) {
                switch (event.type) {
                    case Event.EndReached:
                    case Event.EncounteredError:
                        dialog_error.show();
                        if(dialog.isShowing()){
                            dialog.cancel();
                        }
                        break;

                    case Event.Opening:
                    case Event.Stopped:
                    case Event.Playing:
                        dialog.show();
                        break;
                    case Event.Buffering:
                        if(dialog.isShowing()){
                            dialog.cancel();
                        }
                        break;



                    default:
                        break;
                }
            }
        });

    }

    public void saveData() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(SAVE_RTSP, currentUrl);
        editor.putString(SAVE_CODE, currentCode);
        editor.commit();
    }

    private void loadData() {
        edtCode.setText(sharedpreferences.getString(SAVE_CODE, ""));
        edtCode.setText(sharedpreferences.getString(SAVE_CODE, ""));
        if (sharedpreferences.getString(SAVE_RTSP, "") != null) {
            LoginService(url_login, sharedpreferences.getString(SAVE_CODE, ""));
        } else {
            edtCode.setText("");
            Toast.makeText(CameraActivity.this, "Vui lòng nhập mã đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    void InfoScreen() {

        Display display =
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();

        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                layoutBottom.setVisibility(View.VISIBLE);
                edtCode.setVisibility(View.VISIBLE);
                break;

            case Surface.ROTATION_270:
            case Surface.ROTATION_90:
            case Surface.ROTATION_180:
                layoutBottom.setVisibility(View.GONE);
                edtCode.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        mediaPlayer.stop();
        mediaPlayer.detachViews();
    }

}