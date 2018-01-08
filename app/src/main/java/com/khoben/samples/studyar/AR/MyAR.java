package com.khoben.samples.studyar.AR;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.khoben.samples.studyar.AR.Render.GLView;
import com.khoben.samples.studyar.ImageProcessing.ImageProcessing;
import com.khoben.samples.studyar.MainActivity;
import com.khoben.samples.studyar.TimetableManager.TimetableManager;

import cn.easyar.Engine;


public class MyAR implements AR {
    private final String TAG = "MyAR";
    private static String key = "QjLaKui9g0HpqU2wzKEdmoCSMIrUIyh8LjZJz2JV" +
            "7qGXo80itvYjVzYco59Z3EVSMdGlC3OBJBgsbJ1bW4KJ0lzbAVE34SbjVz0lsEqG0ghnVuosGMnEqghEVARJzgkSsv137IphBLM21z9vVMlmoNagwCsVpejLAGy6um7PE6PRqx4Fn7oRtvQVJpu0u9ILEUqtQu2K";

    private static boolean isFlashlightEnabled = false;

    private GLView glView;

    private MainActivity mainActivity;
    private ImageProcessing imageProcessing;
    private TimetableManager timetableManager;

    private HelloAR helloAR;
    private String lastMessage;

    public static Bitmap bitmap;
    public volatile static boolean needToChange = false;

    public MyAR(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        helloAR = new HelloAR();
        glView = new GLView(mainActivity, this);
        imageProcessing = new ImageProcessing(mainActivity);
        timetableManager = new TimetableManager();
        lastMessage = "undefined";
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
    public boolean initialize() {
        return helloAR.initialize();
    }

    @Override
    public void start() {
        helloAR.start();
    }

    @Override
    public void stop() {
        helloAR.stop();
    }

    @Override
    public void dispose() {
        helloAR.dispose();
    }


    @Override
    public void initGL() {
        helloAR.initGL();
    }

    @Override
    public void resizeGL(int w, int h) {
        helloAR.resizeGL(w, h);
    }

    @Override
    public void render() {
        updateImage();
        helloAR.render();
    }

    @Override
    public boolean getFlashlightState() {
        return isFlashlightEnabled;
    }

    @Override
    public void toogleFlashlightState() {
        isFlashlightEnabled = !isFlashlightEnabled;
        helloAR.toogleFlashlight(isFlashlightEnabled);
    }

    @Override
    public void onResume() {
        glView.onResume();
        setDefFlashlightState();
    }

    @Override
    public void onPause() {
        glView.onPause();
    }

    public void setDefFlashlightState() {
        isFlashlightEnabled = false;
    }

    public void updateImage() {
        String message = helloAR.getMessage();
        if (!message.equals(lastMessage)) {
            if (message.equals("undefined")) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
            } else {
                bitmap = imageProcessing.generateBitmap(timetableManager.getLessonByAud(message));
                Log.i(TAG, String.format("new message: %s old message: %s", message, lastMessage));
            }
            needToChange = true;
        } else
            needToChange = false;
        lastMessage = message;

    }
}
