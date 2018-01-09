//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.khoben.samples.studyar.AR;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.khoben.samples.studyar.AR.Render.ImageRenderer;
import com.khoben.samples.studyar.AR.Render.TextureHelper;
import com.khoben.samples.studyar.DBHandler.DBHandler;
import com.khoben.samples.studyar.ImageProcessing.ImageProcessing;
import com.khoben.samples.studyar.Lesson;
import com.khoben.samples.studyar.MainActivity;

import cn.easyar.CameraCalibration;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraFrameStreamer;
import cn.easyar.Frame;
import cn.easyar.FunctorOfVoidFromPointerOfTargetAndBool;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.Renderer;
import cn.easyar.StorageType;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2I;
import cn.easyar.Vec4I;


public class HelloAR {
    private CameraDevice camera;
    private CameraFrameStreamer streamer;
    private ArrayList<ImageTracker> trackers;
    private Renderer videobg_renderer;
    private ImageRenderer box_renderer;
    private boolean viewport_changed = false;
    private Vec2I view_size = new Vec2I(0, 0);
    private int rotation = 0;
    private Vec4I viewport = new Vec4I(0, 0, 1280, 720);
    private boolean isTracked = false;
    private String current_target = "undefined";
    private String previus_target = "undefined";
    private Lesson curLesson;
    private final String TAG = "HelloAR";
    private final String PATH_TO_MARKERS = "json/auds_1.json";

    private Bitmap bitmap;
    private ImageProcessing imageProcessing;

    public HelloAR() {
        trackers = new ArrayList<ImageTracker>();
        imageProcessing = new ImageProcessing(MainActivity.activityReference);
    }

    private void loadFromImage(ImageTracker tracker, String path) {
        ImageTarget target = new ImageTarget();
        String jstr = "{\n"
                + "  \"images\" :\n"
                + "  [\n"
                + "    {\n"
                + "      \"image\" : \"" + path + "\",\n"
                + "      \"name\" : \"" + path.substring(0, path.indexOf(".")) + "\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        target.setup(jstr, StorageType.Assets | StorageType.Json, "");
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadFromJsonFile(ImageTracker tracker, String path, String targetname) {
        ImageTarget target = new ImageTarget();
        target.setup(path, StorageType.Assets, targetname);
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadAllFromJsonFile(ImageTracker tracker, String path) {
        for (ImageTarget target : ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
                @Override
                public void invoke(Target target, boolean status) {
                    Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                }
            });
        }
    }

    public boolean initialize() {
        camera = new CameraDevice();
        streamer = new CameraFrameStreamer();
        streamer.attachCamera(camera);

        boolean status = true;
        status &= camera.open(CameraDeviceType.Default);
        camera.setSize(new Vec2I(1280, 720));

        if (!status) {
            return status;
        }
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(streamer);
        loadAllFromJsonFile(tracker, PATH_TO_MARKERS);
        trackers.add(tracker);

        return status;
    }

    public void dispose() {
        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        box_renderer = null;
        if (videobg_renderer != null) {
            videobg_renderer.dispose();
            videobg_renderer = null;
        }
        if (streamer != null) {
            streamer.dispose();
            streamer = null;
        }
        if (camera != null) {
            camera.dispose();
            camera = null;
        }
    }

    public boolean start() {
        boolean status = true;
        status &= (camera != null) && camera.start();
        status &= (streamer != null) && streamer.start();
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        for (ImageTracker tracker : trackers) {
            status &= tracker.start();
        }
        return status;
    }

    public boolean stop() {
        boolean status = true;
        for (ImageTracker tracker : trackers) {
            status &= tracker.stop();
        }
        status &= (streamer != null) && streamer.stop();
        status &= (camera != null) && camera.stop();
        return status;
    }

    public void initGL() {
        if (videobg_renderer != null) {
            videobg_renderer.dispose();
        }
        videobg_renderer = new Renderer();
        box_renderer = new ImageRenderer();
        box_renderer.init();
    }

    public void resizeGL(int width, int height) {
        view_size = new Vec2I(width, height);
        viewport_changed = true;
    }

    private void updateViewport() {
        CameraCalibration calib = camera != null ? camera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.rotation) {
            this.rotation = rotation;
            viewport_changed = true;
        }
        if (viewport_changed) {
            Vec2I size = new Vec2I(1, 1);
            if ((camera != null) && camera.isOpened()) {
                size = camera.size();
            }
            if (rotation == 90 || rotation == 270) {
                size = new Vec2I(size.data[1], size.data[0]);
            }
            float scaleRatio = Math.max((float) view_size.data[0] / (float) size.data[0], (float) view_size.data[1] / (float) size.data[1]);
            Vec2I viewport_size = new Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio));
            viewport = new Vec4I((view_size.data[0] - viewport_size.data[0]) / 2, (view_size.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1]);

            if ((camera != null) && camera.isOpened())
                viewport_changed = false;
        }
    }

    public void render() {
        GLES20.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (videobg_renderer != null) {
            Vec4I default_viewport = new Vec4I(0, 0, view_size.data[0], view_size.data[1]);
            GLES20.glViewport(default_viewport.data[0], default_viewport.data[1], default_viewport.data[2], default_viewport.data[3]);
            if (videobg_renderer.renderErrorMessage(default_viewport)) {
                return;
            }
        }

        if (streamer == null) {
            return;
        }
        Frame frame = streamer.peek();
        try {
            updateViewport();
            GLES20.glViewport(viewport.data[0], viewport.data[1], viewport.data[2], viewport.data[3]);

            if (videobg_renderer != null) {
                videobg_renderer.render(frame, viewport);
            }

            isTracked = false;

            ImageTarget imagetarget = null;
            TargetInstance curtarget = null;

            for (TargetInstance targetInstance : frame.targetInstances()) {
                int status = targetInstance.status();
                if (status == TargetStatus.Tracked) {
                    curtarget = targetInstance;
                    Target target = targetInstance.target();
                    imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                    if (imagetarget == null) {
                        continue;
                    }
                    if (box_renderer != null) {
                        // Log.i(TAG,String.format("Target tracked: %s",current_target));
                        current_target = imagetarget.name();
                        isTracked = true;
                        break;
                    }
                }
            }
            if (isTracked == false) {
                current_target = "undefined";
            }

            if (!current_target.equals(previus_target)) {
                Log.i(TAG,String.format("current: %s, prev: %s",current_target,previus_target));
                if (!current_target.equals("undefined")) {
                    DBHandler.timetableReference.child(current_target).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            curLesson = dataSnapshot.getValue(Lesson.class);
                            bitmap = imageProcessing.generateBitmap(curLesson);
                            TextureHelper.updateBitmap(bitmap);
                            System.out.println(curLesson.toString());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }


                    });
                }
                else{
                    Log.i(TAG,"Bitmap is NULL");
                }
            }

            if (isTracked == true) {
                box_renderer.render(camera.projectionGL(0.2f, 500.f), curtarget.poseGL(), imagetarget.size());
            }

            previus_target = current_target;

        } finally {
            frame.dispose();
        }
    }

    public void toogleFlashlight(boolean on) {

        camera.setFlashTorchMode(on);

    }

    public String getMessage() {
        return current_target;
    }
}
