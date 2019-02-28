package com.example.a1thefull.robottest;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.roobo.core.scene.SceneEventListener;
import com.roobo.core.scene.SceneHelper;

import java.io.Serializable;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        final Intent intent = new Intent(MyApplication.this, MainActivity.class);
        if (!Build.MODEL.equals("JT600")) {
            MyApplication.this.startActivity(intent);
        } else {
            SceneHelper.initialize(this);
            SceneHelper.setEventListener(new SceneEventListener() {
                @Override
                public void onSwitchIn(int flags) {
                    super.onSwitchIn(flags);
                }

                @Override
                public void onSwitchOut() {
                    super.onSwitchOut();
                }

                @Override
                public void onCommand(String action, Bundle params, Serializable suggestion) {
                    super.onCommand(action, params, suggestion);
                    Log.v("hello_roobo_tag", "FirstScene onCommand: " + action + "  params = " + params);
                    if (action.equals("Open") || action.equals("SearchPackage")) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MyApplication.this.startActivity(intent);
                    }
                }
            });
        }
    }
}