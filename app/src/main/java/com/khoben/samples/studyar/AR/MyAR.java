//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.khoben.samples.studyar.AR;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.khoben.samples.studyar.AR.Render.GLView;
import com.khoben.samples.studyar.AR.Render.ImageRenderer;
import com.khoben.samples.studyar.AR.Render.TextureHelper;
import com.khoben.samples.studyar.DatabaseHelper.FirebaseHelper;
import com.khoben.samples.studyar.ImageProcessing.ImageProcessing;
import com.khoben.samples.studyar.Lesson;
import com.khoben.samples.studyar.MainActivity;

import cn.easyar.CameraCalibration;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraFrameStreamer;
import cn.easyar.Engine;
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


public class MyAR implements AR {

    private final String TAG = "MyAR";

    private static String key = "QjLaKui9g0HpqU2wzKEdmoCSMIrUIyh8LjZJz2JV" +
            "7qGXo80itvYjVzYco59Z3EVSMdGlC3OBJBgsbJ1bW4KJ0lzbAVE34SbjVz0lsEqG0ghnVuosGMnEqghEVARJzgkSsv137IphBLM21z9vVMlmoNagwCsVpejLAGy6um7PE6PRqx4Fn7oRtvQVJpu0u9ILEUqtQu2K";

    private CameraDevice camera;
    private CameraFrameStreamer streamer;
    private ArrayList<ImageTracker> trackers;
    private Renderer videobg_renderer;
    private ImageRenderer box_renderer;
    private boolean viewport_changed;
    private int rotation = 0;

    private final Vec2I cameraResolution = new Vec2I(1280, 720);
    private Vec2I view_size = new Vec2I(0, 0);
    private Vec4I viewport = new Vec4I(0, 0, 1280, 720);

    private String current_target;
    private String previus_target;

    private Lesson curLesson;

    private String PATH_TO_MARKERS = "json/%s_sign_aud.json";
    private final String typeSign = "shapes"; // number | shapes | full
    private final String[] allTypes = {
            "shapes",
            "number",
            "full"
    };

    private Bitmap bitmap;
    private ImageProcessing imageProcessing;
    private boolean isFlashlightEnabled;

    private GLSurfaceView glView;
    private MainActivity mainActivity;

    private static volatile MyAR instance;

    public static MyAR getInstance(MainActivity mainActivity) {
        MyAR localInstance = instance;
        if (localInstance == null) {
            synchronized (MyAR.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new MyAR(mainActivity);
                }
            }
        }
        return localInstance;
    }

    private MyAR(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        glView = new GLView(mainActivity, this);
        trackers = new ArrayList<>();
        imageProcessing = new ImageProcessing(mainActivity);
        current_target = null;
        previus_target = null;
        viewport_changed = false;
        isFlashlightEnabled = false;

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

    @Override
    public void initAR() {
        if (!Engine.initialize(mainActivity, key)) {
            Log.e(TAG, "Initialization Failed.");
        }
    }

    @Override
    public GLSurfaceView getGLView() {
        return glView;
    }

    @Override
    public void setGLView(GLSurfaceView glView) {
        this.glView = glView;
    }

    public boolean initialize() {
        camera = new CameraDevice();
        streamer = new CameraFrameStreamer();
        streamer.attachCamera(camera);

        boolean status = true;
        status &= camera.open(CameraDeviceType.Default);
        camera.setSize(cameraResolution);

        if (!status) {
            return status;
        }
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(streamer);


        for (String type : allTypes) {
            loadAllFromJsonFile(tracker, String.format(PATH_TO_MARKERS, type));
        }

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

            for (TargetInstance targetInstance : frame.targetInstances()) {
                int status = targetInstance.status();
                if (status == TargetStatus.Tracked) {
                    TargetInstance curtarget = targetInstance;
                    Target target = targetInstance.target();
                    ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                    if (imagetarget == null) {
                        continue;
                    }
                    if (box_renderer != null) {
                        current_target = imagetarget.name();
                        if (!current_target.equals(previus_target)) {
                            Log.i(TAG, String.format("current: %s, prev: %s", current_target, previus_target));
                            FirebaseHelper.timetableReference.child(current_target).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    curLesson = dataSnapshot.getValue(Lesson.class);
                                    new Thread(new Runnable() {
                                        public void run() {
                                            bitmap = imageProcessing.generateBitmap(curLesson);
                                            TextureHelper.updateBitmap(bitmap);
                                        }
                                    }).start();
                                    Log.i(TAG, curLesson.toString());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e(TAG, databaseError.getMessage().toString());
                                }
                            });
                        }
                        box_renderer.render(camera.projectionGL(0.2f, 500.f), curtarget.poseGL(), imagetarget.size());
                    }
                }
                previus_target = current_target;
            }

        } finally {
            frame.dispose();
        }
    }

    @Override
    public boolean getFlashlightState() {
        return isFlashlightEnabled;
    }

    @Override
    public void toogleFlashlightState() {
        isFlashlightEnabled = !isFlashlightEnabled;
        toogleFlashlight(isFlashlightEnabled);
    }

    @Override
    public void onResume() {
        glView.onResume();
    }

    @Override
    public void onPause() {
        glView.onPause();
    }

    public void toogleFlashlight(boolean on) {
        camera.setFlashTorchMode(on);
    }
}
