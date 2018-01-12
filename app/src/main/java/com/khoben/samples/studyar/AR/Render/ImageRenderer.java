//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.khoben.samples.studyar.AR.Render;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import cn.easyar.Vec2F;
import cn.easyar.Matrix44F;

public class ImageRenderer {
    private int shaderProgram;
    private int posCoord;
    private int posTex;
    private int posTrans;
    private int posProj;

    private int vboCoord;
    private int vboTex;
    private int vboFaces;

    private final String TAG = "ImageRenderer";

    private final String vertexShaderProgram = "uniform mat4 trans;\n"
            + "uniform mat4 proj;\n"
            + "attribute vec4 coord;\n"
            + "attribute vec2 texcoord;\n"
            + "varying vec2 vtexcoord;\n"
            + "\n"
            + "void main(void)\n"
            + "{\n"
            + "    vtexcoord = texcoord;\n"
            + "    gl_Position = proj*trans*coord;\n"
            + "}\n"
            + "\n";

    private final String fragmentShaderProgram = "#ifdef GL_ES\n"
            + "precision highp float;\n"
            + "#endif\n"
            + "varying vec2 vtexcoord;\n"
            + "uniform sampler2D texture;\n"
            + "\n"
            + "void main(void)\n"
            + "{\n"
            + "    gl_FragColor = texture2D(texture, vtexcoord);\n"
            + "}\n"
            + "\n";

    private float[] flatten(float[][] a) {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        float[] l = new float[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private int[] flatten(int[][] a) {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        int[] l = new int[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private short[] flatten(short[][] a) {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        short[] l = new short[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private byte[] flatten(byte[][] a) {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        byte[] l = new byte[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private byte[] byteArrayFromIntArray(int[] a) {
        byte[] l = new byte[a.length];
        for (int k = 0; k < a.length; k += 1) {
            l[k] = (byte) (a[k] & 0xFF);
        }
        return l;
    }

    private int generateOneBuffer() {
        int[] buffer = {0};
        GLES20.glGenBuffers(1, buffer, 0);
        return buffer[0];
    }

    private int generateOneTexture() {
        int[] buffer = {0};
        GLES20.glGenTextures(1, buffer, 0);
        return buffer[0];
    }

    public void init() {
        shaderProgram = GLES20.glCreateProgram();
        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, vertexShaderProgram);
        GLES20.glCompileShader(vertShader);
        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, fragmentShaderProgram);
        GLES20.glCompileShader(fragShader);
        GLES20.glAttachShader(shaderProgram, vertShader);
        GLES20.glAttachShader(shaderProgram, fragShader);
        GLES20.glLinkProgram(shaderProgram);
        GLES20.glUseProgram(shaderProgram);
        posCoord = GLES20.glGetAttribLocation(shaderProgram, "coord");
        posTex = GLES20.glGetAttribLocation(shaderProgram, "texcoord");
        posTrans = GLES20.glGetUniformLocation(shaderProgram, "trans");
        posProj = GLES20.glGetUniformLocation(shaderProgram, "proj");

        vboCoord = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboCoord);
        float cube_vertices[][] = {{1.0f / 2, 1.0f / 2, 0.f}, {1.0f / 2, -1.0f / 2, 0.f}, {-1.0f / 2, -1.0f / 2, 0.f}, {-1.0f / 2, 1.0f / 2, 0.f}};
        FloatBuffer cube_vertices_buffer = FloatBuffer.wrap(flatten(cube_vertices));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW);

        vboTex = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboTex);
        int cube_vertex_colors[][] = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};
        ByteBuffer cube_vertex_colors_buffer = ByteBuffer.wrap(byteArrayFromIntArray(flatten(cube_vertex_colors)));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertex_colors_buffer.limit(), cube_vertex_colors_buffer, GLES20.GL_STATIC_DRAW);

        vboFaces = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboFaces);
        short cube_faces[] = {3, 2, 1, 0};
        ShortBuffer cube_faces_buffer = ShortBuffer.wrap(cube_faces);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, cube_faces_buffer.limit() * 2, cube_faces_buffer, GLES20.GL_STATIC_DRAW);

        GLES20.glUniform1i(GLES20.glGetUniformLocation(shaderProgram, "texture"), 0);
        TextureHelper.texture = generateOneTexture();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureHelper.texture);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    public void render(Matrix44F projectionMatrix, Matrix44F cameraview, Vec2F size) {
        float size0 = size.data[0];
        float size1 = size.data[1];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboCoord);
        float height = size0 / 1000;
        float cube_vertices[][] = {{size0 / 2, size1 / 2, 0}, {size0 / 2, -size1 / 2, 0}, {-size0 / 2, -size1 / 2, 0}, {-size0 / 2, size1 / 2, 0}};
        FloatBuffer cube_vertices_buffer = FloatBuffer.wrap(flatten(cube_vertices));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(shaderProgram);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboCoord);
        GLES20.glEnableVertexAttribArray(posCoord);
        GLES20.glVertexAttribPointer(posCoord, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboTex);
        GLES20.glEnableVertexAttribArray(posTex);
        GLES20.glVertexAttribPointer(posTex, 2, GLES20.GL_UNSIGNED_BYTE, false, 0, 0);
        GLES20.glUniformMatrix4fv(posTrans, 1, false, cameraview.data, 0);
        GLES20.glUniformMatrix4fv(posProj, 1, false, projectionMatrix.data, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboFaces);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboFaces);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        TextureHelper.updateTexture();

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

}
