//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.khoben.samples.studyar;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.khoben.samples.studyar.AR.AR;

import java.util.HashMap;

import com.khoben.samples.studyar.AR.MyAR;
import com.khoben.samples.studyar.DatabaseHelper.FirebaseHelper;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private GLSurfaceView glView;
    private AR ar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FirebaseHelper.init();

        ar = MyAR.getInstance(this);
        ar.initAR();

        glView = ar.getGLView();

        FirebaseHelper.titleReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                try {
                    getSupportActionBar().setTitle(value);
                }
                catch (NullPointerException e){
                    Log.e(TAG,e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                ((ViewGroup) findViewById(R.id.preview)).addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private interface PermissionCallback {
        void onSuccess();

        void onFailure();
    }

    private HashMap<Integer, PermissionCallback> permissionCallbacks = new HashMap<Integer, PermissionCallback>();
    private int permissionRequestCodeSerial = 0;

    @TargetApi(23)
    private void requestCameraPermission(PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int requestCode = permissionRequestCodeSerial;
                permissionRequestCodeSerial += 1;
                permissionCallbacks.put(requestCode, callback);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                callback.onSuccess();
            }
        } else {
            callback.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionCallbacks.containsKey(requestCode)) {
            PermissionCallback callback = permissionCallbacks.get(requestCode);
            permissionCallbacks.remove(requestCode);
            boolean executed = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true;
                    callback.onFailure();
                }
            }
            if (!executed) {
                callback.onSuccess();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glView != null) {
            glView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (glView != null) {
            glView.onPause();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.flashlight:
                ar.toogleFlashlightState();

                if (ar.getFlashlightState()) {
                    item.setIcon(R.drawable.flashlight_off);
                } else
                    item.setIcon(R.drawable.flashlight);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
