package com.example.a1thefull.robottest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.roobo.focusinterface.FocusManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();

        GlobalVariable.DIRECTORY_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WonderfullRecord";
        GlobalVariable.IDByANDROID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        File readFile = new File(GlobalVariable.DIRECTORY_NAME + "/opponent.txt");
        try {
            FileReader fileReader = new FileReader(readFile);
            BufferedReader br = new BufferedReader(fileReader);
            String brLine = br.readLine();
            GlobalVariable.IDOfOPPONENT = brLine;
            br.close();
        } catch (Exception e){}

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);

        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        try {
            File dir = new File(GlobalVariable.DIRECTORY_NAME);
            if (!dir.exists()){
                dir.mkdir();
            }
            GlobalVariable.getFilesFromServer(this,GlobalVariable.IDByANDROID, 1);
            GlobalVariable.getFilesFromServer(this,GlobalVariable.IDOfOPPONENT, 2);
        } catch (Exception e) {
        }

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.first_fragment, new FirstFragment()).commit();
        manager.beginTransaction().replace(R.id.second_fragment, new SecondFragment()).commit();

        try {
            if (Build.MODEL.equals("JT600"))
                FocusManager.getInstance(MainActivity.this).requestFocus("audio_recorder");

        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                anim();
                break;
            case R.id.fab1:
                DialogCheckID dialogCheckID = new DialogCheckID(MainActivity.this);
                dialogCheckID.callFunction();
                break;
            case R.id.fab2:
                DialogWriteID dialogWriteID = new DialogWriteID(MainActivity.this);
                dialogWriteID.callFunction();
                break;
        }
    }

    public void anim(){
        if (isFabOpen) {
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab1.hide();
            fab2.hide();
            isFabOpen = false;
        } else {
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab1.show();
            fab2.show();
            isFabOpen = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            if (Build.MODEL.equals("JT600"))
                FocusManager.getInstance(this).releaseFocus("audio_recorder");
        } catch (Exception e) {
        }
    }

    public void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.INTERNET}, 1);
        } else {

        }
    }
}