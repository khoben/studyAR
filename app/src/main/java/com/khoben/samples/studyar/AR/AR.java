package com.khoben.samples.studyar.AR;

import android.opengl.GLSurfaceView;

/**
 * Created by extless on 07.01.2018.
 */

public interface AR {
    void initAR();

    GLSurfaceView getGLView();

    boolean initialize();

    void start();

    void stop();

    void dispose();

    void initGL();

    void resizeGL(int w, int h);

    void render();

    boolean getFlashlightState();

    void toogleFlashlightState();

    void onResume();

    void onPause();
}
