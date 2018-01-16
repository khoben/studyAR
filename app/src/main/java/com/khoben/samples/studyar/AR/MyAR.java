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
import android.util.Pair;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.khoben.samples.studyar.AR.Render.GLView;
import com.khoben.samples.studyar.AR.Render.ImageRenderer;
import com.khoben.samples.studyar.AR.Render.TextureHelper;
import com.khoben.samples.studyar.DatabaseHelper.FirebaseHelper;
import com.khoben.samples.studyar.ImageProcessing.ImagePool;
import com.khoben.samples.studyar.ImageProcessing.ImageProcessing;
import com.khoben.samples.studyar.Lesson;
import com.khoben.samples.studyar.MainActivity;
import com.khoben.samples.studyar.MyIterator.MyIterator;
import com.khoben.samples.studyar.MyIterator.TargetContainer;

import cn.easyar.CameraCalibration;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraFrameStreamer;
import cn.easyar.Engine;
import cn.easyar.Frame;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.Renderer;
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
    private Renderer videobgRenderer;
    private ImageRenderer imageRenderer;
    private boolean viewportChanged;
    private int rotation = 0;

    private final Vec2I cameraResolution = new Vec2I(1280, 720);
    private Vec2I viewSize = new Vec2I(0, 0);
    private Vec4I viewport = new Vec4I(0, 0, cameraResolution.data[0], cameraResolution.data[1]);

    private String currentTarget;
    private String previusTarget;

    private Lesson curLesson;

    private String PATH_TO_MARKERS = "json/%s_sign_aud.json";
    private final String[] allTypes = {
            "shapes",
            "full",
            "number"
    };

    private Bitmap bitmap;
    private ImageProcessing imageProcessing;
    private boolean isFlashlightEnabled;

    private GLSurfaceView glView;
    private MainActivity mainActivity;

    private ImagePool pairObjectPool;

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
        currentTarget = null;
        previusTarget = null;
        viewportChanged = false;
        isFlashlightEnabled = false;
        pairObjectPool = new ImagePool(mainActivity);
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
            ARUtils.loadAllFromJsonFile(tracker, String.format(PATH_TO_MARKERS, type));
        }

        trackers.add(tracker);

        return status;
    }

    public void dispose() {
        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        imageRenderer = null;
        if (videobgRenderer != null) {
            videobgRenderer.dispose();
            videobgRenderer = null;
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
        assert camera != null;
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
        if (videobgRenderer != null) {
            videobgRenderer.dispose();
        }
        videobgRenderer = new Renderer();
        imageRenderer = new ImageRenderer();
        imageRenderer.init();
    }

    public void resizeGL(int width, int height) {
        viewSize = new Vec2I(width, height);
        viewportChanged = true;
    }

    private void updateViewport() {
        CameraCalibration calib = camera != null ? camera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.rotation) {
            this.rotation = rotation;
            viewportChanged = true;
        }
        if (viewportChanged) {
            Vec2I size = new Vec2I(1, 1);
            if ((camera != null) && camera.isOpened()) {
                size = camera.size();
            }
            if (rotation == 90 || rotation == 270) {
                size = new Vec2I(size.data[1], size.data[0]);
            }
            float scaleRatio = Math.max((float) viewSize.data[0] / (float) size.data[0], (float) viewSize.data[1] / (float) size.data[1]);
            Vec2I viewport_size = new Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio));
            viewport = new Vec4I((viewSize.data[0] - viewport_size.data[0]) / 2, (viewSize.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1]);

            if ((camera != null) && camera.isOpened())
                viewportChanged = false;
        }
    }

    public void render() {
        GLES20.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (videobgRenderer != null) {
            Vec4I default_viewport = new Vec4I(0, 0, viewSize.data[0], viewSize.data[1]);
            GLES20.glViewport(default_viewport.data[0], default_viewport.data[1], default_viewport.data[2], default_viewport.data[3]);
            if (videobgRenderer.renderErrorMessage(default_viewport)) {
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

            if (videobgRenderer != null) {
                videobgRenderer.render(frame, viewport);
            }

            TargetContainer targetContainer = new TargetContainer(frame.targetInstances());
            MyIterator targetIterator = targetContainer.getIterator();

            while (targetIterator.hasNext()) {
                TargetInstance targetInstance = (TargetInstance) targetIterator.next();
                int status = targetInstance.status();
                if (status == TargetStatus.Tracked) {
                    Target target = targetInstance.target();
                    ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                    if (imagetarget == null) {
                        continue;
                    }
                    if (imageRenderer != null) {
                        currentTarget = imagetarget.name();
                        if (!currentTarget.equals(previusTarget)) {
                            Log.i(TAG, String.format("current: %s, prev: %s", currentTarget, previusTarget));

                            Lesson existingLesson = pairObjectPool.findExistingLesson(currentTarget);
                            if (existingLesson == null) {
                                FirebaseHelper.timetableReference.child(currentTarget).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        curLesson = dataSnapshot.getValue(Lesson.class);
                                        new Thread(() -> {
                                            //bitmap = imageProcessing.generateBitmap(curLesson);
                                            bitmap = pairObjectPool.checkOut(curLesson).getBitmap();
                                            TextureHelper.updateBitmap(bitmap);
                                        }).start();
                                        Log.i(TAG, curLesson.toString());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, databaseError.getMessage());
                                    }
                                });
                            } else {
                                bitmap = existingLesson.getBitmap();
                                TextureHelper.updateBitmap(bitmap);
                            }
                        }
                        imageRenderer.render(camera.projectionGL(0.2f, 500.f), targetInstance.poseGL(), imagetarget.size());
                    }
                }
                previusTarget = currentTarget;
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
